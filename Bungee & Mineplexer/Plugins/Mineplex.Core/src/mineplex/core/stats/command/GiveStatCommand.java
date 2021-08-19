package mineplex.core.stats.command;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.stats.StatsManager;

public class GiveStatCommand extends CommandBase<StatsManager>
{
	public GiveStatCommand(StatsManager plugin)
	{
		super(plugin, StatsManager.Perm.GIVE_STAT_COMMAND, "givestat");
	}

	@Override
	public void Execute(final Player caller, final String[] args)
	{
		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Stats", "/givestat [player] [amount] [stat name]"));
			return;
		}

		Player player = UtilPlayer.searchOnline(caller, args[0], true);

		int amount;

		try
		{
			amount = Integer.parseInt(args[1]);
		}
		catch (NumberFormatException ex)
		{
			UtilPlayer.message(caller, F.main("Stats", F.elem(args[1]) + " is not a number"));
			return;
		}
		
		if (amount < 1)
		{
			UtilPlayer.message(caller, F.main("Stats", "That amount is invalid"));
			return;
		}

		String statName = StringUtils.join(args, " ", 2, args.length);

		if (player == null)
		{
			Plugin.getClientManager().loadClientByName(args[0], client ->
			{
				if (client != null)
				{
					Plugin.incrementStat(client.getAccountId(), statName, amount);
					UtilPlayer.message(caller, F.main("Stats", "Applied " + F.elem(amount + " " + statName) + " to " + F.elem(args[0]) + "."));
				}
				else
				{
					caller.sendMessage(F.main("Stats", "Couldn't find " + args[0] + "'s account!"));
				}
			});
		}
		else
		{
			Plugin.incrementStat(player, statName, amount);
			UtilPlayer.message(caller, F.main("Stats", "Applied " + F.elem(amount + " " + statName) + " to " + F.elem(player.getName()) + "."));
		}
	}
}