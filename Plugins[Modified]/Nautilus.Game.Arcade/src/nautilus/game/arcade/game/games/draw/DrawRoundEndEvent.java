package nautilus.game.arcade.game.games.draw;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DrawRoundEndEvent extends Event
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

	private final DrawRound _drawRound;

	public DrawRoundEndEvent(DrawRound drawRound)
	{
		_drawRound = drawRound;
	}

	public DrawRound getDrawRound()
	{
		return _drawRound;
	}
}
