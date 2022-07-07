package mineplex.gemhunters.loot;

import org.bukkit.Location;

public class SpawnedChest
{

	private Location _location;
	private ChestProperties _properties;
	private int _id;
	private long _spawnedAt;
	
	private long _openedAt;
	
	public SpawnedChest(Location location, ChestProperties properties, int id)
	{
		_location = location;
		_properties =properties;
		_id = id;
		_spawnedAt = System.currentTimeMillis();
		_openedAt = 0;
	}
	
	public void setOpened()
	{
		_openedAt = System.currentTimeMillis();
	}
	
	public Location getLocation()
	{
		return _location;
	}
	
	public ChestProperties getProperties()
	{
		return _properties;
	}
	
	public int getID()
	{
		return _id;
	}
	
	public long getSpawnedAt()
	{
		return _spawnedAt;
	}
	
	public long getOpenedAt()
	{
		return _openedAt;
	}
	
	public boolean isOpened()
	{
		return _openedAt != 0;
	}
	
}
