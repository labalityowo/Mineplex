package nautilus.game.arcade.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.draw.Draw;
import nautilus.game.arcade.game.games.draw.DrawGuessCorrectlyEvent;

public class MrSquiggleStatTracker extends StatTracker<Draw>
{
	private final Map<UUID, List<Long>> _guessTimes = new HashMap<>();

	public MrSquiggleStatTracker(Draw game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onDrawGuessCorrectly(DrawGuessCorrectlyEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getRank() != 1)
			return;

		Player drawer = event.getDrawRound().getDrawer();

		List<Long> times = _guessTimes.get(drawer.getUniqueId());
		if (times == null)
		{
			times = new ArrayList<>();
			_guessTimes.put(drawer.getUniqueId(), times);
		}

		times.add(System.currentTimeMillis() - event.getDrawRound().Time);

		if (times.size() >= 2)
		{
			boolean greaterThan15000 = false;

			for (long time : times)
			{
				if (time > 15000)
				{
					greaterThan15000 = true;

					break;
				}
			}

			if (!greaterThan15000)
				addStat(drawer, "MrSquiggle", 1, true, false);
		}
	}
}
