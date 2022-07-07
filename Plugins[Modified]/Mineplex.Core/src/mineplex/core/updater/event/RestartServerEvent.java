package mineplex.core.updater.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.updater.RestartReason;

public class RestartServerEvent extends Event implements Cancellable
{

    private static final HandlerList HANDLER_LIST = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	private final RestartReason _reason;
	private boolean _cancelled;

	public RestartServerEvent(RestartReason reason)
    {
    	_reason = reason;
    }

    public RestartReason getReason()
    {
    	return _reason;
    }

    @Override
    public boolean isCancelled()
    {
        return _cancelled;
    }

    @Override
    public void setCancelled(boolean cancel)
    {
        _cancelled = cancel;
    }

	@Override
	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}
}