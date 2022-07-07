package nautilus.game.arcade.game.games.common.ctf_data;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerCaptureFlagEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private Player _player;
	
	public PlayerCaptureFlagEvent(Player player)
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
