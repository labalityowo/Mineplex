package mineplex.core.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/*
 * This event is called when the join message is about to be sent
 */
public class JoinMessageBroadcastEvent extends Event
{
	private static final HandlerList HANDLERS = new HandlerList();

	private Player _player;
	private String _username;

	public JoinMessageBroadcastEvent(Player player)
	{
		this._player = player;
		this._username = player.getName();
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public String getUsername()
	{
		return this._username;
	}

	public void setUsername(String username)
	{
		this._username = username;
	}

	public HandlerList getHandlers()
	{
		return HANDLERS;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLERS;
	}
}
