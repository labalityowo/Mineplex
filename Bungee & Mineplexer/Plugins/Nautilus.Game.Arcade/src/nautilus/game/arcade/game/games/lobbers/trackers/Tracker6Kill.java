package nautilus.game.arcade.game.games.lobbers.trackers;

import mineplex.core.common.util.UtilServer;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.lobbers.BombLobbers;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class Tracker6Kill extends StatTracker<Game>
{
	public Tracker6Kill(Game game)
	{
		super(game);
	}
	
	@EventHandler
	public void onEndgame(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
			return;
		
		if (getGame() instanceof BombLobbers)
		{
			for (Player player : UtilServer.getPlayers())
			{
				if (((BombLobbers) getGame()).getKills(player) >= 6.0)
				{
					addStat(player, "Killer", 1, true, false);
				}
			}
		}
	}
}
