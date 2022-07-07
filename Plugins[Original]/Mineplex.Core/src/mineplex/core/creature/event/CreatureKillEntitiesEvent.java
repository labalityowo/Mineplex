package mineplex.core.creature.event;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CreatureKillEntitiesEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private boolean _cancelled = false;
 
    private List<Entity> _entities;
    
    public CreatureKillEntitiesEvent(List<Entity> entities)
    {
    	_entities = entities;
    }
    
    public List<Entity> GetEntities()
    {
    	return _entities;
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
}
