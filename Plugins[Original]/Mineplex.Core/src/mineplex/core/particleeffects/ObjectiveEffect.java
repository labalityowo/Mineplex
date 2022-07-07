package mineplex.core.particleeffects;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;

public class ObjectiveEffect extends Effect
{

	private final Location _current;
	private final Location _target;
	private final Vector _direction;

	private final float _turnMultiplier;
	private final float _blocksToAdvance;

	private final ParticleType _particleType;

	public ObjectiveEffect(Location start, Vector startVector, Location target, float turnMultiplier, float blocksToAdvance, ParticleType particleType)
	{
		super(120, new EffectLocation(start));

		_current = start;
		_target = target;
		_turnMultiplier = turnMultiplier;
		_blocksToAdvance = blocksToAdvance;
		_particleType = particleType;
		_direction = startVector;

		if (_direction.getY() < 0)
		{
			_direction.setY(0);
		}

		_direction.normalize();
	}

	@Override
	public void runEffect()
	{
		_direction.add(UtilAlg.getTrajectory(_current, _target).multiply(_turnMultiplier));
		_direction.normalize();

		_current.add(_direction.clone().multiply(_blocksToAdvance));

		UtilParticle.PlayParticleToAll(_particleType, _current, null,  0, 1, ViewDist.LONG);

		if (Math.random() < 0.2)
		{
			_current.getWorld().playSound(_current, Sound.FIZZ, 1.5F, 0.5F);
		}

		if (UtilMath.offsetSquared(_current, _target) < 9)
		{
			stop();
		}
	}
}
