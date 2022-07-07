package nautilus.game.arcade.game.games.moba.minion;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.ai.goal.MobaAIMethod;
import nautilus.game.arcade.game.games.moba.ai.goal.MobaDirectAIMethod;
import nautilus.game.arcade.game.games.moba.boss.MobaBoss;
import nautilus.game.arcade.game.games.moba.boss.wither.WitherBoss;
import nautilus.game.arcade.game.games.moba.structure.tower.Tower;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class MinionWave implements Listener
{

	private static final int MAX_MINIONS_PER_WAVE = 6;
	private static final int TOO_CLOSE_SQUARED = 9;
	private static final int DAMAGE_RANGE_SQUARED = 16;
	private static final double DAMAGE_AMOUNT = 0.2;
	private static final MobaAIMethod AI_METHOD = new MobaDirectAIMethod();

	private final Moba _host;
	private final MinionManager _minionManager;
	private final GameTeam _owner;
	private final Class<? extends LivingEntity> _clazz;
	private final boolean _superMinions;
	private final long _startTime;

	private final List<Location> _path;
	private final List<Minion> _minions;

	public MinionWave(Moba host, MinionManager minionManager, GameTeam owner, List<Location> path, Class<? extends LivingEntity> clazz, boolean superMinions)
	{
		_host = host;
		_minionManager = minionManager;
		_owner = owner;
		_clazz = clazz;
		_superMinions = superMinions;
		_startTime = System.currentTimeMillis();
		_path = path;
		_minions = new ArrayList<>(MAX_MINIONS_PER_WAVE);

		UtilServer.RegisterEvents(this);

		spawn();

		UtilServer.runSyncTimer(new BukkitRunnable()
		{

			@Override
			public void run()
			{
				if (spawn())
				{
					cancel();
				}
			}
		}, 15, 15);
	}

	private boolean spawn()
	{
		_host.CreatureAllowOverride = true;

		Minion minion = new Minion(_path.get(0), _clazz, _superMinions);
		MobaUtil.setTeamEntity(minion.getEntity(), _owner);
		_minions.add(minion);

		_host.CreatureAllowOverride = false;

		return _minions.size() >= MAX_MINIONS_PER_WAVE;
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Minion minion : _minions)
		{
			LivingEntity entity = minion.getEntity();
			Location target = null;
			Location towerTarget = targetTower(minion);
			Minion minionTarget = targetMinion(minion);
			Location witherTarget = targetWither(minion);

			// Priority -> Tower -> Minion -> Wither

			if (towerTarget != null)
			{
				target = towerTarget;
			}
			else if (minionTarget != null)
			{
				target = minionTarget.getEntity().getLocation();
				minion.setTarget(target);
			}
			else if (witherTarget != null)
			{
				target = witherTarget;
			}

			if (target != null)
			{
				minion.setTarget(target);

				// Too close
				if (UtilMath.offsetSquared(entity.getLocation(), target) < TOO_CLOSE_SQUARED)
				{
					continue;
				}
			}

			if (!AI_METHOD.updateMovement(entity, minion.getTarget(), 4F))
			{
				int newTarget = minion.getTargetIndex() + 1;

				if (newTarget == _path.size())
				{
					continue;
				}

				minion.setTargetIndex(newTarget);
				minion.setTarget(_path.get(newTarget));
			}
		}
	}

	@EventHandler
	public void updateUnregister(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		Iterator<Minion> iterator = _minions.iterator();

		while (iterator.hasNext())
		{
			Minion minion = iterator.next();
			LivingEntity entity = minion.getEntity();

			if (entity == null || entity.isDead() || !entity.isValid())
			{
				if (entity != null && entity.getKiller() != null)
				{
					_host.AddGems(entity.getKiller(), 0.1, "Minion Kills", true, true);
				}

				iterator.remove();
			}
		}

		// Only should unregister the wave after all entities have spawned
		if (_minions.isEmpty() && UtilTime.elapsed(_startTime, 10000))
		{
			UtilServer.Unregister(this);
			_minionManager.unregisterWave(this);
		}
	}

	private Minion targetMinion(Minion minion)
	{
		for (MinionWave wave : _minionManager.getWaves())
		{
			// Same team
			if (wave.getOwner().equals(_owner))
			{
				continue;
			}

			for (Minion otherMinion : wave.getMinions())
			{
				double distSquared = UtilMath.offsetSquared(minion.getEntity(), otherMinion.getEntity());

				if (distSquared < 3)
				{
					return otherMinion;
				}
				else if (distSquared > Tower.TARGET_RANGE_SQUARED)
				{
					continue;
				}

				return otherMinion;
			}
		}

		return null;
	}

	private Location targetTower(Minion minion)
	{
		for (Tower tower : _host.getTowerManager().getTowers())
		{
			if (tower.isDead() || tower.getOwner().equals(_owner))
			{
				continue;
			}

			Location location = tower.getCrystal().getLocation();
			double distSquared = UtilMath.offsetSquared(minion.getEntity(), tower.getCrystal());

			if (distSquared < 3)
			{
				return location;
			}
			else if (distSquared > Tower.TARGET_RANGE_SQUARED)
			{
				continue;
			}

			return location;
		}

		return null;
	}

	private Location targetWither(Minion minion)
	{
		for (WitherBoss boss : _host.getBossManager().getWitherBosses())
		{
			if (boss.isDead() || boss.getTeam().equals(_owner))
			{
				continue;
			}

			Location location = boss.getEntity().getLocation();
			double distSquared = UtilMath.offsetSquared(minion.getEntity(), boss.getEntity());

			if (distSquared < 3)
			{
				return location;
			}
			else if (distSquared > Tower.TARGET_RANGE_SQUARED)
			{
				continue;
			}

			return location;
		}

		return null;
	}

	@EventHandler
	public void damageMinions(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (Minion minion : _minions)
		{
			for (MinionWave wave : _minionManager.getWaves())
			{
				// Same team
				if (wave.getOwner().equals(_owner))
				{
					continue;
				}

				for (Minion otherMinion : wave.getMinions())
				{
					// Cannot damage, not close enough
					if (UtilMath.offsetSquared(minion.getEntity(), otherMinion.getEntity()) > DAMAGE_RANGE_SQUARED)
					{
						continue;
					}

					_host.getArcadeManager().GetDamage().NewDamageEvent(otherMinion.getEntity(), minion.getEntity(), null, DamageCause.CUSTOM, DAMAGE_AMOUNT, false, true, false, UtilEnt.getName(minion.getEntity()), "Minion");
				}
			}
		}

		for (Minion minion : _minions)
		{
			for (Tower tower : _host.getTowerManager().getTowers())
			{
				// Cannot damage, not close enough
				if (!_host.getTowerManager().canDamage(tower, _owner) || UtilMath.offsetSquared(minion.getEntity(), tower.getCrystal()) > DAMAGE_RANGE_SQUARED)
				{
					continue;
				}

				tower.damage(DAMAGE_AMOUNT);
			}
		}
	}

	@EventHandler
	public void damageTower(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (Minion minion : _minions)
		{
			for (Tower tower : _host.getTowerManager().getTowers())
			{
				// Cannot damage, not close enough
				if (!_host.getTowerManager().canDamage(tower, _owner) || UtilMath.offsetSquared(minion.getEntity(), tower.getCrystal()) > DAMAGE_RANGE_SQUARED)
				{
					continue;
				}

				tower.damage(DAMAGE_AMOUNT);
			}
		}
	}

	@EventHandler
	public void damageWither(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		Collection<WitherBoss> bosses = _host.getBossManager().getWitherBosses();

		for (Minion minion : _minions)
		{
			for (WitherBoss boss : bosses)
			{
				// Dead, not close enough
				if (boss.isDead() || MobaUtil.isTeamEntity(boss.getEntity(), _owner) || UtilMath.offsetSquared(minion.getEntity(), boss.getEntity()) > DAMAGE_RANGE_SQUARED)
				{
					continue;
				}

				_host.getArcadeManager().GetDamage().NewDamageEvent(boss.getEntity(), minion.getEntity(), null, DamageCause.CUSTOM, DAMAGE_AMOUNT, false, false, false, UtilEnt.getName(minion.getEntity()), "Minion");
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void damage(CustomDamageEvent event)
	{
		// Not a Minion
		if (event.isCancelled() || !isMinion(event.GetDamageeEntity()))
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();
		Player damager = event.GetDamagerPlayer(true);
		GameTeam team = _host.GetTeam(damager);

		if (team != null && _owner.equals(team))
		{
			event.SetCancelled("Same Team Minion");
		}
		else
		{
			Minion minion = getMinion(damagee);

			if (minion != null)
			{
				if (event.GetReason() != null && event.GetReason().contains("Tower"))
				{
					event.AddMod("Tower Damage", -event.GetDamage() / 2D);
				}

				minion.updateDisplay(minion.getEntity().getHealth() - event.GetDamage());
			}
		}
	}

	@EventHandler
	public void entityCombust(EntityCombustEvent event)
	{
		if (isMinion(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void suffocation(CustomDamageEvent event)
	{
		if (isMinion(event.GetDamageeEntity()) && event.GetCause() == DamageCause.SUFFOCATION)
		{
			event.SetCancelled("Minion Suffocation");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void entityDeath(EntityDeathEvent event)
	{
		if (!isMinion(event.getEntity()))
		{
			return;
		}

		event.getDrops().clear();
		event.setDroppedExp(0);
	}

	public void cleanup()
	{
		for (Minion minion : _minions)
		{
			minion.getEntity().remove();
		}
	}

	public List<Minion> getMinions()
	{
		return _minions;
	}

	public GameTeam getOwner()
	{
		return _owner;
	}

	private Minion getMinion(Entity entity)
	{
		for (Minion minion : _minions)
		{
			if (entity.equals(minion.getEntity()))
			{
				return minion;
			}
		}

		return null;
	}

	private boolean isMinion(Entity entity)
	{
		return getMinion(entity) != null;
	}

}
