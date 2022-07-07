package nautilus.game.arcade.stats;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.kit.perks.event.PerkLeapEvent;

public class ParalympicsStatTracker extends StatTracker<Game>
{

	private final Set<UUID> _hasLeaped = new HashSet<>();

	public ParalympicsStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPerkLeap(PerkLeapEvent event)
	{
		if (!getGame().IsLive())
		{
			return;
		}

		_hasLeaped.add(event.GetPlayer().getUniqueId());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.End)
		{
			return;
		}

		List<Player> winners = getGame().getWinners();

		if (winners == null)
		{
			return;
		}

		for (Player winner : winners)
		{
			if (!_hasLeaped.contains(winner.getUniqueId()))
			{
				addStat(winner, "Paralympics", 1, true, false);
			}
		}
	}
}

