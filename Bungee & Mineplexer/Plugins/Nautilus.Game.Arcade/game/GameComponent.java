package nautilus.game.arcade.game;

import mineplex.core.lifetimes.Lifetime;
import mineplex.core.lifetimes.Lifetimed;
import mineplex.core.lifetimes.ListenerComponent;

import java.util.Arrays;

public class GameComponent<T extends Game> extends ListenerComponent implements Lifetimed
{
	private final Lifetime _lifetime;
	private final T _game;

	public GameComponent(T game, Game.GameState...active)
	{
		_game = game;
		if (active == null || active.length == 0)
		{
			// Active for the entire duration of the game.
			_lifetime = game.getLifetime();
			_lifetime.register(this);
		} else
		{
			_lifetime = game.getLifetime().register(this, Arrays.asList(active));
		}
	}

	@Override
	public Lifetime getLifetime()
	{
		return _lifetime;
	}

	public T getGame() {
		return _game;
	}
}
