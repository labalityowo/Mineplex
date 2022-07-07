package nautilus.game.arcade.game.games.skywars.trackers;

import org.bukkit.event.EventHandler;

import nautilus.game.arcade.game.games.skywars.Skywars;
import nautilus.game.arcade.game.modules.generator.GeneratorCollectEvent;
import nautilus.game.arcade.stats.StatTracker;

public class TNTStatTracker extends StatTracker<Skywars>
{

	public TNTStatTracker(Skywars game)
	{
		super(game);
	}

	@EventHandler
	public void onTNTPickup(GeneratorCollectEvent event)
	{
		addStat(event.getPlayer(), "BombPickups", 1, false, false);
	}
	
}
