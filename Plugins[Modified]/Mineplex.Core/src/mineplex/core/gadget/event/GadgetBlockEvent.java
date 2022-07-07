package mineplex.core.gadget.event;

import java.util.Collection;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.gadget.types.Gadget;

public class GadgetBlockEvent extends Event implements Cancellable
{

    private static final HandlerList HANDLER_LIST = new HandlerList();
    
    private final Gadget _gadget;
    private final Collection<Block> _blocks;
    
    private boolean _cancelled;

    public GadgetBlockEvent(Gadget gadget, Collection<Block> blocks)
    {
    	_gadget = gadget;
    	_blocks = blocks;
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

	public Collection<Block> getBlocks()
	{
		return _blocks;
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