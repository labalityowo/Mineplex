package mineplex.core.communities.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.core.communities.data.Community;

public class CommunitySettingUpdateEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Community _community;
	
	public CommunitySettingUpdateEvent(Community community)
	{
		_community = community;
	}
	
	public Community getCommunity()
	{
		return _community;
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