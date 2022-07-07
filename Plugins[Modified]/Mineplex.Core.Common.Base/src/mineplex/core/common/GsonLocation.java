package mineplex.core.common;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class GsonLocation
{
	private String _world;
	private double _posX;
	private double _posY;
	private double _posZ;
	private float _yaw;
	private float _pitch;
	
	public GsonLocation(Location location)
	{
		_world = location.getWorld().getName();
		_posX = location.getX();
		_posY = location.getY();
		_posZ = location.getZ();
		_yaw = location.getYaw();
		_pitch = location.getPitch();
	}
	
	public GsonLocation(String world, double x, double y, double z)
	{
		this(Bukkit.getWorld(world), x, y, z, .0f, .0f);
	}
	
	public GsonLocation(String world, double x, double y, double z, float yaw, float pitch)
	{
		this(Bukkit.getWorld(world), x, y, z, yaw, pitch);
	}
	
	public GsonLocation(World world, double x, double y, double z, float yaw, float pitch)
	{
		_world = world.getName();
		_posX = x;
		_posY = y;
		_posZ = z;
		_yaw = yaw;
		_pitch = pitch;
	}
	
	public GsonLocation(double x, double y, double z)
	{
		this(x, y, z, .0f, .0f);
	}
	
	public GsonLocation(double x, double y, double z, float yaw, float pitch)
	{
		this("world", x, y, z, yaw, pitch);
	}
	
	public Location bukkit()
	{
		return new Location(Bukkit.getWorld(_world), _posX, _posY, _posZ);
	}
	
	public String getWorld()
	{
		return _world;
	}
	
	public double getX()
	{
		return _posX;
	}
	
	public double getY()
	{
		return _posY;
	}
	
	public double getZ()
	{
		return _posZ;
	}
}
