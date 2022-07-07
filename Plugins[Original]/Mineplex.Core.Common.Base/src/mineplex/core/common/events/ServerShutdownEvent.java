package mineplex.core.common.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerShutdownEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private JavaPlugin _plugin;
	
	public ServerShutdownEvent(JavaPlugin plugin)
	{
		_plugin = plugin;
	}
	
	public JavaPlugin getPlugin()
	{
		return _plugin;
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