package nautilus.game.arcade.game.games.moba.progression;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class MobaExperienceCalculateEvent extends PlayerEvent
{

	private static final HandlerList _handlers = new HandlerList();

	private AtomicInteger _expEarned;

	public MobaExperienceCalculateEvent(Player player, AtomicInteger expEarned)
	{
		super(player);

		_expEarned = expEarned;
	}

	public AtomicInteger getExpEarned()
	{
		return _expEarned;
	}

	public static HandlerList getHandlerList()
	{
		return _handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

}
