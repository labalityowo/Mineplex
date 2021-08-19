package mineplex.core.common.util;

import mineplex.core.common.events.EntityVelocityChangeEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class UtilAction
{
	private static VelocityReceiver _velocityFix;
	
	public static void registerVelocityFix(VelocityReceiver velocityFix) 
	{
		_velocityFix = velocityFix;
	}
	
	public static void velocity(Entity ent, Vector vec) 
	{
		velocity(ent, vec, vec.length(), false, 0, 0, vec.length(), false);
	}

	public static void velocity(Entity ent, double str, double yAdd, double yMax, boolean groundBoost)
	{
		velocity(ent, ent.getLocation().getDirection(), str, false, 0, yAdd, yMax, groundBoost);
	}

	public static void velocity(Entity ent, Vector vec, double str, boolean ySet, double yBase, double yAdd, double yMax, boolean groundBoost)
	{
		if (Double.isNaN(vec.getX()) || Double.isNaN(vec.getY()) || Double.isNaN(vec.getZ()) || vec.length() == 0)
		{
			zeroVelocity(ent);
			return;
		}

		//YSet
		if (ySet)
			vec.setY(yBase);

		//Modify
		vec.normalize();
		vec.multiply(str);

		//YAdd
		vec.setY(vec.getY() + yAdd);

		//Limit
		if (vec.getY() > yMax)
			vec.setY(yMax);

		if (groundBoost)
			if (UtilEnt.isGrounded(ent))
				vec.setY(vec.getY() + 0.2); 
		
		EntityVelocityChangeEvent event = new EntityVelocityChangeEvent(ent, vec);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		vec = event.getVelocity();

		//Velocity
		ent.setFallDistance(0);

		//Store It!
		if (ent instanceof Player && _velocityFix != null)
		{
			_velocityFix.setPlayerVelocity(((Player)ent), vec);
		}

		ent.setVelocity(vec);
	}

	public static void zeroVelocity(Entity ent) 
	{
		Vector vec = new Vector(0,0,0);
		
		EntityVelocityChangeEvent event = new EntityVelocityChangeEvent(ent, vec);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		vec = event.getVelocity();
		
		ent.setFallDistance(0);

		//Store It!
		if (ent instanceof Player && _velocityFix != null)
		{
			_velocityFix.setPlayerVelocity(((Player)ent), vec);
		}

		ent.setVelocity(vec);	
	}

	
}
