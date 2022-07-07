package nautilus.game.minekart.track;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import nautilus.game.minekart.kart.Kart;
import net.minecraft.server.v1_7_R1.EntityCreature;
import net.minecraft.server.v1_7_R1.Navigation;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftCreature;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public abstract class TrackEntity 
{
	public Track Track;
	
	private String _name;

	private Entity _ent;
	private EntityType _type;

	private Location _loc;
	private double _offset = 3;
	private double _collideRange = 1;
	
	private long _spawnRate = 30000;
	private long _spawnTimer = 0;
	
	public TrackEntity(Track track, EntityType type, String name, double offset, double collideRange, long spawnRate, Location loc)
	{
		Track = track;
		
		_name = name;
		
		_type = type;
		
		_spawnRate = spawnRate;
		
		_offset = offset;
		_collideRange = collideRange;
		
		_loc = loc;
	}
	
	public String GetName()
	{
		return _name;
	}
	
	public Entity GetEntity()
	{
		return _ent;
	}
	
	public void SetEntity(Entity ent)
	{
		_ent = ent;
	}
	
	public EntityType GetType()
	{
		return _type;
	}
	
	public Location GetLocation()
	{
		return _loc;
	}
	
	public long GetSpawnRate()
	{
		return _spawnRate;
	}
	
	public long GetSpawnTimer()
	{
		return _spawnTimer;
	}
	
	public void SetSpawnTimer(long time)
	{
		_spawnTimer = time;
	}
	
	public double GetOffset()
	{
		return _offset;
	}
	
	public double GetCollideRange()
	{
		return _collideRange;
	}
	
	public boolean Update()
	{
		//Respawn
		if (GetEntity() == null || !GetEntity().isValid())
		{
			Respawn();
		}
		//Return
		else 
		{
			Movement();
		}
		
		return false;
	}
	
	public void Respawn()
	{
		if (GetType() == null)
			return;
		
		if (GetEntity() != null)
			GetEntity().remove();
		
		if (UtilTime.elapsed(GetSpawnTimer(), GetSpawnRate()))
		{	
			_ent = GetLocation().getWorld().spawnEntity(GetLocation(), GetType());
			SetSpawnTimer(System.currentTimeMillis());
		}
	}

	public void Movement()
	{
		if (UtilMath.offset(GetLocation(), GetEntity().getLocation()) > GetOffset())
		{
			if (GetEntity() instanceof Creature)
			{
				EntityCreature ec = ((CraftCreature)GetEntity()).getHandle();
				Navigation nav = ec.getNavigation();
				nav.a(GetLocation().getX(), GetLocation().getY(), GetLocation().getZ(), 0.4f);
			}
		}
	}
	
	public void CheckCollision(Kart kart) 
	{
		if (GetEntity() == null || !GetEntity().isValid())
			return;
		
		if (UtilMath.offset(kart.GetDriver().getLocation(), GetEntity().getLocation()) > GetCollideRange())
			return;
		
		Collide(kart);
	}
	
	public abstract void Collide(Kart kart);
}
