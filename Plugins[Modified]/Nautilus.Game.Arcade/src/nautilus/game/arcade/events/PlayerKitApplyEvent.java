package nautilus.game.arcade.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.kit.Kit;

public class PlayerKitApplyEvent extends PlayerEvent implements Cancellable
{

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Game _game;
    private final Kit _kit;
    private boolean _cancelled;

    public PlayerKitApplyEvent(Game game, Kit kit, Player player)
    {
    	super(player);

       _game = game;
       _kit = kit;
    }
 
    public HandlerList getHandlers()
    {
        return HANDLER_LIST;
    }
 
    public static HandlerList getHandlerList()
    {
        return HANDLER_LIST;
    }
    
    public Game getGame()
    {
    	return _game;
    }
        
    public Kit getKit()
    {
    	return _kit;
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
