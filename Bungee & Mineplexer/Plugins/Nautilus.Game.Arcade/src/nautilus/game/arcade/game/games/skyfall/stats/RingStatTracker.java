package nautilus.game.arcade.game.games.skyfall.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.skyfall.BoosterRing;
import nautilus.game.arcade.game.games.skyfall.PlayerBoostRingEvent;
import nautilus.game.arcade.stats.StatTracker;

/**
 * StatTracker which collects
 * stats of the amount of {@link BoosterRing}}
 * Players flew trough.
 *
 * @author xXVevzZXx
 */
public class RingStatTracker extends StatTracker<Game>
{

	public RingStatTracker(Game game)
	{
		super(game);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void ring(PlayerBoostRingEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (!getGame().IsAlive(event.getPlayer()))
			return;
		
		addStat(event.getPlayer(), "Rings", 1, false, false);
	}

}
