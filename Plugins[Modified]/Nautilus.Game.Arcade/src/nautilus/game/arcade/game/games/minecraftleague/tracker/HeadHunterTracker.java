package nautilus.game.arcade.game.games.minecraftleague.tracker;

import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.event.EventHandler;

public class HeadHunterTracker extends StatTracker<MinecraftLeague>
{
	public HeadHunterTracker(MinecraftLeague game)
	{
		super(game);
	}

	@EventHandler
	public void build(GrabSkullEvent e)
	{
		addStat(e.getPlayer(), "HeadHunter", 1, false, false);
	}

}