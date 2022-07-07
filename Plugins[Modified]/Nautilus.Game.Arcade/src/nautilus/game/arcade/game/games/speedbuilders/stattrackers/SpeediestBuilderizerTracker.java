package nautilus.game.arcade.game.games.speedbuilders.stattrackers;

import org.bukkit.event.EventHandler;

import nautilus.game.arcade.game.games.speedbuilders.SpeedBuilders;
import nautilus.game.arcade.game.games.speedbuilders.events.PerfectBuildEvent;
import nautilus.game.arcade.stats.StatTracker;

public class SpeediestBuilderizerTracker extends StatTracker<SpeedBuilders>
{
	public SpeediestBuilderizerTracker(SpeedBuilders game)
	{
		super(game);
	}

	@EventHandler
	public void onPerfectBuild(PerfectBuildEvent event)
	{
		if (event.getTimeElapsed() < 10000) // 10 Seconds
			addStat(event.getPlayer(), "SpeediestBuilderizer", 1, true, false);
	}

}
