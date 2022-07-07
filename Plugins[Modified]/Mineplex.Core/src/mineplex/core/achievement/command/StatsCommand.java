package mineplex.core.achievement.command;

import mineplex.core.incognito.IncognitoManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.achievement.AchievementManager;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class StatsCommand extends CommandBase<AchievementManager>
{
	public StatsCommand(AchievementManager plugin)
	{
		super(plugin, AchievementManager.Perm.STATS_COMMAND, "stats");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			Plugin.openShop(caller);
		}
		else
		{
			Player target = UtilPlayer.searchOnline(caller, args[0], true);

			if (target == null)
			{
				if (Plugin.getClientManager().Get(caller).hasPermission(AchievementManager.Perm.SEE_FULL_STATS))
					attemptOffline(caller, args);
				return;
			}

			// Don't allow players to use this command on vanished staff members
			// Staff can use it on other vanished staff, though.
			if (Plugin.getIncognito() != null && Plugin.getIncognito().Get(target).Status && !Plugin.getClientManager().Get(caller).hasPermission(AchievementManager.Perm.SEE_FULL_STATS))
			{
				UtilPlayer.messageSearchOnlineResult(caller, args[0], 0);
				return;
			}

			Plugin.openShop(caller, target);
		}
	}

	private void attemptOffline(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			Plugin.openShop(caller);
		}
		else
		{
			UtilPlayer.message(caller, F.main("Stats", "Attempting to look up offline stats..."));
			final String playerName = args[0];

			Plugin.getStatsManager().getOfflinePlayerStats(args[0], stats ->
			{
				if (stats == null)
				{
					UtilPlayer.message(caller, F.main("Stats", "Offline Player " + F.elem(playerName) + " not found."));
				}
				else
				{
					Plugin.openShop(caller, playerName, stats);
				}
			});
		}
	}
}