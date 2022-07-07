package nautilus.game.arcade.game.games.gladiators.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by William (WilliamTiger).
 * 10/12/15
 */
public class RoundStartEvent extends Event
{
	private static final HandlerList _handlers = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return _handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}
}
