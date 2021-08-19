package mineplex.core.communities.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CommunityBrowserUpdateEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	public CommunityBrowserUpdateEvent() {}
	
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}