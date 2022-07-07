package nautilus.game.arcade.game.games.monstermaze.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class FirstToSafepadEvent extends PlayerEvent
{
	/**
	 * @author Mysticate
	 */
	
	private static final HandlerList _handlers = new HandlerList();
	
	private static HandlerList getHandlerList()
	{
		return _handlers;
	}
	
	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}
	
	public FirstToSafepadEvent(Player player)
	{
		super(player);
	}
}
