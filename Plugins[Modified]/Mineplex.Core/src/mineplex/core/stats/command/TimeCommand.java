package mineplex.core.stats.command;

import java.util.function.Consumer;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.stats.PlayerStats;
import mineplex.core.stats.StatsManager;

public class TimeCommand extends CommandBase<StatsManager>
{
	public TimeCommand(StatsManager plugin)
	{
		super(plugin, StatsManager.Perm.TIME_COMMAND, "time");
	}

	@Override
	public void Execute(final Player caller, final String[] args)
	{
		if (args.length == 0)
		{
			UtilPlayer.message(caller, F.main("Time", "/time [player]"));
			return;
		}

		Player target = UtilPlayer.searchOnline(caller, args[0], true);

		Consumer<PlayerStats> statsConsumer = stats ->
		{
			if (stats == null)
			{
				UtilPlayer.message(caller, F.main("Time", "Player " + F.elem(args[0]) + " not found!"));
			}
			else
			{
				long time = stats.getStat("Global.TimeInGame");
				UtilPlayer.message(caller, F.main("Time", F.name(args[0]) + " has spent " + F.elem(UtilTime.convertString(time * 1000L, 1, UtilTime.TimeUnit.FIT)) + " in game"));
			}
		};

		if (target == null)
		{
			Plugin.getOfflinePlayerStats(args[0], statsConsumer);
		}
		else
		{
			statsConsumer.accept(Plugin.Get(target));
		}
	}
}