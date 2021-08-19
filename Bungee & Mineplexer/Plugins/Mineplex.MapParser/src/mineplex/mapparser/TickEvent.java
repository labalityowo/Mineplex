package mineplex.mapparser;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
 
public class TickEvent extends Event 
{
	private static final HandlerList handlers = new HandlerList();
	
    public HandlerList getHandlers() 
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList() 
    {
        return handlers;
    }
}