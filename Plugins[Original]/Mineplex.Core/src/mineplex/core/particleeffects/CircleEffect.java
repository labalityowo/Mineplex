package mineplex.core.particleeffects;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;

public class CircleEffect extends Effect
{

	private double _radius;
	private Color _color;
	private int _steps = 0;
	private boolean _instantly = true;
	private List<Color> _randomColors = new ArrayList<>();
	private int _maxCircles = -1;
	private int _totalCircles = 0;
	private double _yOffset = 0.0;
	private int _particles = 20;
	private boolean _infinite = false;

	private static final double RANDOM_COLOR_CHANCE = 0.5;

	public CircleEffect(Location location, double radius, Color color)
	{
		super(-1, new EffectLocation(location));
		_radius = radius;
		_color = color;
	}

	public CircleEffect(Location location, double radius, Color color, boolean instantly)
	{
		this(location, radius, color);
		_instantly = instantly;
	}

	public CircleEffect(Entity entity, double radius, Color color, boolean instantly)
	{
		super(-1, new EffectLocation(entity));
		_radius = radius;
		_color = color;
		_instantly = instantly;
	}

	public CircleEffect(Location location, double radius, Color color, boolean instantly, int particles)
	{
		this(location, radius, color, instantly);
		_particles = particles;
	}

	public CircleEffect(Entity entity, double radius, Color color, boolean instantly, int particles)
	{
		this(entity, radius, color, instantly);
		_particles = particles;
	}

	public void addRandomColor(Color color)
	{
		_randomColors.add(color);
	}

	public void setMaxCircles(int circles)
	{
		_maxCircles = circles;
	}

	public void setYOffset(double yOffset)
	{
		_yOffset = yOffset;
	}

	public void setInfinite(boolean infinite)
	{
		_infinite = infinite;
	}

	@Override
	public void runEffect()
	{
		if (_instantly)
		{
			for (int i = 0; i < _particles; i++)
			{
				Location location = getEffectLocation().getLocation().add(0, _yOffset, 0);
				double increment = (2 * Math.PI) / _particles;
				double angle = _steps * increment;
				Vector vector = new Vector(Math.cos(angle) * _radius, 0, Math.sin(angle) * _radius);
				ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST, new DustSpellColor(_color), location.add(vector));
				coloredParticle.display();
				if (_randomColors.size() > 0)
				{
					double r = UtilMath.random.nextDouble();
					if (r < RANDOM_COLOR_CHANCE)
					{
						coloredParticle.setColor(new DustSpellColor(getRandomColor()));
						coloredParticle.display();
					}
				}
				_steps++;
			}
			if (!_infinite)
				stop();
		}
		else
		{
			if (_maxCircles != -1)
			{
				if (_totalCircles >= _maxCircles)
				{
					stop();
					return;
				}
			}
			Location location = getEffectLocation().getLocation().add(0, _yOffset, 0);
			double increment = (2 * Math.PI) / _particles;
			double angle = _steps * increment;
			Vector vector = new Vector(Math.cos(angle) * _radius, 0, Math.sin(angle) * _radius);
			ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST, new DustSpellColor(_color), location.add(vector));
			coloredParticle.display();
			if (_randomColors.size() > 0)
			{
				double r = UtilMath.random.nextDouble();
				if (r < RANDOM_COLOR_CHANCE)
				{
					coloredParticle.setColor(new DustSpellColor(getRandomColor()));
					coloredParticle.display();
				}
			}
			_steps++;
			if (_steps >= _particles)
			{
				_totalCircles++;
				_steps = 0;
			}
		}
	}

	private Color getRandomColor()
	{
		int r = UtilMath.random.nextInt(_randomColors.size());
		return _randomColors.get(r);
	}

}
