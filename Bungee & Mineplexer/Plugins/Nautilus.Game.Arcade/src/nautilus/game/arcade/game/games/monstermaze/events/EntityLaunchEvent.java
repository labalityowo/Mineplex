package nautilus.game.arcade.game.games.monstermaze.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

public class EntityLaunchEvent extends EntityEvent
{
	/**
	 * @author Mysticate
	 */
	
	private static final HandlerList _handlers = new HandlerList();
	
	private static HandlerList getHandlerList()
	{
		return _handlers;
	}
	
	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}
	
	public EntityLaunchEvent(Entity ent)
	{
		super(ent);
	}
}
