package nautilus.game.arcade.game.games.mineware.tracker;

import org.bukkit.entity.Player;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeBuildRace;

/**
 * A tracker for the Speedy Builders achievement.
 * <br>
 * <br>
 * <b>Challenge:</b> Build Race
 * <br>
 * <b>Goal:</b> Place all blocks in your inventory within 15 seconds
 */
public class SpeedyBuildersTracker extends ChallengeStatTracker
{
	public static final long GOAL = 15000;

	public SpeedyBuildersTracker(Game game)
	{
		super(game);
	}

	@Override
	protected void track(Challenge challenge)
	{
		if (challenge instanceof ChallengeBuildRace)
		{
			ChallengeBuildRace buildRace = (ChallengeBuildRace) challenge;

			for (Player player : getGame().GetPlayers(true))
			{
				if (buildRace.hasData(player) && buildRace.getData().isDone(player))
				{
					addStat(player, "SpeedyBuilders", 1, false, false);
				}
			}
		}
	}
}
