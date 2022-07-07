package mineplex.core.powerplayclub;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SubscriptionAddEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();

	private int _accountId;
	private String _duration;

	public SubscriptionAddEvent(int accountId, String duration)
	{
		_accountId = accountId;
		_duration = duration;
	}

	public int getAccountId()
	{
		return _accountId;
	}

	public String getDuration()
	{
		return _duration;
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