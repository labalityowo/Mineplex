package nautilus.game.arcade.managers;

import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.condition.events.ConditionApplyEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SecondaryDamageManager implements Listener
{
	ArcadeManager Manager;
	public SecondaryDamageManager(ArcadeManager manager)
	{
		Manager = manager;
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void EntDamage(EntityDamageEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)
		{
			event.setCancelled(true);
			return;
		}
		if (!(event.getEntity() instanceof LivingEntity))
			return;
		
		LivingEntity ent = (LivingEntity) event.getEntity();
		if (ent.getWorld().getName().equals("world"))
		{
			event.setCancelled(true);
			
			if (event.getCause() == DamageCause.VOID)
				ent.teleport(Manager.GetLobby().getSpawn());
			
			return;
		}
		
		if (Manager.isSpectator(ent))
		{
			event.setCancelled(true);
			if (ent.getFireTicks() > 0)
			{
				ent.setFireTicks(-1);
			}
			
			return;
		}
		
		if (game.GetState() != GameState.Live)
		{
			event.setCancelled(true);
			return;
		}
		
		if (!game.Damage)
		{
			event.setCancelled(true);
			return;
		}
		
		if (ent instanceof Player)
		{
			if (!game.IsAlive((Player)ent))
			{
				event.setCancelled(true);
				return;
			}
		}
		
		if (event.getCause() == DamageCause.FALL)
		{
			if (!game.DamageFall)
			{
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void DamageEvent(EntityDamageByEntityEvent event)
	{ 
		Game game = Manager.GetGame();
		if (game == null)	
			return;

		if (!(event.getEntity() instanceof LivingEntity))
			return;
		
		LivingEntity damagee = (LivingEntity)event.getEntity();

		if (event.getDamager() instanceof LivingEntity || event.getDamager() instanceof Projectile)
		{
			LivingEntity damager = null;
			if (event.getDamager() instanceof LivingEntity)
			{
				damager = (LivingEntity)event.getDamager();
			}
			else
			{
				if (((Projectile)event.getDamager()).getShooter() instanceof LivingEntity)
				{
					damager = (LivingEntity)((Projectile)event.getDamager()).getShooter();
				}
			}
			//Damager Spec
			if (damager != null && Manager.isSpectator(damager))
			{
				event.setCancelled(true);
				return;
			}

			if (damager != null && damager instanceof Player && !game.IsAlive((Player)damager))
			{
				event.setCancelled(true);
				return;
			}

			//Entity vs Entity
			if (damagee != null && damager != null)	
			{
				//PvP
				if (damagee instanceof Player && damager instanceof Player)
				{
					if (!Manager.canHurt((Player)damagee, (Player)damager))
					{
						event.setCancelled(true);
						return;
					}
				}
				//PvE
				else if (damager instanceof Player)
				{
					if (!game.DamagePvE)
					{
						event.setCancelled(true);
						return;
					}
				}
				//EvP
				else if (damagee instanceof Player)
				{
					if (!game.DamageEvP)
					{
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void DamageExplosion(ConditionApplyEvent event)
	{ 
		if (event.isCancelled())
			return;

		if (event.GetCondition().GetType() != ConditionType.EXPLOSION)
			return;

		LivingEntity damagee = event.GetCondition().GetEnt();
		if (damagee == null)	return;
		if (!(damagee instanceof Player))	return;

		LivingEntity damager = event.GetCondition().GetSource();
		if (damager == null)	return;
		if (!(damager instanceof Player))	return;

		if (Manager.canHurt((Player)damagee, (Player)damager))
			return;

		event.setCancelled(true);
	}
}
