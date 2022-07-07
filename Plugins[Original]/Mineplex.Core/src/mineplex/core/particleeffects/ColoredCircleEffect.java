package mineplex.core.particleeffects;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.RGBData;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;

public class ColoredCircleEffect extends Effect
{

	private double _radius;
	private List<Color> _colors;
	private int _steps = 0;
	private boolean _instantly = true;
	private int _maxCircles = -1;
	private int _totalCircles = 0;
	private double _yOffset = 0.0;
	private Player[] _players = null;

	private static final int PARTICLES_PER_CIRCLE = 20;

	public ColoredCircleEffect(Entity entity, double radius, boolean instantly, Color... colors)
	{
		super(-1, new EffectLocation(entity));
		_radius = radius;
		_colors = new ArrayList<>();
		_instantly = instantly;
		Collections.addAll(_colors, colors);
	}

	public void setPlayers(Player... players)
	{
		_players = players;
	}

	public void setMaxCircles(int circles)
	{
		_maxCircles = circles;
	}

	public void setYOffset(double yOffset)
	{
		_yOffset = yOffset;
	}

	public void addColor(Color color)
	{
		_colors.add(color);
	}

	public void addColors(Color... colors)
	{
		Collections.addAll(_colors, colors);
	}

	public void addColor(RGBData rgbData)
	{
		_colors.add(new Color(rgbData.getFullRed(), rgbData.getFullGreen(), rgbData.getFullBlue()));
	}

	public void addColors(RGBData... rgbDatas)
	{
		for (RGBData rgbData : rgbDatas)
		{
			addColor(rgbData);
		}
	}

	@Override
	public void runEffect()
	{
		if (_instantly)
		{
			for (int i = 0; i < PARTICLES_PER_CIRCLE; i++)
			{
				Location location = getEffectLocation().getLocation().add(0, _yOffset, 0);
				double increment = (2 * Math.PI) / PARTICLES_PER_CIRCLE;
				double angle = _steps * increment;
				Vector vector = new Vector(Math.cos(angle) * _radius, 0, Math.sin(angle) * _radius);
				ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST, new DustSpellColor(getNextColor()), location.add(vector));
				if (_players != null)
					coloredParticle.display(UtilParticle.ViewDist.NORMAL, _players);
				else
					coloredParticle.display();
				_steps++;
			}
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
			double increment = (2 * Math.PI) / PARTICLES_PER_CIRCLE;
			double angle = _steps * increment;
			Vector vector = new Vector(Math.cos(angle) * _radius, 0, Math.sin(angle) * _radius);
			ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST, new DustSpellColor(getNextColor()), location.add(vector));
			if (_players != null)
				coloredParticle.display(UtilParticle.ViewDist.NORMAL, _players);
			else
				coloredParticle.display();
			_steps++;
			if (_steps >= PARTICLES_PER_CIRCLE)
			{
				_totalCircles++;
				_steps = 0;
			}
		}
	}

	private Color getNextColor()
	{
		int r = UtilMath.random.nextInt(_colors.size());
		return _colors.get(r);
	}

}
