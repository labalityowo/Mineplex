package nautilus.game.arcade.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.sheep.SheepGame;

public class SheepThiefStatTracker extends StatTracker<Game>
{
	public SheepThiefStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onSheepStolen(SheepGame.SheepStolenEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		addStat(event.getPlayer(), "Thief", 1, false, false);
	}
}
