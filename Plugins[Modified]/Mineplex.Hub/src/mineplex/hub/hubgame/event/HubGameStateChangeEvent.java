package mineplex.hub.hubgame.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import mineplex.hub.hubgame.CycledGame;
import mineplex.hub.hubgame.CycledGame.GameState;

public class HubGameStateChangeEvent extends Event
{

	private static final HandlerList _handlers = new HandlerList();

	private final CycledGame _game;
	private final GameState _state;

	public HubGameStateChangeEvent(CycledGame game, GameState state)
	{
		_game = game;
		_state = state;
	}

	public CycledGame getGame()
	{
		return _game;
	}

	public GameState getState()
	{
		return _state;
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
