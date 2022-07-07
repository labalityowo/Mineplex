package mineplex.game.clans.clans.mounts;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called before a mount despawns
 */
public class MountDespawnEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private final Mount _mount;
	private final boolean _forced;
	
	public MountDespawnEvent(Mount mount, boolean forced)
	{
		_mount = mount;
		_forced = forced;
	}
	
	public Mount getMount()
	{
		return _mount;
	}
	
	public boolean isForced()
	{
		return _forced;
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