package nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.snake.events.SlimeUpgradeEvent;

public class SlimySheepStatTracker extends StatTracker<Game>
{
	private final Map<UUID, Integer> _count = new HashMap<>();

	public SlimySheepStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onSlimeUpgrade(SlimeUpgradeEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		Integer count = _count.get(event.getPlayer().getUniqueId());

		count = (count == null ? 0 : count) + 1;

		_count.put(event.getPlayer().getUniqueId(), count);

		if (count >= 20)
			addStat(event.getPlayer(), "SlimySheep", 1, true, false);
	}
}
