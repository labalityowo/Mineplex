package nautilus.game.arcade.game.games.draw;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class DrawGuessCorrectlyEvent extends PlayerEvent
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
	private final int _rank;

	public DrawGuessCorrectlyEvent(Player who, DrawRound drawRound, int rank)
	{
		super(who);

		_drawRound = drawRound;
		_rank = rank;
	}

	public DrawRound getDrawRound()
	{
		return _drawRound;
	}

	public int getRank()
	{
		return _rank;
	}
}
