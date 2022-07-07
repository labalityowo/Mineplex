package mineplex.gemhunters.spawn.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerTeleportIntoMapEvent extends PlayerEvent implements Cancellable
{

	private static final HandlerList HANDLERS = new HandlerList();
	
	private boolean _cancel;
	private Location _to;
	
	public PlayerTeleportIntoMapEvent(Player who, Location to)
	{
		super(who);
		
		_to = to;
	}
	
	public void setTo(Location to)
	{
		_to = to;
	}
	
	public Location getTo()
	{
		return _to;
	}

	public HandlerList getHandlers()
	{
		return HANDLERS;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLERS;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancel;
	}

	@Override
	public void setCancelled(boolean cancel)
	{
		_cancel = cancel;
	}

}
