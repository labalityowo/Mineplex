package mineplex.core.gadget.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.gadget.types.ItemGadget;

public class ItemGadgetUseEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    
    private Player _player;
    private ItemGadget _gadget;
    private int _count;
    private boolean _cancelled;
    private String _cancelledMessage = "";
    
    public ItemGadgetUseEvent(Player player, ItemGadget gadget, int count) 
    {
    	_player = player;
    	_gadget = gadget;
    	_count = count;
    }
  
    public HandlerList getHandlers() 
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList() 
    {
        return handlers;
    }

	public int getCount() 
	{
		return _count;
	}
	
	public ItemGadget getGadget() 
	{
		return _gadget;
	}

	public Player getPlayer()
	{
		return _player;
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

	public String getCancelledMessage()
	{
		return _cancelledMessage;
	}

	public void setCancelledMessage(String cancelledMessage)
	{
		_cancelledMessage = cancelledMessage;
	}
}