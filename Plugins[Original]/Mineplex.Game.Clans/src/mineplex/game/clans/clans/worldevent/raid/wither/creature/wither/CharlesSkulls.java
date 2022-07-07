package mineplex.game.clans.clans.worldevent.raid.wither.creature.wither;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossAbility;

public class CharlesSkulls extends BossAbility<CharlesWitherton, Wither>
{
	private long _lastUnguided;
	private long _lastGuided;
	private long _lastBombard;
	private Map<WitherSkull, Entity> _guidedSkulls;
	
	public CharlesSkulls(CharlesWitherton creature)
	{
		super(creature);
		
		_guidedSkulls = new HashMap<>();
		_lastUnguided = System.currentTimeMillis();
		_lastGuided = System.currentTimeMillis();
		_lastBombard = -1;
	}

	@Override
	public void tick()
	{
		if (getBoss().getHealthPercent() <= 0.75)
		{
			if (_lastBombard == -1)
			{
				_lastBombard = System.currentTimeMillis();
			}
			else
			{
				if (UtilTime.elapsed(_lastBombard, 25000))
				{
					Player target = UtilPlayer.getClosest(getLocation());
					if (target != null)
					{
						_lastBombard = System.currentTimeMillis();
						UtilEnt.LookAt(getEntity(), target.getEyeLocation());
						for (int i = 0; i < 15; i++)
						{
							UtilEnt.addFlag(getEntity().launchProjectile(WitherSkull.class), "BOMBARD");
						}
						return;
					}
				}
			}
		}
		if (UtilTime.elapsed(_lastUnguided, 10000))
		{
			Player target = UtilPlayer.getClosest(getLocation());
			if (target != null)
			{
				_lastUnguided = System.currentTimeMillis();
				UtilEnt.LookAt(getEntity(), target.getEyeLocation());
				getEntity().launchProjectile(WitherSkull.class);
				return;
			}
		}
		if (UtilTime.elapsed(_lastGuided, 5000))
		{
			Player target = UtilPlayer.getClosest(getLocation());
			if (target != null)
			{
				_lastGuided = System.currentTimeMillis();
				UtilEnt.LookAt(getEntity(), target.getEyeLocation());
				WitherSkull skull = getEntity().launchProjectile(WitherSkull.class);
				UtilEnt.addFlag(skull, "GUIDED");
				_guidedSkulls.put(skull, target);
			}
		}
		
		Iterator<Entry<WitherSkull, Entity>> iterator = _guidedSkulls.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<WitherSkull, Entity> entry = iterator.next();
			if (!entry.getKey().isValid() || entry.getKey().isDead())
			{
				iterator.remove();
				continue;
			}
			Vector velocity = UtilAlg.getTrajectory(entry.getKey(), entry.getValue());
			entry.getKey().setDirection(velocity);
			entry.getKey().setVelocity(velocity.multiply(0.6));
		}
	}
	
	@EventHandler
	public void onExplode(EntityExplodeEvent event)
	{
		if (event.getEntity().getEntityId() == getEntity().getEntityId())
		{
			event.blockList().clear();
			return;
		}
		if (event.getEntity() instanceof WitherSkull)
		{
			WitherSkull skull = (WitherSkull) event.getEntity();
			if (skull.getShooter() instanceof Wither && ((Wither)skull.getShooter()).getEntityId() == getEntity().getEntityId())
			{
				event.blockList().clear();
				return;
			}
		}
	}
	
	@EventHandler
	public void onSkullHit(ProjectileHitEvent event)
	{
		if (event.getEntity() instanceof WitherSkull)
		{
			WitherSkull skull = (WitherSkull) event.getEntity();
			if (skull.getShooter() instanceof Wither && ((Wither)skull.getShooter()).getEntityId() == getEntity().getEntityId())
			{
				UtilParticle.PlayParticle(ParticleType.EXPLODE, skull.getLocation(), null, 0, 2, ViewDist.MAX, UtilServer.getPlayers());
				skull.getWorld().playSound(skull.getLocation(), Sound.EXPLODE, 10, 0);
				if (UtilEnt.hasFlag(skull, "GUIDED"))
				{
					Player hit = UtilPlayer.getClosest(skull.getLocation(), 0.5);
					if (hit != null)
					{
						getBoss().getEvent().getDamageManager().NewDamageEvent(hit, getEntity(), skull, DamageCause.PROJECTILE, 2, true, true, false, getEntity().getName(), "Guided Skull");
					}
				}
				else if (UtilEnt.hasFlag(skull, "BOMBARD"))
				{
					Player hit = UtilPlayer.getClosest(skull.getLocation(), 0.5);
					if (hit != null)
					{
						getBoss().getEvent().getDamageManager().NewDamageEvent(hit, getEntity(), skull, DamageCause.PROJECTILE, 2, true, true, false, getEntity().getName(), "Bombardment");
					}
				}
				else
				{
					Player hit = UtilPlayer.getClosest(skull.getLocation(), 0.5);
					if (hit != null)
					{
						getBoss().getEvent().getDamageManager().NewDamageEvent(hit, getEntity(), skull, DamageCause.PROJECTILE, 4, true, true, false, getEntity().getName(), "Wither Skull");
						getBoss().getEvent().getCondition().Factory().Wither("Wither Skull", hit, getEntity(), 3, 0, false, true, false);
					}
				}
				skull.remove();
			}
		}
	}

	@Override
	public boolean canMove()
	{
		return true;
	}

	@Override
	public boolean inProgress()
	{
		return false;
	}

	@Override
	public boolean hasFinished()
	{
		return false;
	}

	@Override
	public void setFinished() {}
}