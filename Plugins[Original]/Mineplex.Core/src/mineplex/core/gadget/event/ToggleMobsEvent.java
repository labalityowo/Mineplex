package mineplex.core.gadget.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ToggleMobsEvent extends Event
{

	private static final HandlerList handlers = new HandlerList();

	private boolean _enable;

	public ToggleMobsEvent(boolean enable)
	{
		_enable = enable;
	}

	public boolean enable()
	{
		return _enable;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

}
