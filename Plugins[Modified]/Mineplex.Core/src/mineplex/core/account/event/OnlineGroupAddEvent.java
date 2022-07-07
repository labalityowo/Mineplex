package mineplex.core.account.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.account.permissions.PermissionGroup;

public class OnlineGroupAddEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	private PermissionGroup _group;
	
    public OnlineGroupAddEvent(Player player, PermissionGroup group)
    {
    	_player = player;
    	_group = group;
    }
    
    public Player getPlayer()
    {
    	return _player;
    }
    
    public PermissionGroup getGroup()
    {
    	return _group;
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