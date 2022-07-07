package mineplex.game.clans.clans.map.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/*
 * This event is called when a Player is about to receive a Clans Map
 */
public class PlayerGetMapEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	
	private boolean _cancelled;
	
	public PlayerGetMapEvent(Player player)
	{
		_player = player;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public boolean isCancelled()
	{
		return _cancelled;
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