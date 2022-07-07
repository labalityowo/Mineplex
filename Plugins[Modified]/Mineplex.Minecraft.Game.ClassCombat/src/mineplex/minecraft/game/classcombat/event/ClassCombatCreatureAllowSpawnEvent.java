package mineplex.minecraft.game.classcombat.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClassCombatCreatureAllowSpawnEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    
    private String _worldName;
    private boolean _allow;
    
    public ClassCombatCreatureAllowSpawnEvent(String worldName, boolean allow)
    {
    	_worldName = worldName;
       _allow = allow;
    }
    
    public String getWorldName()
    {
    	return _worldName;
    }
 
    public HandlerList getHandlers()
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
    
    public boolean getAllowed()
    {
    	return _allow;
    }
}
