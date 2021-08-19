package mineplex.minecraft.game.core.condition.events;

import mineplex.minecraft.game.core.condition.Condition;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ConditionApplyEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	private boolean _cancelled = false;

	private Condition _cond;
	
	public ConditionApplyEvent(Condition cond)
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

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean cancel)
	{
		_cancelled = cancel;
	}

	public Condition GetCondition()
	{
		return _cond;
	}
	
}
