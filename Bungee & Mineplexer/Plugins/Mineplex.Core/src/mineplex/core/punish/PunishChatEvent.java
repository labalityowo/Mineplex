package mineplex.core.punish;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PunishChatEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private boolean _cancelled = false;
    private Player _player;
    
    public PunishChatEvent(Player player)
    {
    	_player = player;
    }
    
    public Player GetPlayer()
    {
    	return _player;
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