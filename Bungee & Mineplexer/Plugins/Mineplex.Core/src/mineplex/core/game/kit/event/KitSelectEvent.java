package mineplex.core.game.kit.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import mineplex.core.game.kit.GameKit;

public class KitSelectEvent extends PlayerEvent implements Cancellable
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final GameKit _kit;

	private boolean _cancelled;

	public KitSelectEvent(Player who, GameKit kit)
	{
		super(who);

		_kit = kit;
	}

	public GameKit getKit()
	{
		return _kit;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

}
