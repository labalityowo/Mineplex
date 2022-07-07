package mineplex.game.clans.clans.siege.weapon;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.siege.weapon.projectile.WeaponProjectile;

public class TntProjectile extends WeaponProjectile
{
	public TntProjectile(SiegeWeapon weapon, Location origin, double yawRot, double yVel, double xMulti)
	{
		super(weapon, origin, yawRot, yVel, xMulti);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onTntExplode(EntityExplodeEvent event)
	{
		if (event.getEntity().equals(_projectileEntity))
		{
			((TNTPrimed) event.getEntity()).setFuseTicks(60);
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTntExplode(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		((TNTPrimed) _projectileEntity).setFuseTicks(60);
	}
	
	@Override
	public Entity spawn()
	{
		TNTPrimed tnt = _origin.getWorld().spawn(_origin, TNTPrimed.class);
		
		Vector velocity = UtilAlg.getTrajectory(
					_origin,
					UtilAlg.moveForward(
							_origin,
							2.,
							(float) Math.toDegrees(_yawRot), false))
				.multiply(_xMulti)
				.setY(_yVel);
		
		tnt.setVelocity(velocity);
		
		return tnt;
	}
}