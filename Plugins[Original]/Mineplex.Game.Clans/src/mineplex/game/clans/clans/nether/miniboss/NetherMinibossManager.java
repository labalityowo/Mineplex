package mineplex.game.clans.clans.nether.miniboss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.google.common.collect.Lists;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.nether.NetherManager;
import mineplex.minecraft.game.classcombat.event.ClassCombatCreatureAllowSpawnEvent;

/**
 * Manager to handle miniboss spawning in the nether
 */
public class NetherMinibossManager implements Listener
{
	private static final long MINIBOSS_SPAWN_RATE = 10000;
	private NetherManager _manager;
	private Map<Location, NetherMinibossType> _spawns = new HashMap<>();
	private long _lastSpawned;
	private boolean _allowSpawn = false;
	private boolean _allowSpawnEvent = false;
	
	public NetherMinibossManager(NetherManager manager)
	{
		_manager = manager;
		new MinibossFireball();
		manager.runSyncLater(() ->
		{
			List<Location> sort = Lists.newArrayList();
			sort.add(new Location(manager.getNetherWorld(), -18, 142, 61));
			sort.add(new Location(manager.getNetherWorld(), -39, 133, -25));
			sort.add(new Location(manager.getNetherWorld(), -102, 133, -99));
			sort.add(new Location(manager.getNetherWorld(), -27, 141, -140));
			sort.add(new Location(manager.getNetherWorld(), 32, 143, -95));
			sort.add(new Location(manager.getNetherWorld(), 43, 134, 22));
			sort.add(new Location(manager.getNetherWorld(), 102, 141, -31));
			sort.add(new Location(manager.getNetherWorld(), 151, 136, 34));
			sort.sort((o1, o2) ->
			{
				if (UtilMath.offset2d(o1, manager.getNetherWorld().getSpawnLocation()) < UtilMath.offset(o2, manager.getNetherWorld().getSpawnLocation()))
				{
					return -1;
				}
				return 1;
			});
			for (int i = 0; i < 3; i++)
			{
				_spawns.put(sort.get(i).add(0.5, 1.5, 0.5), NetherMinibossType.ARCHER);
			}
			for (int i = 3; i < 6; i++)
			{
				_spawns.put(sort.get(i).add(0.5, 1.5, 0.5), NetherMinibossType.WARRIOR);
			}
			for (int i = 6; i < 8; i++)
			{
				_spawns.put(sort.get(i).add(0.5, 1.5, 0.5), NetherMinibossType.GHAST);
			}
			Bukkit.getPluginManager().registerEvents(this, manager.getPlugin());
		}, 20L);
	}
	
	private void spawnAttacker(Location loc)
	{
		NetherMinibossType bossType = _spawns.get(loc);
		
		_allowSpawn = true;
		bossType.getNewInstance(loc);
		_allowSpawn = false;
	}
	
	@EventHandler
	public void onAllowSpawn(ClassCombatCreatureAllowSpawnEvent event)
	{
		if (event.getWorldName().equalsIgnoreCase(_manager.getNetherWorld().getName()))
		{
			_allowSpawnEvent = event.getAllowed();
		}
	}
	
	@EventHandler
	public void onSpawnNormal(EntitySpawnEvent event)
	{
		if (event.getEntity() instanceof LivingEntity && _manager.getNetherWorld().equals(event.getLocation().getWorld()))
		{
			if (!_allowSpawn && !_allowSpawnEvent)
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onUnload(ChunkUnloadEvent event)
	{
		if (_manager.getNetherWorld().equals(event.getWorld()))
		{
			if (!_manager.InNether.isEmpty())
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onSpawnThreat(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		
		if (!_manager.InNether.isEmpty() && UtilTime.elapsed(_lastSpawned, MINIBOSS_SPAWN_RATE))
		{
			_lastSpawned = System.currentTimeMillis();
			for (Location spawn : _spawns.keySet())
			{
				spawnAttacker(spawn);
			}
		}
	}
}