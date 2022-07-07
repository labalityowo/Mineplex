package nautilus.game.arcade.stats;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;

public class WinFastStatTracker extends StatTracker<Game>
{
	private final int _seconds;
	private final String _stat;
	private long _gameStartTime;

	public WinFastStatTracker(Game game, int seconds, String stat)
	{
		super(game);

		_seconds = seconds;
		_stat = stat;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.Live)
			_gameStartTime = System.currentTimeMillis();
		else if (event.GetState() == Game.GameState.End)
		{
			if (System.currentTimeMillis() - _gameStartTime < _seconds * 1000)
			{
				List<Player> winners = getGame().getWinners();

				if (winners != null)
				{
					for (Player winner : winners)
						addStat(winner, getStat(), 1, true, false);
				}
			}
		}
	}

	public String getStat()
	{
		return _stat;
	}
}
