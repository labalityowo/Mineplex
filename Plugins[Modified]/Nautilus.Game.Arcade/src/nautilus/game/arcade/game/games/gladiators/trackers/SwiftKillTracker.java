package nautilus.game.arcade.game.games.gladiators.trackers;

import org.bukkit.event.EventHandler;

import nautilus.game.arcade.events.FirstBloodEvent;
import nautilus.game.arcade.game.games.gladiators.Gladiators;
import nautilus.game.arcade.stats.StatTracker;

/**
 * Created by William (WilliamTiger).
 * 10/12/15
 */
public class SwiftKillTracker extends StatTracker<Gladiators>
{
	public SwiftKillTracker(Gladiators game)
	{
		super(game);
	}

	@EventHandler
	public void blood(FirstBloodEvent e)
	{
		addStat(e.getPlayer(), "SwiftKill", 1, false, false);
	}

}