package mineplex.game.nano.game.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerDeathOutEvent extends PlayerEvent implements Cancellable
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	private boolean _shouldRespawn = true;
	private boolean _cancelled;

	public PlayerDeathOutEvent(Player who)
	{
		super(who);
	}

	public void setShouldRespawn(boolean shouldRespawn)
	{
		_shouldRespawn = shouldRespawn;
	}

	public boolean shouldRespawn()
	{
		return _shouldRespawn;
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

	@Override
	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}
}
