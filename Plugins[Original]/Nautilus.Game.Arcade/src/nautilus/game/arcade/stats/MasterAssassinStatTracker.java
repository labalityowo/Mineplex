package nautilus.game.arcade.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.sneakyassassins.event.PlayerMasterAssassinEvent;

public class MasterAssassinStatTracker extends StatTracker<Game>
{
	public MasterAssassinStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerMasterAssassin(PlayerMasterAssassinEvent event)
	{
		addStat(event.getPlayer(), "MasterAssassin", 1, false, false);
	}
}
