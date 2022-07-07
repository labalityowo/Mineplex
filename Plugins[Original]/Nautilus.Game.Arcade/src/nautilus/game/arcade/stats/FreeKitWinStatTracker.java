package nautilus.game.arcade.stats;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.game.kit.KitAvailability;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;

public class FreeKitWinStatTracker extends StatTracker<Game>
{
	public FreeKitWinStatTracker(Game game)
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
					if (getGame().GetKit(winner) != null)
					{
						if (getGame().GetKit(winner).GetAvailability() == KitAvailability.Free)
							addStat(winner, "FreeKitsForever", 1, false, false);
					}
				}
			}
		}
	}
}
