package mineplex.core.account.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.account.permissions.PermissionGroup;

public class PrimaryGroupUpdateEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();

	private int _accountId;
	private PermissionGroup _group;

	public PrimaryGroupUpdateEvent(int accountId, PermissionGroup group)
	{
		_accountId = accountId;
		_group = group;
	}

	public int getAccountId()
	{
		return _accountId;
	}

	public PermissionGroup getGroup()
	{
		return _group;
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