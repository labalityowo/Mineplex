package nautilus.game.minekart.item;

import nautilus.game.minekart.item.control.Movement;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.track.Track;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public abstract class KartItemEntity 
{
	public KartItemManager Manager;
	
	private Track _track;
	
	private Material _mat;
	private byte _data;
	
	private KartItemActive _host;
	
	private Entity _entity;
	private Vector _velocity;
	private long _fireTime;
	
	private Kart _owner;
	private Kart _target;
	
	private double _radius = 2;
	
	public KartItemEntity(KartItemManager manager, Kart owner, Location loc, Material mat, byte data)
	{
		Manager = manager;
		
		_owner = owner;
		_track = owner.GetGP().GetTrack();
		
		_mat = mat;
		_data = data;
		
		_host = null;
		
		Spawn(loc);
		
		manager.RegisterWorldItem(this);
	}

	@SuppressWarnings("deprecation")
	public void Spawn(Location loc)
	{
		SetEntity(loc.getWorld().dropItem(loc.add(0, 0.5, 0), new ItemStack(_mat, 1, (short)0, _data)));	
		SetFired();
	}
		
	public void SetEntity(Entity ent)
	{
		_entity = ent;
	}
	
	public Entity GetEntity()
	{
		return _entity;
	}
	
	public Material GetMaterial()
	{
		return _mat;
	}

	public void SetRadius(double rad)
	{
		_radius = rad;
	}
	
	public void SetVelocity(Vector vel) 
	{
		_velocity = vel;
	}
	
	public Vector GetVelocity()
	{
		return _velocity;
	}
	
	public Vector GetVelocityClone()
	{
		return new Vector(_velocity.getX(), _velocity.getY(), _velocity.getZ());
	}
	
	public Kart GetOwner()
	{
		return _owner;
	}
	
	public void SetTarget(Kart kart)
	{
		_target = kart;
	}
	
	public Kart GetTarget()
	{
		return _target;
	}
	
	public long GetFireTime()
	{
		return _fireTime;
	}
	
	public void SetFired()
	{
		_fireTime = System.currentTimeMillis();
	}
	
	public void SetFiredAdd(long time) 
	{
		_fireTime = System.currentTimeMillis() + time;
	}
	
	public KartItemActive GetHost()
	{
		return _host;
	}
	
	public void SetHost(KartItemActive host)
	{
		_host = host;
	}
	
	public double GetRadius() 
	{
		return _radius;
	}
	
	public abstract void CollideHandle(Kart kart);
	
	public boolean TickUpdate()
	{
		if (GetHost() != null)
			return false;
		
		Movement.Move(this);
		
		return false;
	}
	
	public void Clean() 
	{
		if (_entity == null)
			return;
		
		if (_entity.getPassenger() != null)
			_entity.getPassenger().remove();
		
		_entity.remove();
	}

	public Track GetTrack()
	{
		return _track;
	}
}
