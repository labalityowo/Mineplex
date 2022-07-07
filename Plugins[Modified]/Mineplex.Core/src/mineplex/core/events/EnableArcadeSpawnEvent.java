package mineplex.core.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EnableArcadeSpawnEvent extends Event
{

	/**
	 * Allows spawning mobs inside arcade games without having to do it inside Arcade's code
	 */

	private static final HandlerList handlers = new HandlerList();

	private boolean _enable;

	public EnableArcadeSpawnEvent(boolean enable)
	{
		_enable = enable;
	}

	public boolean canEnable()
	{
		return _enable;
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
