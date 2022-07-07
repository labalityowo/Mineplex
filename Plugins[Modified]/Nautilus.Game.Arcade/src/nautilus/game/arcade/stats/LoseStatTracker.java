package nautilus.game.arcade.stats;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;

public class LoseStatTracker extends StatTracker<Game>
{
	private final Set<UUID> _losers = new HashSet<>();

	public LoseStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.End)
		{
			List<Player> losers = getGame().getLosers();

			if (losers != null)
			{
				for (Player loser : losers)
				{
					if (_losers.contains(loser.getUniqueId()))
						continue;

					addStat(loser, "Losses", 1, false, false);

//					if (getGame().GetKit(loser) != null)
//						addStat(loser, getGame().GetKit(loser).getName() + " Losses", 1, false, false);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (getGame().GetState() == Game.GameState.Live && getGame().IsAlive(event.getPlayer()))
		{
			addStat(event.getPlayer(), "Losses", 1, false, false);

			if (getGame().GetKit(event.getPlayer()) != null)
				addStat(event.getPlayer(), getGame().GetKit(event.getPlayer()).GetName() + " Losses", 1, false, false);

			_losers.add(event.getPlayer().getUniqueId());
		}
	}
}
