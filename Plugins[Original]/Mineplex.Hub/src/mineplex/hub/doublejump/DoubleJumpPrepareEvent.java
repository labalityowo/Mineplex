package mineplex.hub.doublejump;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class DoubleJumpPrepareEvent extends PlayerEvent implements Cancellable
{

	private static final HandlerList _handlers = new HandlerList();

	private boolean _cancelled;

	public DoubleJumpPrepareEvent(Player who)
	{
		super(who);
	}

	@Override
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	public static HandlerList getHandlerList()
	{
		return _handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

}
