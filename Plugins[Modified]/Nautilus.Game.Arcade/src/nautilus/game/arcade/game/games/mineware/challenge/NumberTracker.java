package nautilus.game.arcade.game.games.mineware.challenge;

import org.bukkit.entity.Player;

/**
 * Tracks number related data used for statistic trackers.
 */
public interface NumberTracker
{
	public Number getData(Player player);
	
	public boolean hasData(Player player);
}
