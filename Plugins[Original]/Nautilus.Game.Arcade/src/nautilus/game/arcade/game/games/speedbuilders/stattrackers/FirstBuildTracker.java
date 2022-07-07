package nautilus.game.arcade.game.games.speedbuilders.stattrackers;

import org.bukkit.event.EventHandler;

import nautilus.game.arcade.game.games.speedbuilders.SpeedBuilders;
import nautilus.game.arcade.game.games.speedbuilders.events.PerfectBuildEvent;
import nautilus.game.arcade.stats.StatTracker;

public class FirstBuildTracker extends StatTracker<SpeedBuilders>
{

	private boolean _first = true;

	public FirstBuildTracker(SpeedBuilders game)
	{
		super(game);
	}

	@EventHandler
	public void onPerfectBuild(PerfectBuildEvent event)
	{
		if (_first)
		{
			addStat(event.getPlayer(), "PerfectFirst", 1, false, false);
			
			_first = false;
		}
	}

}
