package mineplex.core.common.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

/**
 * Called just before UtilAction#velocity changes an entity's velocity.
 */
public class EntityVelocityChangeEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Entity _ent;
	private Vector _vel;
	
	private boolean _cancelled;
	
	public EntityVelocityChangeEvent(Entity entity, Vector velocity)
	{
		_ent = entity;
		_vel = velocity;
	}
	
	public Entity getEntity()
	{
		return _ent;
	}
	
	public boolean isCancelled()
	{
		return _cancelled;
	}
	
	public Vector getVelocity()
	{
		return _vel;
	}
	
	public void setVelocity(Vector velocity)
	{
		_vel = velocity;
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