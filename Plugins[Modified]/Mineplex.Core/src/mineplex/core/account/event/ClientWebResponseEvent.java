package mineplex.core.account.event;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClientWebResponseEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    
    private String _response;
    private UUID _uuid;
 
    public ClientWebResponseEvent(String response, UUID uuid)
    {
    	_response = response;
    	_uuid = uuid;
    }
    
    public String GetResponse()
    {
    	return _response;
    }
        
    public HandlerList getHandlers()
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList()
    {
        return handlers;
    }

	public UUID getUniqueId()
	{
		return _uuid;
	}
}
