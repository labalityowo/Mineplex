package nautilus.game.arcade.managers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.world.WorldData;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class GameWorldManager implements Listener
{

	final ArcadeManager Manager;
	 
	private final Set<WorldData> _worldLoader = new HashSet<>();
	
	public GameWorldManager(ArcadeManager manager)
	{
		Manager = manager;
		
		Manager.registerEvents(this);
	}

	@EventHandler
	public void LoadWorldChunks(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
 
		Iterator<WorldData> worldIterator = _worldLoader.iterator();

		long endTime = System.currentTimeMillis() + 25;

		while (worldIterator.hasNext())
		{	
			long timeLeft = endTime - System.currentTimeMillis();
			if (timeLeft <= 0)	break;

			final WorldData worldData = worldIterator.next();

			if (worldData.World == null)
			{
				worldIterator.remove();
			}
			else if (worldData.Host.loadNecessaryChunks(timeLeft))
			{
				worldData.Host.prepareToRecruit();
				worldIterator.remove();
			}
		}
	}
	
	@EventHandler
	public void ChunkUnload(ChunkUnloadEvent event)
	{
		if (event.getWorld().getName().equals("world"))
		{
			event.setCancelled(true);
			return;
		}

		if (Manager.GetGame() != null)
		{
			if (Manager.GetGame().WorldData != null)
			{

				if (Manager.GetGame().WorldChunkUnload)
				{
					return;
				}
				if (Manager.GetGame().WorldData.World == null)
					return;

				if (!event.getWorld().equals(Manager.GetGame().WorldData.World))
					return;

				event.setCancelled(true);
			}
		}
	}

	public void RegisterWorld(WorldData worldData)
	{
		_worldLoader.add(worldData);
	}
}
