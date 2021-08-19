package nautilus.game.arcade.game.games.mineware.tracker;

import org.bukkit.entity.Player;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeBouncingBlock;

/**
 * A tracker for the Bouncing Shadow achievement.
 * <br>
 * <br>
 * <b>Challenge:</b> Bouncing Block
 * <br>
 * <b>Goal:</b> Win 3 entire rounds without stepping on red wool
 */
public class BouncingShadowTracker extends ChallengeStatTracker
{
	public BouncingShadowTracker(Game game)
	{
		super(game);
	}

	@Override
	protected void track(Challenge challenge)
	{
		if (challenge instanceof ChallengeBouncingBlock)
		{
			ChallengeBouncingBlock bouncingBlock = (ChallengeBouncingBlock) challenge;

			for (Player player : getGame().GetPlayers(true))
			{
				if (bouncingBlock.hasData(player) && bouncingBlock.getData().isCompleted(player))
				{
					addStat(player, "BouncingShadow", 1, false, false);
				}
			}
		}
	}
}
