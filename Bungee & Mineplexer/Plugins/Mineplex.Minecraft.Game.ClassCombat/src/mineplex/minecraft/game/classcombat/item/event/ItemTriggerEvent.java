package mineplex.minecraft.game.classcombat.item.event;

import mineplex.minecraft.game.classcombat.item.Item;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ItemTriggerEvent extends Event 
{
    private static final HandlerList handlers = new HandlerList();
    
    private Player _player;
    private Item _item;
    private boolean _cancelled = false;

    public ItemTriggerEvent(Player player,  Item item) 
    {
    	_player = player;
    	_item = item;
    }

	public HandlerList getHandlers() 
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList() 
    {
        return handlers;
    }

	public Player GetPlayer()
	{
		return _player;
	}

	public Item GetItemType() 
	{
		return _item;
	}
	
	public boolean IsCancelled() {
		return _cancelled;
	}

	public void SetCancelled(boolean cancelled) 
	{
		this._cancelled = cancelled;
	}
}