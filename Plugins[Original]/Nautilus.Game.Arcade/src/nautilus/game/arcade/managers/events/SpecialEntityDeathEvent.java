package nautilus.game.arcade.managers.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpecialEntityDeathEvent extends Event
{

	private static final HandlerList handlers = new HandlerList();

	private Entity _entity;
	private Player _killer;

	public SpecialEntityDeathEvent(Entity entity, Player killer)
	{
		_entity = entity;
		_killer = killer;
	}

	public Entity getEntity()
	{
		return _entity;
	}

	public Player getKiller()
	{
		return _killer;
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
