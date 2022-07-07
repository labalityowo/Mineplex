package nautilus.game.arcade.game.games.cakewars.trackers;

import org.bukkit.event.EventHandler;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.stats.StatTracker;

public class WinWithOneBiteTracker extends StatTracker<CakeWars>
{

	private static final byte ONE_BITE_CAKE = 6;

	public WinWithOneBiteTracker(CakeWars game)
	{
		super(game);
	}

	@EventHandler
	public void gameEnd(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
		{
			return;
		}

		GameTeam winners = getGame().WinnerTeam;

		if (winners == null || getGame().getCakeTeamModule().getCakeTeam(winners).getCake().getBlock().getData() != ONE_BITE_CAKE)
		{
			return;
		}

		winners.GetPlayers(false).forEach(player -> addStat(player, "WinWithOneBite", 1, true, false));
	}
}
