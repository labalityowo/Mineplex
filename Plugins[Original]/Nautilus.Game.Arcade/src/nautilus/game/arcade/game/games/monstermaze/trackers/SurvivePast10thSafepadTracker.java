package nautilus.game.arcade.game.games.monstermaze.trackers;

import nautilus.game.arcade.game.games.monstermaze.MonsterMaze;
import nautilus.game.arcade.game.games.monstermaze.events.SafepadBuildEvent;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class SurvivePast10thSafepadTracker extends StatTracker<MonsterMaze>
{
	/**
	 * @author Mysticate
	 */
			
	public SurvivePast10thSafepadTracker(MonsterMaze game)
	{
		super(game);
	}

	@EventHandler
	public void onSafepadBuild(SafepadBuildEvent event)
	{
		if (!getGame().IsLive())
			return;
			
		if (getGame().getMaze().getCurrentSafePadCount() > 10)
		{
			for (Player player : getGame().GetPlayers(true))
			{
				addStat(player);
			}
		}		
	}
	
	private void addStat(Player player)
	{
		addStat(player, "ToughCompetition", 1, true, false);
	}
}
