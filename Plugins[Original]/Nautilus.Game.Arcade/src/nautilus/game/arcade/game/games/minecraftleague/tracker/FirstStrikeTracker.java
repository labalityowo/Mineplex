package nautilus.game.arcade.game.games.minecraftleague.tracker;

import nautilus.game.arcade.events.FirstBloodEvent;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.event.EventHandler;

public class FirstStrikeTracker extends StatTracker<MinecraftLeague>
{
	public FirstStrikeTracker(MinecraftLeague game)
	{
		super(game);
	}

	@EventHandler
	public void blood(FirstBloodEvent e)
	{
		addStat(e.getPlayer(), "FirstStrike", 1, false, false);
	}

}