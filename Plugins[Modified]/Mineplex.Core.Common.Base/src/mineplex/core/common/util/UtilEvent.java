package mineplex.core.common.util;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class UtilEvent
{	
	public enum ActionType
	{
		L,
		L_AIR,
		L_BLOCK,
		R,
		R_AIR,
		R_BLOCK,
		ANY
	}
	
	public static boolean isAction(PlayerInteractEvent event, ActionType action)
	{
		if (action == ActionType.L)
			return (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK);
		
		if (action == ActionType.L_AIR)
			return (event.getAction() == Action.LEFT_CLICK_AIR);
		
		if (action == ActionType.L_BLOCK)
			return (event.getAction() == Action.LEFT_CLICK_BLOCK);
		
		if (action == ActionType.R)
			return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK);
		
		if (action == ActionType.R_AIR)
			return (event.getAction() == Action.RIGHT_CLICK_AIR);
		
		if (action == ActionType.R_BLOCK)
			return (event.getAction() == Action.RIGHT_CLICK_BLOCK);
		
		if (action == ActionType.ANY)
			return event.getAction() != Action.PHYSICAL; 
		
		return false;
	}
	
	public static boolean isBowDamage(EntityDamageEvent event)
	{
		if (!(event instanceof EntityDamageByEntityEvent))
			return false;
		
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event;
		return e.getDamager() instanceof Arrow;
	}
	
	public static LivingEntity GetDamagerEntity(EntityDamageEvent event, boolean ranged)
	{
		if (!(event instanceof EntityDamageByEntityEvent))
			return null;

		EntityDamageByEntityEvent eventEE = (EntityDamageByEntityEvent)event;

		//Get Damager
		if (eventEE.getDamager() instanceof LivingEntity)
			return (LivingEntity)eventEE.getDamager();

		if (!ranged)
			return null;

		if (!(eventEE.getDamager() instanceof Projectile))
			return null;

		Projectile projectile = (Projectile)eventEE.getDamager();

		if (projectile.getShooter() == null)
			return null;

		if (!(projectile.getShooter() instanceof LivingEntity))
			return null;

		return (LivingEntity)projectile.getShooter();
	}
}
