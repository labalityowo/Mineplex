package mineplex.minecraft.game.classcombat.item.event;

import mineplex.minecraft.game.classcombat.item.Item;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ProximityUseEvent extends Event 
{
    private static final HandlerList handlers = new HandlerList();
    
    private Player _player;
    private Item _item;
    private org.bukkit.entity.Item _entity;

    public ProximityUseEvent(Player player,  Item item, org.bukkit.entity.Item ent) 
    {
    	_player = player;
    	_item = item;
    	_entity = ent;
    }

	public HandlerList getHandlers() 
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList() 
    {
        return handlers;
    }

	public Player getPlayer()
	{
		return _player;
	}

	public Item getItemType() 
	{
		return _item;
	}
	
	public org.bukkit.entity.Item getEntity()
	{
		return _entity;
	}
}