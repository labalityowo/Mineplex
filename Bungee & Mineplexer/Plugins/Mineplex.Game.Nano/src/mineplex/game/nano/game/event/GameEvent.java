package mineplex.game.nano.game.event;

import org.bukkit.event.Event;

import mineplex.game.nano.game.Game;

public abstract class GameEvent extends Event
{

	private final Game _game;

	public GameEvent(Game game)
	{
		_game = game;
	}

	public Game getGame()
	{
		return _game;
	}
}
