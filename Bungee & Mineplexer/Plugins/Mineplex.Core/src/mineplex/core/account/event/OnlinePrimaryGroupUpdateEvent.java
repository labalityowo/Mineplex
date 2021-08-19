package mineplex.core.account.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.account.permissions.PermissionGroup;

public class OnlinePrimaryGroupUpdateEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	private PermissionGroup _from, _to;
	
    public OnlinePrimaryGroupUpdateEvent(Player player, PermissionGroup from, PermissionGroup to)
    {
    	_player = player;
    	_from = from;
    	_to = to;
    }
    
    public Player getPlayer()
    {
    	return _player;
    }
    
    public PermissionGroup getFrom()
    {
    	return _from;
    }
    
    public PermissionGroup getTo()
    {
    	return _to;
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