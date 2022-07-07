package mineplex.core.treasure.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import mineplex.core.treasure.TreasureSession;

/**
 * Called when a player finishes opening chests
 */
public class TreasureFinishEvent extends TreasureEvent
{

	private static final HandlerList handlers = new HandlerList();

	public TreasureFinishEvent(Player player, TreasureSession session)
	{
		super(player, session);
	}

	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}
