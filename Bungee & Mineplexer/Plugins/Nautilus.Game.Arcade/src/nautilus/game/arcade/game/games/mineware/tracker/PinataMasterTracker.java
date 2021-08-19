package nautilus.game.arcade.game.games.mineware.tracker;

import org.bukkit.entity.Player;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.type.ChallengeChickenShooting;

/**
 * A tracker for the Pinata Master achievement.
 * <br>
 * <br>
 * <b>Challenge:</b> Chicken Shooting
 * <br>
 * <b>Goal:</b> Shoot 500 chickens
 */
public class PinataMasterTracker extends ChallengeStatTracker
{
	public PinataMasterTracker(Game game)
	{
		super(game);
	}

	@Override
	protected void track(Challenge challenge)
	{
		if (challenge instanceof ChallengeChickenShooting)
		{
			ChallengeChickenShooting chickenShooting = (ChallengeChickenShooting) challenge;

			for (Player player : getGame().GetPlayers(true))
			{
				if (chickenShooting.hasData(player))
				{
					int score = (int) chickenShooting.getData(player);

					if (score > 0)
					{
						addStat(player, "PinataMaster", score, false, false);
					}
				}
			}
		}
	}
}
