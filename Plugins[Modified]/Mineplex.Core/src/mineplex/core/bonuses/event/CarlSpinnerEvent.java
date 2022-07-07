package mineplex.core.bonuses.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player attempts to purchase a spin through carl
 */
public class CarlSpinnerEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();

	private Player _player;
	private boolean _cancelled;

	public CarlSpinnerEvent(Player player)
	{
		_player = player;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public void setPlayer(Player player)
	{
		_player = player;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean b)
	{
		_cancelled = b;
	}
}
