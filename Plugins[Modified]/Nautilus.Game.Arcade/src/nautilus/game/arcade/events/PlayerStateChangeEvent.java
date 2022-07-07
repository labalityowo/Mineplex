package nautilus.game.arcade.events;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam.PlayerState;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerStateChangeEvent extends PlayerEvent
{

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return HANDLER_LIST;
    }

    private final Game _game;
    private final PlayerState _state;
    
    public PlayerStateChangeEvent(Game game, Player player, PlayerState state)
    {
        super(player);

       _game = game;
       _state = state;
    }

    @Override
    public HandlerList getHandlers()
    {
        return HANDLER_LIST;
    }

    public Game GetGame()
    {
    	return _game;
    }

    @Deprecated
    public Player GetPlayer()
    {
    	return getPlayer();
    }
    
    public PlayerState GetState()
    {
    	return _state;
    }
}
