package mineplex.core.incognito.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when an Incognito player is getting hidden from all other players.
 */
public class IncognitoHidePlayerEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	private boolean _cancelled;
	
	public IncognitoHidePlayerEvent(Player player)
	{
		_player = player;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}
	
	public boolean isCancelled()
	{
		return _cancelled;
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