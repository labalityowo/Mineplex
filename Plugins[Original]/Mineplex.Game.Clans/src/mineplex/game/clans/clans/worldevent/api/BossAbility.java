package mineplex.game.clans.clans.worldevent.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;

public abstract class BossAbility<T extends EventCreature<Y>, Y extends LivingEntity> implements Listener
{
	private T _creature;
	private Map<UUID, Long> _damaged = new HashMap<>();

	public BossAbility(T creature)
	{
		_creature = creature;
	}

	public abstract boolean canMove();

	public abstract boolean inProgress();
	
	public abstract boolean hasFinished();

	public abstract void setFinished();

	public abstract void tick();
	
	public boolean canDamage(Entity player)
	{
		if (_damaged.containsKey(player.getUniqueId()))
		{

			if (!UtilTime.elapsed(_damaged.get(player.getUniqueId()), 400))
			{
				return false;
			}
		}

		_damaged.put(player.getUniqueId(), System.currentTimeMillis());
		return true;
	}

	public int getCooldown()
	{
		return 3;
	}

	public Y getEntity()
	{
		return getBoss().getEntity();
	}

	public T getBoss()
	{
		return _creature;
	}

	public Location getLocation()
	{
		return getEntity().getLocation();
	}

	public Player getTarget()
	{
		return getTarget(30);
	}

	public Player getTarget(double minDistance, double maxDistance)
	{
		Player target = null;
		double dist = 0;

		for (Player player : UtilPlayer.getNearby(getLocation(), maxDistance, true))
		{
			if (!player.hasLineOfSight(getEntity()))
			{
				continue;
			}

			double d = player.getLocation().distance(getLocation());

			if (d < minDistance)
			{
				continue;
			}

			if (target == null || dist > d)
			{
				target = player;
				dist = d;
			}
		}

		return target;
	}

	public Player getTarget(double maxDistance)
	{
		return getTarget(0, maxDistance);
	}
}