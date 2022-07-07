package nautilus.game.arcade.stats;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.turfforts.TurfForts;

public class TheComebackStatTracker extends StatTracker<TurfForts>
{
	private final Set<GameTeam> _hasWentFiveOrBelow = new HashSet<>();

	public TheComebackStatTracker(TurfForts game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onUpdate(UpdateEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getType() == UpdateType.TICK)
		{
			for (GameTeam team : getGame().GetTeamList())
			{
				if (getGame().getLines(team) <= 5)
					_hasWentFiveOrBelow.add(team);
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.End)
		{
			if (_hasWentFiveOrBelow.contains(getGame().WinnerTeam))
			{
				if (getGame().getWinners() != null)
				{
					for (Player player : getGame().getWinners())
						addStat(player, "TheComeback", 1, true, false);
				}
			}
		}
	}
}
