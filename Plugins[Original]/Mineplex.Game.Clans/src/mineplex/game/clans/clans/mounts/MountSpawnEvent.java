package mineplex.game.clans.clans.mounts;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called after a mount spawns
 */
public class MountSpawnEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private final Mount _mount;
	
	public MountSpawnEvent(Mount mount)
	{
		_mount = mount;
	}
	
	public Mount getMount()
	{
		return _mount;
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