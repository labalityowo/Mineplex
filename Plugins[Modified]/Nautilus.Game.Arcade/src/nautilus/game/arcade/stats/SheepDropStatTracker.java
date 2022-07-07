package nautilus.game.arcade.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.sheep.SheepGame;

public class SheepDropStatTracker extends StatTracker<Game>
{
	public SheepDropStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onSheepStolen(SheepGame.DropEnemySheepEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		addStat(event.getPlayer(), "AnimalRescue", 1, false, false);
	}
}
