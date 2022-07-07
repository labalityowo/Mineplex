package mineplex.core.disguise.playerdisguise.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerDisguisedEvent extends Event
{
	private static final HandlerList HANDLERS = new HandlerList();

	private Player _player;

	public PlayerDisguisedEvent(Player disguisee)
	{
		this._player = disguisee;
	}

	public Player getPlayer()
	{
		return _player;
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
