package nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import nautilus.game.arcade.game.Game;

public class TimeInGameStatTracker extends StatTracker<Game>
{
	private final HashMap<UUID, Long> _joinTimes = new HashMap<>();

	public TimeInGameStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		_joinTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Long joinTime = _joinTimes.remove(event.getPlayer().getUniqueId());

		if (joinTime != null)
		{
			int timeInGame = (int) ((System.currentTimeMillis() - joinTime) / 1000);
			addStat(event.getPlayer(), "TimeInGame", timeInGame, false, true);
		}
	}
}
