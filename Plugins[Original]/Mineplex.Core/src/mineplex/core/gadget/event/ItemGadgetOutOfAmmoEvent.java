package mineplex.core.gadget.event;

import mineplex.core.gadget.types.ItemGadget;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ItemGadgetOutOfAmmoEvent extends Event 
{
    private static final HandlerList handlers = new HandlerList();
    
    private Player _player;
    private ItemGadget _gadget;
    
    public ItemGadgetOutOfAmmoEvent(Player player, ItemGadget gadget) 
    {
    	_player = player;
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
	
	public ItemGadget getGadget() 
	{
		return _gadget;
	}

	public Player getPlayer()
	{
		return _player;
	}
}