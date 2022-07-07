package mineplex.game.clans.clans.invsee;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class InvseeModifyOnlineInventoryEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player _modified;
	private boolean _cancelled;
	
	public InvseeModifyOnlineInventoryEvent(Player modified)
	{
		_modified = modified;
	}
	
	public Player getModified()
	{
		return _modified;
	}
	
	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}