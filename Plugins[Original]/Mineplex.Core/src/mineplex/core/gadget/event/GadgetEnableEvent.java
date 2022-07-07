package mineplex.core.gadget.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import mineplex.core.gadget.types.Gadget;

public class GadgetEnableEvent extends PlayerEvent implements Cancellable
{

    private static final HandlerList handlers = new HandlerList();
    
    private final Gadget _gadget;
	private boolean _showMessage = true;
    
    private boolean _cancelled = false;
    
    public GadgetEnableEvent(Player player, Gadget gadget) 
    {
    	super(player);

    	_gadget = gadget;
    }
  
    public HandlerList getHandlers() 
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList() 
    {
        return handlers;
    }

	public Gadget getGadget() 
	{
		return _gadget;
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

	public void setShowMessage(boolean showMessage)
	{
		_showMessage = showMessage;
	}

	public boolean canShowMessage()
	{
		return _showMessage;
	}
}