package nautilus.game.arcade.game.games.mineware.tracker;

import org.bukkit.entity.Player;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeWaveCrush;

/**
 * A tracker for the Surf Up achievement.
 * <br>
 * <br>
 * <b>Challenge:</b> Wave Crush
 * <br>
 * <b>Goal:</b> Avoid 500 waves
 */
public class SurfUpTracker extends ChallengeStatTracker
{
	public SurfUpTracker(Game game)
	{
		super(game);
	}

	@Override
	protected void track(Challenge challenge)
	{
		if (challenge instanceof ChallengeWaveCrush)
		{
			ChallengeWaveCrush waveCrush = (ChallengeWaveCrush) challenge;

			for (Player player : getGame().GetPlayers(true))
			{
				if (waveCrush.hasData(player))
				{
					int score = (int) waveCrush.getData(player);

					if (score > 0)
					{
						addStat(player, "SurfUp", score, false, false);
					}
				}
			}
		}
	}
}
