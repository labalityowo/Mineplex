package nautilus.game.arcade.stats;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.titles.tracks.standard.PeacefulTrack;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;

public class WinStatTracker extends StatTracker<Game>
{
	public WinStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.End)
		{
			List<Player> winners = getGame().getWinners();

			if (winners != null)
			{
				for (Player winner : winners)
				{
					if (!winner.isOnline())
						continue;
					
					addStat(winner, "Wins", 1, false, false);
					addStat(winner, "TrackWins", 1, false, false);


					if (getGame().getArcadeManager().GetServerConfig().RewardStats)
					{
						getGame().getArcadeManager().getTrackManager().getTrack(PeacefulTrack.class).wonGame(winner, getGame().GetType().getDisplay());
					}

//					if (getGame().GetKit(winner) != null)
//						addStat(winner, getGame().GetKit(winner).getName() + " Wins", 1, false, false);
				}
			}
		}
	}
}
