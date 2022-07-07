package nautilus.game.arcade.kit.perks.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PerkDestructorBlockEvent extends Event 
{
    private static final HandlerList handlers = new HandlerList();
    private Player _player;
    private Block _block;
    
    private boolean _cancelled = false;
    
    public PerkDestructorBlockEvent(Player player, Block block)
    {
       _player = player;
       _block = block;
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
    
    public Block getBlock()
    {
    	return _block;
    }
    
    public void setCancelled(boolean var)
    {
    	_cancelled = var;
    }
    
    public boolean isCancelled()
    {
    	return _cancelled;
    }
}
