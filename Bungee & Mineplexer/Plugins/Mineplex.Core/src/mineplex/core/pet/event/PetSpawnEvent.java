package mineplex.core.pet.event;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PetSpawnEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private boolean _cancelled = false;
    
    private Player _player;
    private EntityType _entityType;
    private Location _location;
 
    public PetSpawnEvent(Player player, EntityType entityType, Location location)
    {
    	_player = player;
    	_entityType = entityType;
    	_location = location;
    }
    
    public Player getPlayer()
    {
    	return _player;
    }
    
    public EntityType getEntityType()
    {
    	return _entityType;
    }
    
    public Location getLocation()
    {
    	return _location;
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
