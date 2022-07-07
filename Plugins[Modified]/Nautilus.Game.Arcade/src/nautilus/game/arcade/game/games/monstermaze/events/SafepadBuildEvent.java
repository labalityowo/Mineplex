package nautilus.game.arcade.game.games.monstermaze.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SafepadBuildEvent extends Event
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
}
