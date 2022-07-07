package nautilus.game.arcade.game.games.cakewars.trackers;

import org.bukkit.event.EventHandler;

import nautilus.game.arcade.events.FirstBloodEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.stats.StatTracker;

public class FirstBloodStatTracker extends StatTracker<Game>
{

	public FirstBloodStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler
	public void firstBlood(FirstBloodEvent event)
	{
		addStat(event.getPlayer(), "FirstBlood", 1, true, false);
	}
}
