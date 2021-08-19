package mineplex.minecraft.game.core.condition.events;

import mineplex.minecraft.game.core.condition.Condition;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ConditionExpireEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();

	private Condition _cond;

	public ConditionExpireEvent(Condition cond)
	{
		_cond = cond;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	public Condition getCondition()
	{
		return _cond;
	}

}
