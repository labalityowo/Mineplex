package mineplex.core.disguise.playerdisguise.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerUndisguisedEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private Player _player;
	public PlayerUndisguisedEvent(Player disguisee)
	{
		this._player = disguisee;
	}
	
	public Player getPlayer()
	{
		return _player;
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
