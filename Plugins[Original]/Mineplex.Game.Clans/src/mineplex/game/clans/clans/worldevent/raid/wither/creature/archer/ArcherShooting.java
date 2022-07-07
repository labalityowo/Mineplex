package mineplex.game.clans.clans.worldevent.raid.wither.creature.archer;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossAbility;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ArcherShooting extends BossAbility<DecayingArcher, Zombie>
{
	private long _lastShoot;
	private long _lastRope;
	
	public ArcherShooting(DecayingArcher creature)
	{
		super(creature);
		
		_lastShoot = System.currentTimeMillis();
		_lastRope = System.currentTimeMillis();
	}

	@Override
	public void tick()
	{
		if (getEntity().getTarget() != null && getEntity().getTarget() instanceof Player)
		{
			if (UtilTime.elapsed(_lastRope, 10000))
			{
				_lastRope = System.currentTimeMillis();
				UtilEnt.LookAt(getEntity(), getEntity().getTarget().getEyeLocation());
				UtilEnt.addFlag(getEntity().launchProjectile(Arrow.class), "ROPED_ARROW");
				return;
			}
			if (UtilTime.elapsed(_lastShoot, 2000))
			{
				_lastShoot = System.currentTimeMillis();
				UtilEnt.LookAt(getEntity(), getEntity().getTarget().getEyeLocation());
				getEntity().launchProjectile(Arrow.class);
			}
		}
	}
	
	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		if (event.GetDamagerEntity(event.GetCause() == DamageCause.PROJECTILE) == null)
		{
			return;
		}
		LivingEntity ent = event.GetDamagerEntity(event.GetCause() == DamageCause.PROJECTILE);
		if (ent.getEntityId() == getEntity().getEntityId())
		{
			if (event.GetCause() != DamageCause.PROJECTILE)
			{
				event.SetCancelled("Archer Only");
				return;
			}
			if (UtilEnt.hasFlag(event.GetProjectile(), "ROPED_ARROW"))
			{
				ent.setVelocity(UtilAlg.getTrajectory(ent, event.GetDamageeEntity()).normalize());
				event.AddMod("Roped Arrow", -event.GetDamage());
				return;
			}
			event.AddMod("Ranged Attack", 2 - event.GetDamage());
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