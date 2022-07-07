package nautilus.game.arcade.game.modules.winstreak;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.game.GameDisplay;
import mineplex.core.game.winstreaks.WinStreakManager;
import mineplex.core.stats.PlayerStats;
import mineplex.core.stats.StatsManager;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.modules.Module;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryModule;

public class WinStreakModule extends Module
{

	public static final String BEST_STREAK_STAT = "BestWinStreak";
	private static final String SCRAMBLE_TEXT = C.cGold + C.Scramble + "!";
	static final String BEST_STREAK_TEXT = SCRAMBLE_TEXT + C.cGreenB + " NEW BEST STREAK " + SCRAMBLE_TEXT;

	@Override
	protected void setup()
	{
		getGame().getModule(GameSummaryModule.class)
				.addComponent(new WinStreakSummaryComponent(getGame().getArcadeManager()));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void gameEnd(GameStateChangeEvent event)
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

		winners.forEach(this::incrementStreak);

		for (Player player : UtilServer.getPlayersCollection())
		{
			if (player.isOnline() && !winners.contains(player))
			{
				removeStreak(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerQuit(PlayerQuitEvent event)
	{
		if (!getGame().IsLive())
		{
			return;
		}

		removeStreak(event.getPlayer());
	}

	private void incrementStreak(Player player)
	{
		ArcadeManager manager = getGame().getArcadeManager();

		if (!isEnabled(player))
		{
			return;
		}

		StatsManager statsManager = manager.GetStatsManager();
		PlayerStats stats = statsManager.Get(player);
		WinStreakManager winStreakManager = manager.getWinStreakManager();
		GameDisplay display = getGame().GetType().getDisplay();

		String bestStreakStat = getStat(BEST_STREAK_STAT);
		int streak = manager.getWinStreakManager().getCurrentStreak(player, display);
		long bestStreak = stats.getStat(bestStreakStat);

		// This can happen due to when stats are saved.
		// To ensure bestStreak doesn't fall behind streak we increment the difference here and then
		// never incremented due to the next if statement.
		if (bestStreak < streak)
		{
			statsManager.incrementStat(player, bestStreakStat, streak - bestStreak);
		}

		if (streak == bestStreak)
		{
			statsManager.incrementStat(player, bestStreakStat, 1);
		}

		winStreakManager.incrementWinStreak(player, display);
	}

	private void removeStreak(Player player)
	{
		if (!isEnabled(player))
		{
			return;
		}

		getGame().getArcadeManager().getWinStreakManager().removeWinStreak(player, getGame().GetType().getDisplay());
	}

	private boolean isEnabled(Player player)
	{
		return getGame().getArcadeManager().IsRewardStats() && getGame().isAllowingGameStats() && getGame().getArcadeManager().hasBeenPlaying(player);
	}

	private String getStat(String stat)
	{
		return getGame().GetName() + "." + stat;
	}
}
