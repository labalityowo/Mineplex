package mineplex.core.gadget.event;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.gadget.types.Gadget;

public class GadgetSelectLocationEvent extends Event implements Cancellable
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final Gadget _gadget;
	private final Location _location;
	private boolean _cancelled;

	public GadgetSelectLocationEvent(Gadget gadget, Location location)
	{
		_gadget = gadget;
		_location = location;
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

	public Location getLocation()
	{
		return _location;
	}

	@Override
	public void setCancelled(boolean cancel)
	{
		_cancelled = cancel;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}
}
