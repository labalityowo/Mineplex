package nautilus.game.arcade.game.games.mineware.tracker;

import org.bukkit.entity.Player;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeMiniOneInTheQuiver;

/**
 * A tracker for the Elite Archer achievement.
 * <br>
 * <br>
 * <b>Challenge:</b> Mini OITQ
 * <br>
 * <b>Goal:</b> Kill 100 players
 */
public class EliteArcherTracker extends ChallengeStatTracker
{
	public EliteArcherTracker(Game game)
	{
		super(game);
	}

	@Override
	protected void track(Challenge challenge)
	{
		if (challenge instanceof ChallengeMiniOneInTheQuiver)
		{
			ChallengeMiniOneInTheQuiver miniOITQ = (ChallengeMiniOneInTheQuiver) challenge;

			for (Player player : getGame().GetPlayers(true))
			{
				if (miniOITQ.hasData(player))
				{
					int score = (int) miniOITQ.getData(player);

					if (score > 0)
					{
						addStat(player, "EliteArcher", score, false, false);
					}
				}
			}
		}
	}
}