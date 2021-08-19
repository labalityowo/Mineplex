package mineplex.core.common.events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called just before UtilPlayer#message sends out a message to the specified Player.
 */
public class PlayerMessageEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	private String _message;
	
	private boolean _cancelled;
	
	public PlayerMessageEvent(Player player, String message)
	{
		_player = player;
		_message = message;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public boolean isCancelled()
	{
		return _cancelled;
	}
	
	public String getMessage()
	{
		return _message;
	}
	
	public String getUnformattedMessage()
	{
		return ChatColor.stripColor(_message);
	}
	
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
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