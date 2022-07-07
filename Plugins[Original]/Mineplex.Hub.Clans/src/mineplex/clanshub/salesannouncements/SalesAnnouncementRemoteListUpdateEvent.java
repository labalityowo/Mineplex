package mineplex.clanshub.salesannouncements;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SalesAnnouncementRemoteListUpdateEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	public SalesAnnouncementRemoteListUpdateEvent() {}
	
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}