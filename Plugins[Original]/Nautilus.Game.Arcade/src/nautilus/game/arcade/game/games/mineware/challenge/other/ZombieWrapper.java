package nautilus.game.arcade.game.games.mineware.challenge.other;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeZombieInfection;

/**
 * This is a zombie wrapper class that holds the speed and a freeze timer.
 * 
 * @see ChallengeZombieInfection
 */
public class ZombieWrapper
{
	private ChallengeZombieInfection _challenge;
	private Zombie _wrapper;
	private boolean _frozen;
	private long _freezeTime;
	private float _speed;

	public ZombieWrapper(ChallengeZombieInfection challenge)
	{
		_challenge = challenge;
	}

	public Zombie spawn()
	{
		Location center = _challenge.getCenter();
		World world = center.getWorld();

		_wrapper = (Zombie) world.spawnEntity(center.clone().add(0.5, 1, 0.5), EntityType.ZOMBIE);

		UtilEnt.vegetate(_wrapper);
		UtilEnt.ghost(_wrapper, true, false);

		_wrapper.setCustomName(C.cRedB + "Infected Zombie");
		_wrapper.setCustomNameVisible(true);

		return _wrapper;
	}

	public void move(Player target)
	{
		UtilEnt.CreatureLook(_wrapper, target);
		UtilEnt.CreatureMove(_wrapper, target.getLocation(), _speed);
	}

	public void extinguish()
	{
		_wrapper.setFireTicks(0);
	}

	public Location getLocation()
	{
		return _wrapper.getLocation().clone();
	}

	public ChallengeZombieInfection getChallenge()
	{
		return _challenge;
	}

	public Zombie getEntity()
	{
		return _wrapper;
	}

	public void remove()
	{
		_wrapper.remove();
	}

	public void freeze()
	{
		_frozen = true;
	}

	public void unfreeze()
	{
		_frozen = false;
	}

	public boolean isFrozen()
	{
		return _frozen;
	}

	public void setFreezeTime(long freezeTime)
	{
		_freezeTime = freezeTime;
	}

	public long getFreezeTime()
	{
		return _freezeTime;
	}

	public void setSpeed(float speed)
	{
		_speed = speed;
	}

	public float getSpeed()
	{
		return _speed;
	}

	public void setTarget(Player target)
	{
		_wrapper.setTarget(target);
	}

	public Player getTarget()
	{
		return (Player) _wrapper.getTarget();
	}
}
