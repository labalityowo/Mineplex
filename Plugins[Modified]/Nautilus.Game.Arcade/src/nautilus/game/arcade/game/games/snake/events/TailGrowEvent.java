package nautilus.game.arcade.game.games.snake.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class TailGrowEvent extends PlayerEvent
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

	private final int _length;

	public TailGrowEvent(Player who, int length)
	{
		super(who);

		_length = length;
	}

	public int getLength()
	{
		return _length;
	}
}