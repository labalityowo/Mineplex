package nautilus.game.arcade.stats;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;

public class WinWithoutBowStatTracker extends StatTracker<Game>
{
	private final String _statName;

	private final Set<UUID> _hasUsedBow = new HashSet<>();

	public WinWithoutBowStatTracker(Game game, String statName)
	{
		super(game);

		_statName = statName;
	}

	public String getStatName()
	{
		return _statName;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onEntityShootBow(EntityShootBowEvent event)
	{
		if (event.getEntity() instanceof Player)
			_hasUsedBow.add(event.getEntity().getUniqueId());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.End)
		{
			List<Player> winners = getGame().getWinners();

			if (winners != null)
			{
				for (Player winner : winners)
				{
					if (!_hasUsedBow.contains(winner.getUniqueId()))
						addStat(winner, getStatName(), 1, true, false);
				}
			}
		}
	}
}
