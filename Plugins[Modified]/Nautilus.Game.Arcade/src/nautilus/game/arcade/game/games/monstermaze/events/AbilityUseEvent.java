package nautilus.game.arcade.game.games.monstermaze.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class AbilityUseEvent extends PlayerEvent
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
	
	public AbilityUseEvent(Player player)
	{
		super(player);
	}
}
