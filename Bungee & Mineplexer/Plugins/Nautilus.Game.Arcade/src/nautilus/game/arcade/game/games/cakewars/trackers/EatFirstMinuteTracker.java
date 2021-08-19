package nautilus.game.arcade.game.games.cakewars.trackers;

import java.util.concurrent.TimeUnit;

import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilTime;

import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.event.CakeWarsEatCakeEvent;
import nautilus.game.arcade.stats.StatTracker;

public class EatFirstMinuteTracker extends StatTracker<CakeWars>
{

	private static final long TIME = TimeUnit.MINUTES.toMillis(1);

	public EatFirstMinuteTracker(CakeWars game)
	{
		super(game);
	}

	@EventHandler
	public void cakeEat(CakeWarsEatCakeEvent event)
	{
		if (!UtilTime.elapsed(getGame().GetStateTime(), TIME))
		{
			addStat(event.getPlayer(), "Eat1", 1, true, false);
		}
	}
}
