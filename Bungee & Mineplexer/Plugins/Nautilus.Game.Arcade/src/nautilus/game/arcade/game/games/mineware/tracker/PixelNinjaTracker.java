package nautilus.game.arcade.game.games.mineware.tracker;

import org.bukkit.entity.Player;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeFallingBlocks;

/**
 * A tracker for the Pixel Ninja achievement.
 * <br>
 * <br>
 * <b>Challenge:</b> Falling Blocks
 * <br>
 * <b>Goal:</b> Dodge 100 waves of falling blocks
 */
public class PixelNinjaTracker extends ChallengeStatTracker
{
	public PixelNinjaTracker(Game game)
	{
		super(game);
	}

	@Override
	protected void track(Challenge challenge)
	{
		if (challenge instanceof ChallengeFallingBlocks)
		{
			ChallengeFallingBlocks fallingBlocks = (ChallengeFallingBlocks) challenge;

			for (Player player : getGame().GetPlayers(true))
			{
				if (fallingBlocks.hasData(player))
				{
					int score = (int) fallingBlocks.getData(player);

					if (score > 0)
					{
						addStat(player, "PixelNinja", score, false, false);
					}
				}
			}
		}
	}
}