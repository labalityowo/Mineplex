package nautilus.game.arcade.events;

import nautilus.game.arcade.game.Game;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerPrepareTeleportEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private Game _game;
    private Player _player;
    
    public PlayerPrepareTeleportEvent(Game game, Player player)
    {
       _game = game;
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
    
    public Game GetGame()
    {
    	return _game;
    }
    
    public Player GetPlayer()
    {
    	return _player;
    }
}
