package nautilus.game.minekart.track;

import nautilus.game.minekart.kart.Kart;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class TrackItem 
{
	private Location _loc;
	private Entity _ent = null;
	private long _delay = 0;
	
	public TrackItem(Location loc)
	{
		_loc = loc;
	}
	
	public Location GetLocation()
	{
		return _loc;
	}
	
	public Entity GetEntity()
	{
		return _ent;
	}
	
	public long GetDelay()
	{
		return _delay;
	}
	
	public void SetEntity(Entity ent)
	{
		_ent = ent;
	}
	
	public void SetDelay(long delay)
	{
		_delay = delay;
	}

	public void SpawnEntity(World world) 
	{
		if (GetEntity() != null)
			GetEntity().remove();
		
		SetEntity(world.spawnEntity(_loc.clone().add(0, 0, 0), EntityType.ENDER_CRYSTAL));
		
		//SetEntity(world.dropItem(new Location(_loc.getWorld(), _loc.getX(), _loc.getY() + 1, _loc.getZ()), new ItemStack(Material.CHEST)));
		//SetEntity(world.spawnFallingBlock(GetLocation(), Material.LOCKED_CHEST, (byte)0));
		//GetEntity().setVelocity(new Vector(0,0,0));
	}

	public void Pickup(Kart kart) 
	{
		if (GetEntity() != null)
			GetEntity().remove();
		
		SetEntity(null);
		SetDelay(System.currentTimeMillis());
		
		GetLocation().getWorld().playEffect(GetLocation().clone().add(0, 1, 0), Effect.STEP_SOUND, 35);
		
		kart.PickupItem();
	}
}
