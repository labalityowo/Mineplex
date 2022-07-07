package mineplex.core.particleeffects;

import java.awt.*;
import java.util.Random;

import org.bukkit.Location;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.common.util.particles.NormalParticle;

public class BabyFireworkEffect extends Effect
{

	private Color _fireworkColor;
	private Location _fireworkLocation;
	private Random _random = new Random();
	private int _count = 0, _fireworkCount = 0, _maxCount = -1;
	private boolean _multipleColors = false;
	private int _currentColor = 0;
	private Color[] _colors;
	private boolean _trail = true;

	public BabyFireworkEffect(Location location, Color color)
	{
		this(location, color, -1);
	}

	public BabyFireworkEffect(Location location, Color color, int maxCount)
	{
		super(10, new EffectLocation(location), 2);
		_fireworkColor = color;
		_fireworkLocation = location.clone();
		if (color == null)
		{
			_multipleColors = true;
			_fireworkColor = Color.RED;
		}

		_colors = new Color[0];
		_maxCount = maxCount;
	}

	public BabyFireworkEffect(Location location, Color... colors)
	{
		super(10, new EffectLocation(location), 2);
		_fireworkLocation = location.clone();
		_colors = colors;
		_fireworkColor = _colors[0];
		_multipleColors = true;
	}

	public void setCount(int count)
	{
		_count = count;
	}

	public void setTrail(boolean trail)
	{
		_trail = trail;
	}

	@Override
	public void onStart()
	{
		if (!_trail)
		{
			_count = 6;
		}
	}

	@Override
	public void runEffect()
	{
		if (_maxCount != -1)
		{
			if (_maxCount >= _fireworkCount)
			{
				stop();
			}
		}
		if (_count == 0)
		{
			double randX = _random.nextDouble() * 2 - 1, randY = _random.nextDouble() + .5,
					randZ = _random.nextDouble()  * 2 - 1;
			_fireworkLocation = _fireworkLocation.clone().add(randX, randY, randZ);
		}
		if (_count < 6 && _count % 2 == 0)
		{
			NormalParticle normalParticle = new NormalParticle(UtilParticle.ParticleType.FIREWORKS_SPARK,
					_fireworkLocation.clone());
			normalParticle.display();
			_fireworkLocation = _fireworkLocation.clone().add(0, .2, 0);
		}
		// Displays the colored baby firework
		else if (_count == 6)
		{
			ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST,
					new DustSpellColor(_fireworkColor), _fireworkLocation.clone());
			coloredParticle.display();
		}
		else
		{
			ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST,
					new DustSpellColor(_fireworkColor), _fireworkLocation.clone());
			// Y UP
			coloredParticle.setLocation(_fireworkLocation.clone().add(0, (_fireworkCount * .125), 0));
			coloredParticle.display();
			// Y DOWN
			coloredParticle.setLocation(_fireworkLocation.clone().add(0, (_fireworkCount * .125) * -1, 0));
			coloredParticle.display();
			// X POSITIVE
			coloredParticle.setLocation(_fireworkLocation.clone().add((_fireworkCount * .125), 0, 0));
			coloredParticle.display();
			// X NEGATIVE
			coloredParticle.setLocation(_fireworkLocation.clone().add((_fireworkCount * .125) * -1, 0, 0));
			coloredParticle.display();
			// Z POSITIVE
			coloredParticle.setLocation(_fireworkLocation.clone().add(0, 0, (_fireworkCount * .125)));
			coloredParticle.display();
			// Z NEGATIVE
			coloredParticle.setLocation(_fireworkLocation.clone().add(0, 0, (_fireworkCount * .125) * -1));
			coloredParticle.display();
			_fireworkCount++;
			if (_multipleColors)
			{
				if (_colors.length == 0)
				{
					if (_fireworkColor == Color.RED)
						_fireworkColor = Color.WHITE;
					else if (_fireworkColor == Color.WHITE)
						_fireworkColor = Color.BLUE;
					else
						_fireworkColor = Color.RED;
				}
				else
				{
					_currentColor++;
					if (_currentColor == _colors.length)
					{
						_currentColor = 0;
					}
					_fireworkColor = _colors[_currentColor];
				}
			}
		}

		_count++;
	}

}
