package mineplex.core.common.util;

import org.bukkit.World;

public class WorldLoadInfo
{
	private World _world;
	private int _minChunkX;
	private int _minChunkZ;
	private int _maxChunkX;
	private int _maxChunkZ;
	
	public int CurrentChunkX;
	public int CurrentChunkZ;
	
	public WorldLoadInfo(World world, int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ)
	{
		_world = world;
		_minChunkX = minChunkX;
		_minChunkZ = minChunkZ;
		_maxChunkX = maxChunkX;
		_maxChunkZ = maxChunkZ;
		
		CurrentChunkX = minChunkX;
		CurrentChunkZ = minChunkZ;
	}
	
	public World GetWorld()
	{
		return _world;
	}
	
	public int GetMinChunkX()
	{
		return _minChunkX;
	}
	
	public int GetMinChunkZ()
	{
		return _minChunkZ;
	}
	
	public int GetMaxChunkX()
	{
		return _maxChunkX;
	}
	
	public int GetMaxChunkZ()
	{
		return _maxChunkZ;
	}
}
