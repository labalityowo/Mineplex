package nautilus.game.arcade.game.games.minecraftleague.tracker;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GrabSkullEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private Player _player;
	
	public GrabSkullEvent(Player player)
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
    
    public Player getPlayer()
    {
    	return _player;
    }
}