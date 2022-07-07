package nautilus.game.arcade.stats;

import java.util.List;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.common.CaptureTheFlag;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class SpecialWinStatTracker extends StatTracker<CaptureTheFlag>
{
	private final String _stat;

	public SpecialWinStatTracker(CaptureTheFlag game, String stat)
	{
		super(game);
		_stat = stat;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.End)
		{
			if (getGame().getScoreDifference() < 5)
				return;
			
			List<Player> winners = getGame().getWinners();
			
			if (winners != null)
			{
				for (Player winner : winners)
					addStat(winner, _stat, 1, false, false);
			}
		}
	}
	
	public String getStat()
	{
		return _stat;
	}

}
