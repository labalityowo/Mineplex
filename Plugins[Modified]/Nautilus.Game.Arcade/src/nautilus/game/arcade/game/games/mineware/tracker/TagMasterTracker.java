package nautilus.game.arcade.game.games.mineware.tracker;

import org.bukkit.entity.Player;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeReverseTag;

/**
 * A tracker for the Tag Master achievement.
 * <br>
 * <br>
 * <b>Challenge:</b> Tag Master
 * <br>
 * <b>Goal:</b> Win 5 entire rounds without being untagged
 */
public class TagMasterTracker extends ChallengeStatTracker
{
	public TagMasterTracker(Game game)
	{
		super(game);
	}

	@Override
	protected void track(Challenge challenge)
	{
		if (challenge instanceof ChallengeReverseTag)
		{
			ChallengeReverseTag reverseTag = (ChallengeReverseTag) challenge;

			for (Player player : getGame().GetPlayers(true))
			{
				if (reverseTag.hasData(player))
				{
					addStat(player, "TagMaster", 1, false, false);
				}
			}
		}
	}
}
