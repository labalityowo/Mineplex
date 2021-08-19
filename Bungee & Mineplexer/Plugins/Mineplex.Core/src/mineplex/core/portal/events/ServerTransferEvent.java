package mineplex.core.portal.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.portal.Intent;

public class ServerTransferEvent extends Event implements Cancellable
{
	private static final HandlerList HANDLER_LIST = new HandlerList();

	private Player _player;
	private String _server;
	private boolean _cancel = false;

	private Intent _intent;

	public ServerTransferEvent(Player player, String server, Intent intent)
	{
		_player = player;
		_server = server;
		_intent = intent;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public String getServer()
	{
		return _server;
	}

	public Intent getIntent()
	{
		return _intent;
	}

	@Override
	public boolean isCancelled()
	{
		return this._cancel;
	}

	@Override
	public void setCancelled(boolean b)
	{
		this._cancel = b;
	}

	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}
}