package nautilus.game.arcade.stats;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.mission.MissionTrackerType;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class WinWithoutDyingStatTracker extends StatTracker<Game>
{
	private final Set<String> _hasDied = new HashSet<>();
	private final String _stat;

	public WinWithoutDyingStatTracker(Game game, String stat)
	{
		super(game);

		_stat = stat;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (!getGame().IsLive())
			return;

		if (event.GetLog().GetPlayer() == null)
			return;

		if (!event.GetLog().GetPlayer().IsPlayer())
			return;

		Player player = UtilPlayer.searchExact(event.GetLog().GetPlayer().GetName());
		if (player == null || !player.isOnline())
		{
			return;
		}
		_hasDied.add(player.getUniqueId().toString());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.End)
		{
			List<Player> winners = getGame().getWinners();

			if (winners != null)
			{
				for (Player winner : winners)
				{
					if (!_hasDied.contains(winner.getUniqueId().toString()))
					{
						addStat(winner, _stat, 1, true, false);
						getGame().getArcadeManager().getMissionsManager().incrementProgress(winner, 1, MissionTrackerType.GAME_WIN_WITHOUT_DYING, getGame().GetType().getDisplay(), null);
					}
				}
			}
		}
	}

	public String getStat()
	{
		return _stat;
	}
}
