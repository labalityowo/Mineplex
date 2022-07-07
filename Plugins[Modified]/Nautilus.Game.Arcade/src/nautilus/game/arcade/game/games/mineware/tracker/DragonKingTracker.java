package nautilus.game.arcade.game.games.mineware.tracker;

import org.bukkit.entity.Player;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeEggSmash;

/**
 * A tracker for the Dragon King achievement.
 * <br>
 * <br>
 * <b>Challenge:</b> Egg Smash
 * <br>
 * <b>Goal:</b> Smash 300 dragon eggs
 */
public class DragonKingTracker extends ChallengeStatTracker
{
	public DragonKingTracker(Game game)
	{
		super(game);
	}

	@Override
	protected void track(Challenge challenge)
	{
		if (challenge instanceof ChallengeEggSmash)
		{
			ChallengeEggSmash eggSmash = (ChallengeEggSmash) challenge;

			for (Player player : getGame().GetPlayers(true))
			{
				if (eggSmash.hasData(player))
				{
					int score = (int) eggSmash.getData(player);

					if (score > 0)
					{
						addStat(player, "DragonKing", score, false, false);
					}
				}
			}
		}
	}
}
