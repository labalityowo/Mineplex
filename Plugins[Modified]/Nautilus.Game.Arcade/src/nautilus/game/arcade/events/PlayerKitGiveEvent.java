package nautilus.game.arcade.events;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.kit.Kit;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerKitGiveEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private Game _game;
    private Kit _kit;
    private Player _player;
    
    public PlayerKitGiveEvent(Game game, Kit kit, Player player)
    {
       _game = game;
       _kit = kit;
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
    
    public Game getGame()
    {
    	return _game;
    }
        
    public Kit getKit()
    {
    	return _kit;
    }
    
    public Player getPlayer()
    {
    	return _player;
    }
}
