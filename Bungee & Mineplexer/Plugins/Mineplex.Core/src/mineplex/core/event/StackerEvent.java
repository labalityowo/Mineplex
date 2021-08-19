package mineplex.core.event;


import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class StackerEvent extends Event 
{
    private static final HandlerList handlers = new HandlerList();
    
    private Entity _entity;
    
    private boolean _cancelled = false;
    
    public StackerEvent(Entity entity) 
    {
    	_entity = entity;
    }
  
    public HandlerList getHandlers() 
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList() 
    {
        return handlers;
    }

	public Entity getEntity()
	{
		return _entity;
	}

	public void setCancelled(boolean cancel)
	{
		_cancelled = cancel;
	}
	
	public boolean isCancelled()
	{
		return _cancelled;
	}
}