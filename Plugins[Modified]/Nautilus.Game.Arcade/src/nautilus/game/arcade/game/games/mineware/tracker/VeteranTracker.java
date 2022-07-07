package nautilus.game.arcade.game.games.mineware.tracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.stats.StatTracker;

/**
 * A tracker for the Veteran achievement.
 * <br>
 * <br>
 * <b>Goal:</b> Win 50 games of Bawk Bawk Battles
 */
public class VeteranTracker extends StatTracker<Game>
{
	public VeteranTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.End && getGame().getWinners().size() > 0)
		{
			Player winner = getGame().getWinners().get(0);

			if (winner != null)
			{
				addStat(winner, "Veteran", 1, true, false);
			}
		}
	}
}
