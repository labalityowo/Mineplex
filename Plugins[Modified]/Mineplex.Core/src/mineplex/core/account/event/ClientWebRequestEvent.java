package mineplex.core.account.event;

import com.google.gson.stream.JsonWriter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClientWebRequestEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    
    private JsonWriter _writer;
 
    public ClientWebRequestEvent(JsonWriter writer)
    {
    	_writer = writer;
    }
    
    public JsonWriter GetJsonWriter()
    {
    	return _writer;
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
