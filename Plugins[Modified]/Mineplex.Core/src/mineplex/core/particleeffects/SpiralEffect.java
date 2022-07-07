package mineplex.core.particleeffects;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;

public abstract class SpiralEffect extends Effect
{

	private static final double DELTA_THETA = Math.PI / 20D;
	private static final double DELTA_Z = 0.1;
	private static final double DELTA_RADIUS = 0.05;

	private final double _maxRadius;
	private final int _iterations;

	private double _radius, _theta, _z;

	public SpiralEffect(int ticks, double maxRadius, Location location)
	{
		this(ticks, maxRadius, 1, location);
	}

	public SpiralEffect(int ticks, double maxRadius, int iterations, Location location)
	{
		super(ticks, new EffectLocation(location));

		_maxRadius = maxRadius;
		_iterations = iterations;
	}

	@Override
	public void runEffect()
	{
		for (int i = 0; i < _iterations; i++)
		{
			double x = _radius * Math.cos(_theta);
			double y = _radius * Math.sin(_theta);
			double z = _z += DELTA_Z;

			Location location = getEffectLocation().getFixedLocation();

			location.add(x, y, z);

			rotateDirection(location);

			location.subtract(x * 2, y * 2, 0);

			rotateDirection(location);

			_theta += DELTA_THETA;

			if (_radius < _maxRadius)
			{
				_radius += DELTA_RADIUS;
			}
		}
	}

	private void rotateDirection(Location location)
	{
		Location fixedLocation = getEffectLocation().getFixedLocation();
		Vector vector = location.toVector().subtract(fixedLocation.toVector());

		UtilAlg.rotateAroundXAxis(vector, Math.toRadians(location.getPitch()));
		UtilAlg.rotateAroundYAxis(vector, Math.toRadians(location.getYaw()));

		playParticle(fixedLocation.add(vector));
	}

	public abstract void playParticle(Location location);
}
