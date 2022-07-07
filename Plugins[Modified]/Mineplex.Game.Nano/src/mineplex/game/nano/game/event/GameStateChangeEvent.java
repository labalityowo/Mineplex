package mineplex.game.nano.game.event;

import org.bukkit.event.HandlerList;

import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;

public class GameStateChangeEvent extends GameEvent
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	private final GameState _state;

	public GameStateChangeEvent(Game game, GameState state)
	{
		super(game);

		_state = state;
	}

	public GameState getState()
	{
		return _state;
	}

	@Override
	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}
}
