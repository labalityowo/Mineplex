package nautilus.game.arcade.game.modules.winstreak;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.stats.PlayerStats;
import mineplex.core.stats.StatsManager;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponent;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponentType;

public class WinStreakSummaryComponent extends GameSummaryComponent<Pair<Long, Long>>
{

	WinStreakSummaryComponent(ArcadeManager manager)
	{
		super(GameSummaryComponentType.WIN_STREAK, player ->
		{
			String game = manager.GetGame().GetName();
			PlayerStats stats = manager.GetStatsManager().Get(player);

			long streak = manager.getWinStreakManager().getCurrentStreak(player, manager.GetGame().GetType().getDisplay());
			long bestStreak = stats.getStat(game + "." + WinStreakModule.BEST_STREAK_STAT);

			return Pair.create(streak, bestStreak);
		});
	}

	@Override
	public String getMainText(Pair<Long, Long> data)
	{
		long streak = data.getLeft();
		long bestStreak = data.getRight();

		if (streak == 0)
		{
			return null;
		}

		String text = C.mBody + "Win Streak " + F.count(String.valueOf(streak)) + " ";

		if (streak == bestStreak)
		{
			text += WinStreakModule.BEST_STREAK_TEXT;
		}

		return text;
	}

	@Override
	public List<String> getHoverText(Pair<Long, Long> data)
	{
		return Arrays.asList(
				C.cGray + "Current Streak " + F.count(String.valueOf(data.getLeft())),
				C.cGray + "Best Streak " + F.count(String.valueOf(data.getRight()))
		);
	}

	@Override
	public boolean sendMessage(Player player)
	{
		boolean sent = super.sendMessage(player);

		if (sent)
		{
			sendBlank(player);
		}

		return sent;
	}
}