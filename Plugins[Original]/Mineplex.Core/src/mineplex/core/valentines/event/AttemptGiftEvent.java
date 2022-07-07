package mineplex.core.valentines.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AttemptGiftEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	public static HandlerList getHandlerList() { return handlers; }
	public HandlerList getHandlers() { return handlers; }

	private Player _from;
	private Player _to;

	public AttemptGiftEvent(Player from, Player to)
	{
		_from = from;
		_to = to;
	}

	public Player getFrom()
	{
		return _from;
	}

	public Player getTo()
	{
		return _to;
	}
}
