package mineplex.game.nano.game.components.stats;

import java.util.Arrays;

import org.bukkit.entity.Player;

import mineplex.core.lifetimes.ListenerComponent;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;

public class StatTracker<T extends Game> extends ListenerComponent implements StatsComponent
{

	protected final T _game;

	protected StatTracker(T game)
	{
		_game = game;

		game.getLifetime().register(this, Arrays.asList(GameState.Live, GameState.End));
	}

	@Override
	public void addStat(Player player, String stat, int amount, boolean limitTo1, boolean global)
	{
		_game.addStat(player, stat, amount, limitTo1, global);
	}
}
