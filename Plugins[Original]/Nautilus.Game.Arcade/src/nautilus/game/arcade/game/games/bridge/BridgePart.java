package nautilus.game.arcade.game.games.bridge;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;

public class BridgePart 
{
	public FallingBlock Entity;  
	public Location Target; 
	public Location Initial;
	
	public boolean Velocity;
	
	public BridgePart(FallingBlock entity, Location target, boolean velocity)
	{
		Entity = entity;
		Target = target;
		Initial = entity.getLocation();
		Velocity = velocity;
	}
	 
	@SuppressWarnings("deprecation")
	public boolean Update()
	{
		if (!Entity.isValid())
		{
			MapUtil.QuickChangeBlockAt(Target, Entity.getBlockId(), Entity.getBlockData());
			return true;
		}
		 
		//Form
		if (UtilMath.offset(Entity.getLocation(), Target) < 1 || Entity.getTicksLived() > 600 || Entity.getLocation().getY() < Target.getY())
		{
			MapUtil.QuickChangeBlockAt(Target, Entity.getBlockId(), Entity.getBlockData());
			
			Entity.remove();
		
			Target.getBlock().getWorld().playEffect(Target, Effect.STEP_SOUND, Target.getBlock().getTypeId());
			
			return true;
		}
		
		if (!Velocity)
			return false;
	
		//Perfect Align
		if (UtilMath.offset2d(Entity.getLocation(), Target) < 0.1)
		{
			Location loc = Entity.getLocation();
			loc.setX(Target.getX());
			loc.setZ(Target.getZ());
		}
		
		
		//Velocity
		Vector dir = UtilAlg.getTrajectory(Entity.getLocation(), Target);
		dir.add(new Vector(0, 0.6, 0));
		dir.normalize();
		dir.multiply(0.8);
		
		if (UtilMath.offset(Entity.getLocation(), Initial) < UtilMath.offset(Entity.getLocation(), Target))
			dir.add(new Vector(0, 0.6, 0));
		
		Entity.setVelocity(dir);
		
		return false;
	}

	public boolean ItemSpawn(Item item) 
	{
		if (UtilMath.offset(Entity, item) < 1)
		{
			return true;
		}
		
		return false;
	}
}
