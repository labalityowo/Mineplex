package mineplex.core.updater.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.updater.UpdateType;
import net.minecraft.server.v1_8_R3.MinecraftServer;
 
public class UpdateEvent extends Event 
{
    private static final HandlerList handlers = new HandlerList();
    private final UpdateType _type;
 
    public UpdateEvent(UpdateType example) 
    {
    	_type = example;
    }
 
    public UpdateType getType() 
    {
        return _type;
    }
    
    public int getTick()
    {
    	return MinecraftServer.currentTick;
    }
 
    public HandlerList getHandlers() 
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList() 
    {
        return handlers;
    }
}