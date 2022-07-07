package mineplex.core.gadget.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.gadget.types.Gadget;

public class GadgetDisableEvent extends Event
{
	
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	private Gadget _gadget;

	public GadgetDisableEvent(Player player, Gadget gadget)
	{
		_player = player;
		_gadget = gadget;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public Gadget getGadget()
	{
		return _gadget;
	}

	public static HandlerList getHandlerList() 
    {
        return handlers;
    }
	
	public HandlerList getHandlers() 
    {
        return handlers;
    }

}
