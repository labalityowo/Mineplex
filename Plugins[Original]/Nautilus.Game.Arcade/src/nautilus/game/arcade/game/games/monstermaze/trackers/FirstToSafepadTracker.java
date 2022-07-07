package nautilus.game.arcade.game.games.monstermaze.trackers;

import nautilus.game.arcade.game.games.monstermaze.MonsterMaze;
import nautilus.game.arcade.game.games.monstermaze.events.FirstToSafepadEvent;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class FirstToSafepadTracker extends StatTracker<MonsterMaze>
{
	/**
	 * @author Mysticate
	 */
		
	public FirstToSafepadTracker(MonsterMaze game)
	{
		super(game);
	}

	@EventHandler
	public void onSafepadFirst(FirstToSafepadEvent event)
	{
		if (!getGame().IsLive())
			return;
		
		addStat(event.getPlayer());
	}
	
	private void addStat(Player player)
	{
		addStat(player, "Speed", 1, false, false);
	}
}
