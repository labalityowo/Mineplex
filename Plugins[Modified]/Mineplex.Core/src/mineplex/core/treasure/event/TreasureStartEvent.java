package mineplex.core.treasure.event;

import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import mineplex.core.treasure.TreasureSession;
import mineplex.core.reward.Reward;

/**
 * Called when a player is able to begin opening chests.
 */
public class TreasureStartEvent extends TreasureEvent
{

	private static final HandlerList handlers = new HandlerList();

	public TreasureStartEvent(Player player, TreasureSession session)
	{
		super(player, session);
	}

	public List<Reward> getRewards()
	{
		return Collections.unmodifiableList(getSession().getRewards());
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
