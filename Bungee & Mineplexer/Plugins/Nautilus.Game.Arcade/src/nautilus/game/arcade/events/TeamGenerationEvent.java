package nautilus.game.arcade.events;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam.PlayerState;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeamGenerationEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private Game _game;
    private Player _player;
    private PlayerState _state;
    private boolean _cancelled = false;
    
    public TeamGenerationEvent(Game game)
    {
       _game = game;
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
    
    public PlayerState GetState()
    {
    	return _state;
    }

	@Override
	public boolean isCancelled() 
	{
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}
}
