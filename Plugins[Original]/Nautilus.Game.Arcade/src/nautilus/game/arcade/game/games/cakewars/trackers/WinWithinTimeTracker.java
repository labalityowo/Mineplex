package nautilus.game.arcade.game.games.cakewars.trackers;

import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilTime;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.stats.StatTracker;

public class WinWithinTimeTracker extends StatTracker<Game>
{

	private final String _stat;
	private final long _time;

	public WinWithinTimeTracker(Game game, String stat, long timeInMs)
	{
		super(game);

		_stat = stat;
		_time = timeInMs;
	}

	@EventHandler
	public void gameEnd(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End || UtilTime.elapsed(getGame().getGameLiveTime(), _time))
		{
			return;
		}

		GameTeam winners = getGame().WinnerTeam;

		if (winners == null)
		{
			return;
		}

		winners.GetPlayers(false).forEach(player -> addStat(player, _stat, 1, true, false));
	}

}
