package nautilus.game.arcade.game.games.paintball.trackers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.game.games.paintball.Paintball;
import nautilus.game.arcade.game.games.paintball.events.ReviveEvent;
import nautilus.game.arcade.stats.StatTracker;

public class MedicStatTracker extends StatTracker<Paintball>
{
	public MedicStatTracker(Paintball game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(ReviveEvent event)
	{
		if (!getGame().IsLive())
			return;

		addStat(event.getPlayer(), "Medic", 1, false, false);
	}
}
