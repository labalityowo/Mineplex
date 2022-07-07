package mineplex.core.boosters.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player is about to receive a booster gem. If cancelled the player will not receive said gem
 */
public class BoosterItemGiveEvent extends Event implements Cancellable
{
	private Player _player;
	private boolean _cancelled;

	public BoosterItemGiveEvent(Player player)
	{
		this._player = player;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public boolean isCancelled()
	{
		return this._cancelled;
	}

	public void setCancelled(boolean cancelled)
	{
		this._cancelled = cancelled;
	}

	private static final HandlerList _handlers = new HandlerList();

	private static HandlerList getHandlerList()
	{
		return _handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}
}
