package nautilus.game.arcade.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.minestrike.PlayerHeadshotEvent;

public class HeadShotStatTracker extends StatTracker<Game>
{
	private final String _statName;

	public HeadShotStatTracker(Game game, String statName)
	{
		super(game);

		_statName = statName;
	}

	public String getStatName()
	{
		return _statName;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerHeadshot(PlayerHeadshotEvent event)
	{
		addStat(event.getShooter(), "Headshot", 1, false, false);
	}
}
