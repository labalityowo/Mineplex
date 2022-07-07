package nautilus.game.arcade.stats;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.sheep.SheepGame;

public class WinWithSheepStatTracker extends StatTracker<SheepGame>
{
	public WinWithSheepStatTracker(SheepGame game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.End)
		{
			if (getGame().getSheepCount(getGame().WinnerTeam) > 12)
			{
				List<Player> winners = getGame().getWinners();

				if (winners != null)
				{
					for (Player winner : winners)
						addStat(winner, "Selfish", 1, true, false);
				}
			}
		}
	}
}
