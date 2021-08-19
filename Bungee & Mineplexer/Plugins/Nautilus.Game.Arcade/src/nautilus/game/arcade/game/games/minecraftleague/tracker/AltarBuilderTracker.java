package nautilus.game.arcade.game.games.minecraftleague.tracker;

import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.event.EventHandler;

public class AltarBuilderTracker extends StatTracker<MinecraftLeague>
{
	public AltarBuilderTracker(MinecraftLeague game)
	{
		super(game);
	}

	@EventHandler
	public void build(PlaceSkullEvent e)
	{
		addStat(e.getPlayer(), "AltarBuilder", 1, false, false);
	}

}