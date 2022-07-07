package nautilus.game.arcade.kit.perks.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PerkLeapEvent extends Event 
{
    private static final HandlerList handlers = new HandlerList();
    private Player _player;
    
    public PerkLeapEvent(Player player)
    {
       _player = player;
    }
 
    public HandlerList getHandlers()
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
        
    public Player GetPlayer()
    {
    	return _player;
    }
}
