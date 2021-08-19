package nautilus.game.arcade.game.games.mineware.tracker;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.events.ChallengeEndEvent;
import nautilus.game.arcade.stats.StatTracker;

/**
 * A statistic tracker that tracks data when a challenge ends.
 */
public abstract class ChallengeStatTracker extends StatTracker<Game>
{
	public ChallengeStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onChallengeEnd(ChallengeEndEvent event)
	{
		if (getGame().GetState() != GameState.Live)
			return;

		track(event.getEndedChallenge());
	}

	protected abstract void track(Challenge challenge);
}
