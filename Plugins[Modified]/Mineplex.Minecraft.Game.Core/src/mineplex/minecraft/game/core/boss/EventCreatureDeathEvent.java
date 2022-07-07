package mineplex.minecraft.game.core.boss;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EventCreatureDeathEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private EventCreature _creature;
	
	public EventCreatureDeathEvent(EventCreature creature)
	{
		_creature = creature;
	}
	
	public EventCreature getCreature()
	{
		return _creature;
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