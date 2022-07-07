package nautilus.game.arcade.game.games.mineware.tracker;

import org.bukkit.entity.Player;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeMilkACow;

/**
 * A tracker for the Milk Man achievement.
 * <br>
 * <br>
 * <b>Challenge:</b> Milk a Cow
 * <br>
 * <b>Goal:</b> Deliver 300 buckets of milk to the farmer
 */
public class MilkManTracker extends ChallengeStatTracker
{
	public MilkManTracker(Game game)
	{
		super(game);
	}

	@Override
	protected void track(Challenge challenge)
	{
		if (challenge instanceof ChallengeMilkACow)
		{
			ChallengeMilkACow milkACow = (ChallengeMilkACow) challenge;

			for (Player player : getGame().GetPlayers(true))
			{
				if (milkACow.hasData(player))
				{
					int score = (int) milkACow.getData(player);

					if (score > 0)
					{
						addStat(player, "MilkMan", score, false, false);
					}
				}
			}
		}
	}
}
