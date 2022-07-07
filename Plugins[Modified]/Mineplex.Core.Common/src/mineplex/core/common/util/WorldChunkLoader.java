package mineplex.core.common.util;

import java.util.Iterator;


import org.bukkit.Bukkit;

public class WorldChunkLoader implements Runnable
{
	private static WorldChunkLoader _worldChunkLoader = null;
	
	private NautHashMap<WorldLoadInfo, Runnable> _worldRunnableMap = new NautHashMap<WorldLoadInfo, Runnable>();
	
	private long _loadPassStart;
	private long _maxPassTime = 25;
	
	private WorldChunkLoader()
	{
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Bukkit.getPluginManager().getPlugins()[0], this, 0, 1L);
	}
	
	public static void AddWorld(WorldLoadInfo worldInfo, Runnable runnable)
	{
		if (_worldChunkLoader == null)
		{
			_worldChunkLoader = new WorldChunkLoader();
		}
		
		_worldChunkLoader._worldRunnableMap.put(worldInfo, runnable);
	}

	@Override
	public void run()
	{
		_loadPassStart = System.currentTimeMillis();
		
		Iterator<WorldLoadInfo> worldInfoIterator = _worldRunnableMap.keySet().iterator();
		
		while (worldInfoIterator.hasNext())
		{
			WorldLoadInfo worldInfo = worldInfoIterator.next();
			System.out.println("Loading chunks for " + worldInfo.GetWorld().getName());
			
			while (worldInfo.CurrentChunkX <= worldInfo.GetMaxChunkX())
	        {
				while (worldInfo.CurrentChunkZ <= worldInfo.GetMaxChunkZ()) 
	            {
	    			if (System.currentTimeMillis() - _loadPassStart >= _maxPassTime)
	    				return;
	                
	    			worldInfo.GetWorld().loadChunk(worldInfo.CurrentChunkX, worldInfo.CurrentChunkZ);
	    			worldInfo.CurrentChunkZ++;
	            }
	            
	            worldInfo.CurrentChunkZ = worldInfo.GetMinChunkZ();
	            worldInfo.CurrentChunkX++;
	        }
			
			_worldRunnableMap.get(worldInfo).run();
			worldInfoIterator.remove();
		}
	}
}
