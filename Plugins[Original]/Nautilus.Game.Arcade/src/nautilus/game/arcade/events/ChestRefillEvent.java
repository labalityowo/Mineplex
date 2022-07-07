package nautilus.game.arcade.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChestRefillEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();

    private final List<Location> _chests;
    
    public ChestRefillEvent(List<Location> chests)
	{
    	_chests = new ArrayList<>(chests);
	}
    
    public List<Location> getChests()
    {
    	return _chests;
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
