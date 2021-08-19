package mineplex.core.donation.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.DonationManager;

public class CrownCommand extends CommandBase<DonationManager>
{
	public CrownCommand(DonationManager plugin)
	{
		super(plugin, DonationManager.Perm.CROWN_COMMAND, "crown");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Crown", "Missing Args: " + F.elem("/crown <player> <amount>")));
			return;
		}

		String targetName = args[0];

		int amount;

		try
		{
			amount = Integer.parseInt(args[1]);
		}
		catch (NumberFormatException ex)
		{
			UtilPlayer.message(caller, F.main("Crown", "Invalid crown Amount"));
			return;
		}

		if (targetName.equalsIgnoreCase("@a"))
		{
			rewardAllCrowns(caller, amount);
		}
		else
		{
			Player target = UtilPlayer.searchExact(targetName);
			if (target != null)
			{
				rewardCrowns(caller, target, amount);
			}
			else
			{
				UtilPlayer.message(caller, F.main("Crown", "Could not find player " + F.name(targetName)));
			}
		}
	}

	private void rewardAllCrowns(Player caller, int crowns)
	{
		if (crowns > 1000)
		{
			UtilPlayer.message(caller, F.main("Crown", "You can only give everybody 1000 crowns at a time."));
			return;
		}

		for (Player player : UtilServer.getPlayers())
		{
			Plugin.rewardCrowns(crowns, player);
		}

		UtilPlayer.message(caller, F.main("Crown", "Gave everyone " + F.elem(crowns + " crowns")));
	}

	private void rewardCrowns(Player caller, Player target, int crowns)
	{
		Plugin.rewardCrowns(crowns, target, completed ->
		{
			if (completed)
			{
				UtilPlayer.message(caller, F.main("Crown", "You gave " + F.elem(crowns + " crowns") + " to " + F.name(target.getName()) + "."));
				UtilPlayer.message(target, F.main("Crown", F.name(caller.getName()) + " gave you " + F.elem(crowns + " crowns") + "."));
			}
			else
			{
				UtilPlayer.message(caller, F.main("Crown", "There was an error giving " + F.elem(crowns + " crowns") + " to " + F.name(target.getName()) + "."));
			}
		});
	}
}