package nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.mission.MissionTrackerType;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.hideseek.HideSeek;

public class MeowStatTracker extends StatTracker<Game>
{
	private final Map<UUID, Integer> _meowCount = new HashMap<>();

	public MeowStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onMeow(HideSeek.MeowEvent event)
	{
		if (!getGame().IsLive())
		{
			return;
		}

		Integer meows = _meowCount.get(event.getPlayer().getUniqueId());

		meows = (meows == null ? 0 : meows) + 1;

		_meowCount.put(event.getPlayer().getUniqueId(), meows);

		if (meows >= 50)
		{
			addStat(event.getPlayer(), "Meow", 1, true, false);
		}

		getGame().getArcadeManager().getMissionsManager().incrementProgress(event.getPlayer(), 1, MissionTrackerType.BLOCK_HUNT_MEOW, getGame().GetType().getDisplay(), null);
	}
}
