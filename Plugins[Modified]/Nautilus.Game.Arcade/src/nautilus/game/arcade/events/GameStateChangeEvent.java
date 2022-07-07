package nautilus.game.arcade.events;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStateChangeEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private final Game _game;
    private final GameState _to;
    
    public GameStateChangeEvent(Game game, GameState to)
    {
       _game = game;
       _to = to;
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
        
    public GameState GetState()
    {
    	return _to;
    }
}
