package nautilus.game.minekart.item.control;

import java.util.Collection;
import java.util.HashSet;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import nautilus.game.minekart.item.KartItemEntity;
import nautilus.game.minekart.item.world_items_default.RedShell;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartState;
import nautilus.game.minekart.kart.condition.ConditionType;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class Collision 
{
	public static boolean CollideBlock(KartItemEntity item) 
	{	
		Entity ent = item.GetEntity();
		if (ent == null)	return true;
		
		//Current Velocity
		Vector vel = item.GetVelocity();
		if (vel == null)	return true;

		if (vel.length() <= 0)
			return true;
			
		boolean collided = false;	
		double range = 0.31;
		
		Block block;
		
		block = ent.getLocation().add(range, 0, 0).getBlock();
		if (vel.getX() > 0 && UtilBlock.solid(block)) 		{vel.setX(-vel.getX()); collided = true;}

		block = ent.getLocation().add(-range, 0, 0).getBlock();
		if (vel.getX() < 0 && UtilBlock.solid(block))		{vel.setX(-vel.getX()); collided = true;}

		block = ent.getLocation().add(0, 0, range).getBlock();
		if (vel.getZ() > 0 && UtilBlock.solid(block))		{vel.setZ(-vel.getZ()); collided = true;}

		block = ent.getLocation().add(0, 0, -range).getBlock();
		if (vel.getZ() < 0 && UtilBlock.solid(block))		{vel.setZ(-vel.getZ()); collided = true;}
		
		return collided;
	}
	
	public static boolean CollidePlayer(KartItemEntity item, Collection<Kart> allKarts)
	{
		if (item.GetEntity() == null)
			return false;
		
		for (Kart kart : allKarts)
		{
			if (kart.GetKartState() == KartState.Lakitu)
				continue;
			
			if (kart.HasCondition(ConditionType.Ghost))
			{
				if (item instanceof RedShell)
					if (item.GetTarget() != null && item.GetTarget().equals(kart))
						return true;
	
				continue;
			}

			//Don't hit owner
			if (item.GetOwner() != null && kart.equals(item.GetOwner()))
			{
				if (item.GetHost() != null)
					continue;
				
				if (!UtilTime.elapsed(item.GetFireTime(), 1000))
					continue;
			}
				
			if (UtilMath.offset(kart.GetDriver(), item.GetEntity()) < item.GetRadius() && kart.GetDriver().getWorld() == item.GetEntity().getWorld())
			{
				item.CollideHandle(kart);
				return true;
			}
		}
		
		return false;
	}

	public static KartItemEntity CollideItem(KartItemEntity item, HashSet<KartItemEntity> allItems) 
	{
		if (item.GetEntity() == null)
			return null;
		
		for (KartItemEntity other : allItems)
		{
			if (item.equals(other))
				continue;
			
			//Both Arent Moving
			if (item.GetVelocity() == null && other.GetVelocity() == null)
				continue;
			
			//Both Arent Moving
			if ((item.GetVelocity() != null && item.GetVelocity().length() <= 0) && (item.GetVelocity() != null && item.GetVelocity().length() <= 0))
				continue;
			
			//Dont collide with friends on orbit!
			if (item.GetHost() != null && other.GetHost() != null)
				if (item.GetHost().equals(other.GetHost()))
					continue;
			
			//Don't collide with friends after shot (same owner)
			if (item.GetOwner() != null && other.GetOwner() != null && item.GetOwner().equals(other.GetOwner()))
			{
				//Item is trailing
				if (item.GetHost() != null && other.GetHost() == null)
				{
					if (!UtilTime.elapsed(other.GetFireTime(), 1000))
					{
						continue;
					}
				}
				//Other is trailing
				else if (item.GetHost() == null && other.GetHost() != null)
				{
					if (!UtilTime.elapsed(item.GetFireTime(), 1000))
					{
						continue;
					}
				}
			}
			
			if (UtilMath.offset(other.GetEntity(), item.GetEntity()) < item.GetRadius())
			{
				return other;
			}
		}
		
		return null;
	}
}
