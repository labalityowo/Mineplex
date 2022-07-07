package mineplex.core.disguise.playerdisguise.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerPreDisguiseEvent extends Event implements Cancellable
{
	private static final HandlerList HANDLERS = new HandlerList();

	private Player _player;
	private String _requestedUsername;
	private boolean _cancelled;

	public PlayerPreDisguiseEvent(Player disguisee, String requestedUsername)
	{
		this._player = disguisee;
		this._requestedUsername = requestedUsername;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public String getRequestedUsername()
	{
		return this._requestedUsername;
	}

	@Override
	public HandlerList getHandlers()
	{
		return HANDLERS;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLERS;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean b)
	{
		this._cancelled = b;
	}
}
