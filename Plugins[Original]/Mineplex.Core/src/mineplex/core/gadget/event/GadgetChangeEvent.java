package mineplex.core.gadget.event;

import mineplex.core.gadget.types.Gadget;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player enables or disables a gadget manually,
 * allowing it to be saved when the player changes a gadget, instead of
 * when they quit or join a server
 */
public class GadgetChangeEvent extends Event
{

	public enum GadgetState
	{
		ENABLED,
		DISABLED
	}

	private static final HandlerList handlers = new HandlerList();

	private final Player _player;
	private final Gadget _gadget;
	private final GadgetState _gadgetState;

	public GadgetChangeEvent(Player player, Gadget gadget, GadgetState gadgetState)
	{
		_player = player;
		_gadget = gadget;
		_gadgetState = gadgetState;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public Gadget getGadget()
	{
		return _gadget;
	}

	public GadgetState getGadgetState()
	{
		return _gadgetState;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

}
