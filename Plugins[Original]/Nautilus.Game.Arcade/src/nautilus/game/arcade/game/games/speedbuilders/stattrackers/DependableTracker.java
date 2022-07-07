package nautilus.game.arcade.game.games.speedbuilders.stattrackers;

import nautilus.game.arcade.game.games.speedbuilders.SpeedBuilders;
import nautilus.game.arcade.game.games.speedbuilders.events.PerfectBuildEvent;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.event.EventHandler;

public class DependableTracker extends StatTracker<SpeedBuilders>
{

	public DependableTracker(SpeedBuilders game)
	{
		super(game);
	}

	@EventHandler
	public void onPerfectBuild(PerfectBuildEvent event)
	{
		addStat(event.getPlayer(), "PerfectBuild", 1, false, false);
	}

}
