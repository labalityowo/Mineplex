package mineplex.game.clans.clans.worldevent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.blood.Blood;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardManager;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.elements.ScoreboardElement;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.bosstoken.BossTokenShop;
import mineplex.game.clans.clans.loot.LootManager;
import mineplex.game.clans.clans.regions.ClansRegions;
import mineplex.game.clans.clans.worldevent.api.EventState;
import mineplex.game.clans.clans.worldevent.api.WorldEvent;
import mineplex.game.clans.clans.worldevent.boss.BossArenaLocationFinder;
import mineplex.game.clans.clans.worldevent.command.WorldEventCommand;
import mineplex.game.clans.clans.worldevent.raid.RaidManager;
import mineplex.minecraft.game.core.damage.DamageManager;

public class WorldEventManager extends MiniPlugin implements ScoreboardElement
{
	public enum Perm implements Permission
	{
		WORLD_EVENT_COMMAND,
		START_EVENT_COMMAND,
		STOP_EVENT_COMMAND,
	}

	private final List<WorldEvent> _runningEvents;
	
	private Random _random;
	private ClansManager _clansManager;
	private EventTerrainFinder _terrainFinder;
	private BossArenaLocationFinder _bossFinder;
	private DamageManager _damageManager;
	private LootManager _lootManager;
	private BlockRestore _blockRestore;
	
	private long _nextEventStart;
	
	private RaidManager _raidManager;
	
	private BossTokenShop _shop;
	
	public WorldEventManager(JavaPlugin plugin, ClansManager clansManager, DamageManager damageManager, LootManager lootManager, BlockRestore blockRestore, ClansRegions clansRegions)
	{
		super("World Event", plugin);
		
		_random = new Random();
		_terrainFinder = new EventTerrainFinder(clansManager);
		_bossFinder = new BossArenaLocationFinder(UtilWorld.getWorld("world"));
		_clansManager = clansManager;
		_damageManager = damageManager;
		_lootManager = lootManager;
		_blockRestore = blockRestore;
		_runningEvents = new LinkedList<>();
		
		_shop = new BossTokenShop(this);
		
		new Blood(plugin);
		
		_raidManager = new RaidManager(plugin);
		
		updateNextEventTime();
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.WORLD_EVENT_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.START_EVENT_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.STOP_EVENT_COMMAND, true, true);
	}
	
	public void addCommands()
	{
		addCommand(new WorldEventCommand(this));
	}
	
	@Override
	public void disable()
	{
		for (WorldEvent event : _runningEvents)
		{
			event.stop(true);
		}
		_runningEvents.clear();
		_raidManager.onDisable();
	}
	
	public boolean isInEvent(Location location, boolean icePrisonCheck)
	{
		for (WorldEvent event : _runningEvents)
		{
			if (event.isInBounds(location, true))
			{
				if (!icePrisonCheck || !event.allowsIcePrison())
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public BossTokenShop getShop()
	{
		return _shop;
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		boolean removed = _runningEvents.removeIf(e -> e.getState() == EventState.STOPPED);
		
		if (removed && _runningEvents.size() == 0)
		{
			updateNextEventTime();
		}

		if (_runningEvents.size() == 0 && System.currentTimeMillis() >= _nextEventStart)
		{
			if (UtilServer.getPlayers().length > 0)
			{
				tryStartEvent();
			}
			else
			{
				updateNextEventTime();
			}
		}
	}
	
	public WorldEventType randomEvent()
	{
		if (_runningEvents.size() == 0)
		{
			if (UtilServer.getPlayers().length > 0)
			{
				return tryStartEvent();
			}
		}
		return null;
	}
	
	private WorldEventType tryStartEvent()
	{
		WorldEventType[] types = WorldEventType.values();
		if (types.length == 0)
		{
			return null;
		}
		else
		{
			WorldEventType type = types[_random.nextInt(types.length)];
			//Location location = _terrainFinder.findAreaInBorderlands(false);
			//if (location != null)
			{
				initializeEvent(type.createInstance(this));
				return type;
			}
			//else
			//{
				// Try again in 5 minutes
				//_nextEventStart = System.currentTimeMillis() + (5 * 60 * 1000);
			//}
		}
	}
	
	private void initializeEvent(WorldEvent event)
	{
		if (event == null)
		{
			throw new RuntimeException("WorldEvent may not be null");
		}
		
		event.start();
		_runningEvents.add(event);
	}
	
	public WorldEvent startEventFromName(String name)
	{
		WorldEventType eventType = WorldEventType.valueOf(name);
		if (eventType != null)
		{
			WorldEvent event = eventType.createInstance(this);
			if (event == null)
			{
				return null;
			}
			initializeEvent(event);
			return event;
		}
		
		return null;
	}
	
	public WorldEvent startEventFromType(WorldEventType eventType)
	{
		if (eventType != null)
		{
			//Location location = _terrainFinder.findAreaInBorderlands(true);
			WorldEvent event = eventType.createInstance(this);
			if (event != null)
			{
				initializeEvent(event);
			}
			return event;
		}
		
		return null;
	}
	
	public void clearEvents()
	{
		Iterator<WorldEvent> iterator = _runningEvents.iterator();
		while (iterator.hasNext())
		{
			WorldEvent event = iterator.next();
			event.stop(true);
			iterator.remove();
		}
		
		updateNextEventTime();
	}
	
	public ClansManager getClans()
	{
		return _clansManager;
	}
	
	public DamageManager getDamage()
	{
		return _damageManager;
	}
	
	public LootManager getLoot()
	{
		return _lootManager;
	}
	
	public EventTerrainFinder getTerrainFinder()
	{
		return _terrainFinder;
	}
	
	public BossArenaLocationFinder getBossArenaLocationFinder()
	{
		return _bossFinder;
	}
	
	public RaidManager getRaidManager()
	{
		return _raidManager;
	}
	
	private void updateNextEventTime()
	{
		// 45 Minutes + (0 - 15 Minutes)
		long waitTime = (45 * 60 * 1000) + _random.nextInt(15 * 60 * 1000);
		_nextEventStart = System.currentTimeMillis() + waitTime;
	}
	
	@Override
	public List<String> getLines(ScoreboardManager manager, Player player, List<String> out)
	{
		List<String> output = Lists.newArrayList();
		
		Iterator<WorldEvent> iterator = _runningEvents.iterator();
		while (iterator.hasNext())
		{
			WorldEvent event = iterator.next();
			
			if (event.getState() == EventState.LIVE)
			{
				output.add("    ");
				output.add(C.cAqua + C.Bold + "Event");
				
				Location eventLocation = event.getCenterLocation();
				String locationString = eventLocation.getBlockX() + ", " + eventLocation.getBlockY() + ", " + eventLocation.getBlockZ();
				output.add("  " + C.cWhite + event.getName());
				output.add("  " + C.cWhite + locationString);
				
				List<String> scoreboardLines = event.getLines(manager, player, out);
				if (scoreboardLines != null)
				{
					output.addAll(scoreboardLines);
				}
				
				break;
			}
		}
		
		return output;
	}
	
	public BlockRestore getBlockRestore()
	{
		return _blockRestore;
	}
	
	public List<WorldEvent> getEvents()
	{
		return _runningEvents;
	}

	public DisguiseManager getDisguiseManager()
	{
		return getClans().getDisguiseManager();
	}
}