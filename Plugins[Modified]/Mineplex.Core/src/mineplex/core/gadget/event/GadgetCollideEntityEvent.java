package mineplex.core.gadget.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

import mineplex.core.gadget.types.Gadget;

public class GadgetCollideEntityEvent extends EntityEvent implements Cancellable
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final Gadget _gadget;
	private boolean _cancelled;

	public GadgetCollideEntityEvent(Gadget gadget, Entity collided)
	{
		super(collided);

		_gadget = gadget;
	}

	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	public Gadget getGadget()
	{
		return _gadget;
	}

	public void setCancelled(boolean cancel)
	{
		_cancelled = cancel;
	}

	public boolean isCancelled()
	{
		return _cancelled;
	}
}