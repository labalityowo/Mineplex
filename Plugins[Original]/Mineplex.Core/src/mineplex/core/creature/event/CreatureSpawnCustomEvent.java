package mineplex.core.creature.event;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class CreatureSpawnCustomEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private boolean _cancelled = false;
 
    private Location _location;
    private SpawnReason _reason;
    
    public CreatureSpawnCustomEvent(Location location, SpawnReason reason)
    {
    	_location = location;
    	_reason = reason;
    }
    
    public HandlerList getHandlers()
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    @Override
    public boolean isCancelled()
    {
        return _cancelled;
    }

    @Override
    public void setCancelled(boolean cancel)
    {
        _cancelled = cancel;
    }

	public Location GetLocation()
	{
		return _location;
	}
	
	public SpawnReason getReason()
	{
		return _reason;
	}
}
