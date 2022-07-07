package nautilus.game.pvp.worldevent;

import java.util.HashSet;

import mineplex.core.MiniPlugin;
import mineplex.core.common.Rank;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import nautilus.game.pvp.worldevent.command.EventCommand;
import nautilus.game.pvp.worldevent.events.BossSkeleton;
import nautilus.game.pvp.worldevent.events.BossSlime;
import nautilus.game.pvp.worldevent.events.BaseUndead;
import nautilus.game.pvp.worldevent.events.BossSpider;
import nautilus.game.pvp.worldevent.events.BossSwarmer;
import nautilus.game.pvp.worldevent.events.BossWither;
import nautilus.game.pvp.worldevent.events.EndFlood;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class EventManager extends MiniPlugin
{
	private HashSet<EventBase> _active = new HashSet<EventBase>();
	
	private EventTerrainFinder _terrainFinder;
	
	private long _lastStart = 0;
	private long _lastStop = 0;
	
	public EventManager(JavaPlugin plugin) 
	{
		super("Event Manager", plugin);
		
		_terrainFinder = new EventTerrainFinder(this);
		
		_lastStart = System.currentTimeMillis();
		_lastStop = System.currentTimeMillis();
	}
	
	@Override
	public void AddCommands() 
	{
		addCommand(new EventCommand(this));
	}

	@EventHandler
	public void StartEvent(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;
		
		if (!_active.isEmpty())
			return;
		
		if (System.currentTimeMillis() - _lastStart < 7200000)
			return;
		
		if (System.currentTimeMillis() - _lastStop < 7200000)
			return;
		
		StartEvent();
	}
	
	public void StartEvent()
	{
		double rand = Math.random();
		
		if (rand > 0.90)			(new BaseUndead(this)).TriggerStart();
		else if (rand > 0.72)		(new BossSlime(this)).TriggerStart();
		else if (rand > 0.54)		(new BossSkeleton(this)).TriggerStart();	
		else if (rand > 0.36)		(new BossSwarmer(this)).TriggerStart();	
		else if (rand > 0.18)		(new BossWither(this)).TriggerStart();	
		else						(new BossSpider(this)).TriggerStart();

	}

	public EventTerrainFinder TerrainFinder() 
	{
		return _terrainFinder;
	}

	public void RecordStart(EventBase event)
	{
		_active.add(event);
		
		//Register Events
		UtilServer.getServer().getPluginManager().registerEvents(event, GetPlugin());
		
		_lastStart = System.currentTimeMillis();
	}

	public void RecordStop(EventBase event) 
	{
		_active.remove(event);
		
		//Deregister Events
		HandlerList.unregisterAll(event);
		
		_lastStop = System.currentTimeMillis();
	}

	public void addEvent(EventBase event)
	{
		_active.add(event);
	}
}
