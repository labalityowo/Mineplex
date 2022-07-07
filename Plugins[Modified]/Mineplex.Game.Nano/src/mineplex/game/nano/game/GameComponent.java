package mineplex.game.nano.game;

import java.util.Arrays;

import mineplex.core.lifetimes.Lifetime;
import mineplex.core.lifetimes.Lifetimed;
import mineplex.core.lifetimes.ListenerComponent;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.components.Disposable;

public abstract class GameComponent<T extends Game> extends ListenerComponent implements Lifetimed, Disposable
{

	private final Lifetime _lifetime;
	private final T _game;

	public GameComponent(T game, GameState... active)
	{
		_game = game;

		if (active == null || active.length == 0)
		{
			// Active for the entire duration of the game.
			_lifetime = game.getLifetime();
			_lifetime.register(this);
		}
		else
		{
			_lifetime = game.getLifetime().register(this, Arrays.asList(active));
		}
	}

	@Override
	public final void deactivate()
	{
		disable();

		super.deactivate();
	}

	@Override
	public Lifetime getLifetime()
	{
		return _lifetime;
	}

	public T getGame()
	{
		return _game;
	}
}
