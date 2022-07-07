package mineplex.game.nano.game.event;

import org.bukkit.event.HandlerList;

import mineplex.game.nano.game.Game;

public class GameTimeoutEvent extends GameEvent
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	public GameTimeoutEvent(Game game)
	{
		super(game);
	}

	@Override
	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}
}
