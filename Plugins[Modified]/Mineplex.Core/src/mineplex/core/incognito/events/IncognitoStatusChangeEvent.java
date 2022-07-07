package mineplex.core.incognito.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class IncognitoStatusChangeEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _player;
	private boolean _newState;
	
	private boolean _cancelled;
	
	private boolean _show = true;
	
	public IncognitoStatusChangeEvent(Player player, boolean newState)
	{
		_player = player;
		_newState = newState;
	}
	
	public boolean getNewState()
	{
		return _newState;
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
	
	public void show(boolean show)
	{
		_show = show;
	}

	public boolean doShow()
	{
		return _show;
	}
}