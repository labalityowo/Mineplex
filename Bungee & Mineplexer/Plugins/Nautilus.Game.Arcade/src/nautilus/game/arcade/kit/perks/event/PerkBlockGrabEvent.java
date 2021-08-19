package nautilus.game.arcade.kit.perks.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PerkBlockGrabEvent extends Event 
{
    private static final HandlerList handlers = new HandlerList();
    private Player _player;
    private int _id;
    private byte _data;
    
    public PerkBlockGrabEvent(Player player, int id, byte data)
    {
       _player = player;
       _id = id;
       _data = data;
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
    
    public int GetId()
    {
    	return _id;
    }
    
    public byte GetData()
    {
    	return _data;
    }
}
