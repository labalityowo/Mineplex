package mineplex.core.treasure.animation.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import mineplex.core.treasure.TreasureSession;
import mineplex.core.treasure.animation.TreasureAnimation;
import mineplex.core.treasure.event.TreasureEvent;

/**
 * Called when a player is able to begin opening chests.
 */
public class TreasureAnimationFinishEvent extends TreasureEvent
{

	private static final HandlerList handlers = new HandlerList();

	private final TreasureAnimation _animation;

	public TreasureAnimationFinishEvent(Player player, TreasureSession session, TreasureAnimation animation)
	{
		super(player, session);

		_animation = animation;
	}

	public TreasureAnimation getAnimation()
	{
		return _animation;
	}

	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}
