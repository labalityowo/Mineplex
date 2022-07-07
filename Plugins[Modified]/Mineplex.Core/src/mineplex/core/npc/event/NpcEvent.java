package mineplex.core.npc.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NpcEvent extends Event 
{
    private static final HandlerList handlers = new HandlerList();
    
    private LivingEntity _npc;
    
    private boolean _cancelled = false;
    
    public NpcEvent(LivingEntity npc) 
    {
    	_npc = npc;
    }
  
    public HandlerList getHandlers() 
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList() 
    {
        return handlers;
    }

	public LivingEntity getNpc() 
	{
		return _npc;
	}

	public void setCancelled(boolean cancel)
	{
		_cancelled = cancel;
	}
	
	public boolean isCancelled()
	{
		return _cancelled;
	}
}