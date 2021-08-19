package nautilus.game.arcade.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.kit.perks.PerkSparkler;

public class SparklezStatTracker extends StatTracker<Game>
{
	public SparklezStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onThrowSparkler(PerkSparkler.ThrowSparklerEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		addStat(event.getPlayer(), "Sparklez", 1, false, false);
	}
}
