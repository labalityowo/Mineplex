package mineplex.core.gadget.gadgets.particle.king.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.gadget.gadgets.particle.king.types.King;

public class UpdateKingEvent extends Event
{

	private static final HandlerList handlers = new HandlerList();

	private King _newKing;
	private King _oldKing;

	public UpdateKingEvent(King newKing, King oldKing)
	{
		_newKing = newKing;
		_oldKing = oldKing;
	}

	public King getNewKing()
	{
		return _newKing;
	}

	public King getOldKing()
	{
		return _oldKing;
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
