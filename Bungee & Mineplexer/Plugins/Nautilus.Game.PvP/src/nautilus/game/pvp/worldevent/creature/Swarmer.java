package nautilus.game.pvp.worldevent.creature;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventMobMinion;

public class Swarmer extends EventMobMinion
{
	private int _leapRange = 2;

	public Swarmer(EventBase event, Location location, SwarmerHost host) 
	{
		super(event, location, "Swarmer", false, 10, EntityType.SILVERFISH, host);

		_target = host.GetEntity();
	}
	
	@Override
	public void Target(UpdateEvent event)
	{
		//None!
	}

	@Override
	public void DamagedCustom(CustomDamageEvent event)
	{
		if (event.GetCause() == DamageCause.FIRE)
		{
			if (!event.GetDamageeEntity().equals(GetEntity()))
				return;

			GetEntity().setFireTicks(0);

			event.SetCancelled("Swarm Resistance");
			return;
		}

		if (Math.random() > 0.5)
		{
			LivingEntity damager = event.GetDamagerEntity(true);
			if (damager != null)
				SetTarget(damager);
		}	
	}

	@Override
	public void DamagerCustom(CustomDamageEvent event)
	{
		if (!event.GetDamageeEntity().equals(_target))
			return;

		event.AddMod("Swarmer", "Swarm", 2, false);
		event.SetKnockback(false);
		
		_target = _host.GetEntity();
	}

	@EventHandler
	public void Heal(UpdateEvent event)
	{
		if (GetEntity() == null)
			return;

		if (event.getType() != UpdateType.SEC)
			return;
		
		ModifyHealth(1);
	}

	@Override
	public void Die()
	{
		Event.Manager.Blood().Effects(GetEntity().getEyeLocation(), 4, 0.5, 
				Sound.SILVERFISH_KILL, 1f, 1f, Material.BONE, (byte)0, false);
		
		Loot();
		Remove();
	}
	
	@Override
	public void Return(UpdateEvent event)
	{
		
	}

	@EventHandler
	public void Leap(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (!Valid())
			return;

		if (_target == null || _target.isDead() || !_target.isValid())
			_target = _host.GetEntity();

		if (!UtilEnt.isGrounded(GetEntity()))
			return;

		//Leap
		if (Math.random() > 0.95 || UtilMath.offset(GetEntity(), _target) > _leapRange)
		{
			Vector vec = UtilAlg.getTrajectory(GetEntity(), _target);
			UtilAction.velocity(GetEntity(), vec, Math.random()/2 + 0.5, false, 0, Math.random()/3, 0.3, true);
		}

		//Move
		((Creature)GetEntity()).setTarget(_target);
	}

	@Override
	public void StateChange(int newState) 
	{
		//None
	}
}
