package mineplex.game.clans.clans.worldevent.undead;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.World;

public enum UndeadCityLocation
{
	CITY(92, 68, 1181, 30, 45)
	;
	
	private static UndeadCityLocation _lastLocation;
	private final double _x, _y, _z;
	private final int _maxChests, _maxMobs;
	
	private UndeadCityLocation(double x, double y, double z, int maxChests, int maxMobs)
	{
		_x = x;
		_y = y;
		_z = z;
		_maxChests = maxChests;
		_maxMobs = maxMobs;
	}
	
	public Location toLocation(World world)
	{
		return new Location(world, _x, _y, _z);
	}
	
	public int getMaxChests()
	{
		return _maxChests;
	}
	
	public int getMaxMobs()
	{
		return _maxMobs;
	}
	
	public static UndeadCityLocation getRandomLocation()
	{
		return _lastLocation = UndeadCityLocation.values()[ThreadLocalRandom.current().nextInt(UndeadCityLocation.values().length)];
	}
	
	public static UndeadCityLocation getLastLocation()
	{
		return _lastLocation;
	}
}