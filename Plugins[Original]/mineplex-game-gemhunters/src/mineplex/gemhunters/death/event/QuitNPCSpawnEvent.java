package mineplex.gemhunters.death.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class QuitNPCSpawnEvent extends PlayerEvent implements Cancellable
{

	private static final HandlerList HANDLERS = new HandlerList();
	
	private boolean _cancel;
	
	public QuitNPCSpawnEvent(Player who)
	{
		super(who);
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
