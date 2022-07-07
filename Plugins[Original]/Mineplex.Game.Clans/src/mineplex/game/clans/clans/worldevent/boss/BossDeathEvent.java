package mineplex.game.clans.clans.worldevent.boss;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BossDeathEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private final BossWorldEvent<?> _event;
	private final Location _deathLocation;
	
	public BossDeathEvent(BossWorldEvent<?> event, Location location)
	{
		_event = event;
		_deathLocation = location;
	}
	
	public BossWorldEvent<?> getEvent()
	{
		return _event;
	}
	
	public Location getDeathLocation()
	{
		return _deathLocation;
	}
	
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}