package mineplex.core.common.util.particles.effects;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;

/**
 * Creates a line of particles with customise able traits.
 * 
 * Most noticeable with Blink skills.
 */
public class LineParticle
{

	private final Location _start;
	private Vector _direction;
	private Location _lastLocation;

	private double _curRange;
	private final double _incrementedRange;
	private final double _maxRange;

	private boolean _ignoreAllBlocks;

	private final ParticleType _particleType;
	private final Player[] _toDisplay;

	public LineParticle(Location start, Vector direction, double incrementedRange, double maxRange, ParticleType particleType, Player... toDisplay)
	{
		this(start, null, direction, incrementedRange, maxRange, particleType, toDisplay);
	}

	public LineParticle(Location start, Location end, Vector direction, double incrementedRange, double maxRange, ParticleType particleType, Player... toDisplay)
	{
		_start = start;
		_direction = direction;
		_lastLocation = start;
		
		_curRange = 0;
		_incrementedRange = incrementedRange;
		_maxRange = maxRange;

		_particleType = particleType;
		_toDisplay = toDisplay;

		if (_direction == null)
		{
			_direction = UtilAlg.getTrajectory(start, end);
		}
	}

	/**
	 * Advances the line.
	 * 
	 * @return true when the line has reached its target or has collided with a solid block.
	 */
	public boolean update()
	{
		boolean done = _curRange > _maxRange;
		Location newTarget = _start.clone().add(_direction.clone().multiply(_curRange));

		if (newTarget.getY() < 0)
		{
			newTarget.add(0, 0.2, 0);
		}

		_lastLocation = newTarget;

		if (!_ignoreAllBlocks && UtilBlock.solid(newTarget.getBlock()) && UtilBlock.solid(newTarget.getBlock().getRelative(BlockFace.UP)))
		{
			done = true;
		}

		_curRange += _incrementedRange;

		UtilParticle.PlayParticle(_particleType, newTarget, null, 0, 1, ViewDist.LONG, _toDisplay);

		return done;
	}

	public void setIgnoreAllBlocks(boolean b)
	{
		_ignoreAllBlocks = b;
	}

	public Location getLastLocation()
	{
		return _lastLocation;
	}

	public Location getDestination()
	{
		return _lastLocation.subtract(_direction);
	}
}
