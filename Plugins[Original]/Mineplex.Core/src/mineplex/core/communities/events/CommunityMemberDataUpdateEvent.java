package mineplex.core.communities.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CommunityMemberDataUpdateEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	
	public CommunityMemberDataUpdateEvent(Player player)
	{
		_player = player;
	}
	
	public Player getPlayer()
	{
		return _player;
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