package mineplex.core.account.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class ClientUnloadEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();

	private String _name;
	private UUID _uuid;
	private int _accountId;

	public ClientUnloadEvent(String name, UUID uuid, int accountId)
	{
		_name = name;
		_accountId = accountId;
		this._uuid = uuid;
	}

	public String GetName()
	{
		return _name;
	}

	public UUID getUniqueId()
	{
		return this._uuid;
	}

	public int getAccountId()
	{
		return _accountId;
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
