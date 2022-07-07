package nautilus.game.arcade.game.games.paintball.trackers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.paintball.Paintball;
import nautilus.game.arcade.game.games.paintball.events.PaintballEvent;
import nautilus.game.arcade.stats.StatTracker;

public class KillingSpreeTracker extends StatTracker<Paintball>
{
	private final Map<UUID, Integer> _killCount = new HashMap<>();
	private final Map<UUID, Long> _lastKillTime = new HashMap<>();

	public KillingSpreeTracker(Paintball game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(PaintballEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		Long lastTime = _lastKillTime.get(event.getKiller().getUniqueId());

		long now = System.currentTimeMillis();

		Integer killCount;
		if (lastTime == null || now - lastTime > 5000)
			killCount = 0;
		else
		{
			killCount = _killCount.get(event.getKiller().getUniqueId());
			if (killCount == null)
				killCount = 0;
		}

		killCount++;

		_killCount.put(event.getKiller().getUniqueId(), killCount);
		_lastKillTime.put(event.getKiller().getUniqueId(), now);

		_killCount.remove(event.getPlayer().getUniqueId());
		_lastKillTime.remove(event.getPlayer().getUniqueId());

		if (killCount >= 4)
			addStat(event.getKiller(), "KillingSpree", 1, true, false);
	}
}