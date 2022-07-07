package nautilus.game.pvp.modules;

import java.util.List;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.NautHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TreeRemover implements Runnable, Listener
{
	private JavaPlugin _plugin;
	private World _world;
	private boolean _stopDecay;
	private int _taskId;
	private int _x = -600;
	private int _z = -600;
	
	public boolean _clearedLogs = false;
	
	public NautHashMap<Integer, NautHashMap<Integer, NautHashMap<Integer, Boolean>>> _tempLeafMap = new NautHashMap<Integer, NautHashMap<Integer, NautHashMap<Integer, Boolean>>>();
	
	public TreeRemover(JavaPlugin plugin)
	{
		_plugin = plugin;
		_world = Bukkit.getWorlds().get(0);
		
		_plugin.getServer().getPluginManager().registerEvents(this, _plugin);		
	}
	
	public void run()
	{
		if (!_clearedLogs)
		{
			_clearedLogs = ClearArea();
			
			if (_clearedLogs)
			{
				_x = -600;
				_z = -600;
				_stopDecay = false;
			}
		}
		else if (PlaceNewTrees())
		{
			List<Entity> entities = _world.getEntities();
			
			for (Entity entity : entities)
			{
				if (entity instanceof Item)
				{
					entity.remove();
				}
			}
			
			_plugin.getServer().getScheduler().cancelTask(_taskId);
		}
	}
	
	private boolean ClearArea()
	{
		for (; _x <= 600;)
		{			
			for (; _z <= 600;)
			{
				if (!_world.isChunkLoaded(_x >> 4, _z >> 4))
					_world.loadChunk(_x >> 4, _z >> 4);
				
				for (int y = 50; y <= 100; y++)
				{
					Block block = _world.getBlockAt(_x,  y,  _z);
					
					if (block.getType() == Material.LOG || block.getType() == Material.LEAVES)
					{
						MapUtil.QuickChangeBlockAt(_world, _x, y, _z, Material.AIR);
					}
				}
				
				_z++;
				
				if (_z % (GetHeaviness() * 10) == 0)
					return false;
			}
			
			_z = -600;
			_x++;
			
			if (_x % 60 == 0)
				System.out.println("Removing trees progress : " + (((_x / 60) + 10) * 5) + "% done.");
		}
		
		if (_x >= 600)
		{
			return true;
		}

		return false;
	}
	
	private boolean PlaceNewTrees()
	{
		for (; _x <= 600;)
		{			
			for (; _z <= 600;)
			{
				if (!_world.isChunkLoaded(_x >> 4, _z >> 4))
					_world.loadChunk(_x >> 4, _z >> 4);
				
				Block previousBlock = null;
				
				for (int y = 100; y >= 50; y--)
				{
					Block block = _world.getBlockAt(_x,  y,  _z);

					if (block.getType().isSolid() && previousBlock != null && previousBlock.getType().isTransparent())
					{
						if (_x % GetHeaviness() == 0 && _z % GetHeaviness() == 0 && Math.random() > .4)
						{
							_world.generateTree(previousBlock.getLocation().add((Math.random() >= .5 ? -1 : 1) * Math.random() * 5, 0.0, (Math.random() >= .5 ? -1 : 1) * Math.random() * 5), Math.random() > .25 ? TreeType.TREE : TreeType.BIG_TREE);
							break;
						}
					}
					
					if (block.isLiquid())
						break;
					
					previousBlock = block;
				}
				
				_z++;
				
				if (_z % (GetHeaviness() * 10) == 0)
					return false;
			}
			
			_z = -600;
			_x++;
			
			if (_x % 60 == 0)
				System.out.println("Placing trees progress : " + (((_x / 60) + 10) * 5) + "% done.");
		}
		
		if (_x >= 600)
		{
			return true;
		}

		return false;
	}

	@EventHandler
	public void OnPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().equals("/removetrees") && event.getPlayer().isOp())
		{
			_plugin.getServer().getScheduler().cancelTask(_taskId);
			_taskId = _plugin.getServer().getScheduler().scheduleSyncRepeatingTask(_plugin, this, 0L, 1L);
			_stopDecay = true;
		}
		else if (event.getMessage().equals("/placetrees") && event.getPlayer().isOp())
		{
			_plugin.getServer().getScheduler().cancelTask(_taskId);
			_taskId = _plugin.getServer().getScheduler().scheduleSyncRepeatingTask(_plugin, this, 0L, 1L);
			_clearedLogs = true;
		}
	}
	
	@EventHandler
	public void LeavesDecay(LeavesDecayEvent event)
	{
		if (_stopDecay)
			event.setCancelled(true);
	}
	
	private int GetHeaviness()
	{
		return (_x >= -400 && _z >= -400 && _x <= 400 && _z <= 400) ? 10 : 14;
	}
}
