package nautilus.game.arcade.game.games.paintball.trackers;

import java.util.UUID;

import org.bukkit.event.EventHandler;

import mineplex.core.common.util.NautHashMap;
import nautilus.game.arcade.game.games.paintball.Paintball;
import nautilus.game.arcade.game.games.paintball.events.PaintballEvent;
import nautilus.game.arcade.stats.StatTracker;

public class LastStandStatTracker extends StatTracker<Paintball>
{
	private final NautHashMap<UUID, Integer> _kills = new NautHashMap<UUID, Integer>();

	public LastStandStatTracker(Paintball game)
	{
		super(game);
	}

	@EventHandler
	public void onPaintball(PaintballEvent event)
	{
		if (!getGame().IsLive())
			return;

		if (getGame().GetTeam(event.getKiller()).GetPlayers(true).size() == 1)
		{
			if (!_kills.containsKey(event.getKiller().getUniqueId()))
				_kills.put(event.getKiller().getUniqueId(), 0);
			
			int kills = _kills.get(event.getKiller().getUniqueId()) + 1;

			if (kills >= 3)
				addStat(event.getKiller(), "LastStand", 1, true, false);

			_kills.put(event.getKiller().getUniqueId(), kills);
		}
	}
}
