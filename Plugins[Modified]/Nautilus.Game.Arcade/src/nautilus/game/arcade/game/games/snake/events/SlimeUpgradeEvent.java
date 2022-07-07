package nautilus.game.arcade.game.games.snake.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class SlimeUpgradeEvent extends PlayerEvent
{
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

	public SlimeUpgradeEvent(Player who)
	{
		super(who);
	}
}
