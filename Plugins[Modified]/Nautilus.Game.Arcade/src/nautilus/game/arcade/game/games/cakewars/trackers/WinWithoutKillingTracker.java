package nautilus.game.arcade.game.games.cakewars.trackers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.stats.StatTracker;

public class WinWithoutKillingTracker extends StatTracker<Game>
{

	private final Set<Player> _hasKilled = new HashSet<>();
	private final String _stat;

	public WinWithoutKillingTracker(Game game, String stat)
	{
		super(game);

		_stat = stat;
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		Player killer = event.getEntity().getKiller();

		if (killer != null)
		{
			_hasKilled.add(killer);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
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
			if (!_hasKilled.contains(winner) && getGame().IsAlive(winner))
			{
				addStat(winner, _stat, 1, true, false);
			}
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_hasKilled.remove(event.getPlayer());
	}

	public String getStat()
	{
		return _stat;
	}
}
