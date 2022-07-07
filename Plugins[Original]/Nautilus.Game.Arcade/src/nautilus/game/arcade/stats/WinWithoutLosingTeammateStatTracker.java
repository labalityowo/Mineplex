package nautilus.game.arcade.stats;

import nautilus.game.arcade.events.*;
import nautilus.game.arcade.game.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

public class WinWithoutLosingTeammateStatTracker extends StatTracker<TeamGame>
{
	private final String _stat;

	public WinWithoutLosingTeammateStatTracker(TeamGame game, String stat)
	{
		super(game);
		_stat = stat;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.End)
		{
			GameTeam winner = getGame().WinnerTeam;

			if (winner == null)
				return;
			
			if (winner.GetPlayers(false).size() < 4)
				return;
			
			if (winner.GetPlayers(true).size() == winner.GetPlayers(false).size())
			{
				for (Player player : winner.GetPlayers(true))
					addStat(player, getStat(), 1, true, false);
			}
		}
	}

	public String getStat()
	{
		return _stat;
	}
}
