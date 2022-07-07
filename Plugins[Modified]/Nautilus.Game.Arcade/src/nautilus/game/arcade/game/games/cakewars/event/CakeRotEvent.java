package nautilus.game.arcade.game.games.cakewars.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CakeRotEvent extends Event
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

}
