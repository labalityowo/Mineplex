package nautilus.game.arcade.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.snake.events.TailGrowEvent;

public class ChooChooStatTracker extends StatTracker<Game>
{
	public ChooChooStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onTailGrow(TailGrowEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getLength() >= 60)
			addStat(event.getPlayer(), "ChooChoo", 1, true, false);
	}
}
