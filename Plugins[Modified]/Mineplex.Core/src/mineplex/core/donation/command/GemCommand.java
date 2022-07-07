package mineplex.core.donation.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClient;
import mineplex.core.command.CommandBase;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.DonationManager;

public class GemCommand extends CommandBase<DonationManager>
{
	public GemCommand(DonationManager plugin)
	{
		super(plugin, DonationManager.Perm.GEM_COMMAND, "gem");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Gem", "Missing Args: " + F.elem("/gem <player> <amount>")));
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
			UtilPlayer.message(caller, F.main("Gem", "Invalid gems Amount"));
			return;
		}

		if (targetName.equalsIgnoreCase("@a"))
		{
			rewardAllGems(caller, amount);
		}
		else
		{
			Plugin.getClientManager().getOrLoadClient(targetName, client ->
			{
				if (client != null)
				{
					rewardGems(caller, client, amount);
				}
				else
				{
					UtilPlayer.message(caller, F.main("Gem", "Could not find player " + F.name(targetName)));
				}
			});
		}
	}

	private void rewardAllGems(Player caller, int gems)
	{
		if (gems > 1000)
		{
			UtilPlayer.message(caller, F.main("Gem", "You can only give everybody 1000 gems at a time."));
			return;
		}

		for (Player player : UtilServer.getPlayers())
		{
			Plugin.rewardCurrency(GlobalCurrency.GEM, player, caller.getName(), gems);
		}

		UtilPlayer.message(caller, F.main("Gem", "Gave everyone " + F.elem(gems + " gems")));
	}

	private void rewardGems(Player caller, CoreClient target, int gems)
	{
		Plugin.rewardCurrency(GlobalCurrency.GEM, target, caller.getName(), gems, completed ->
		{
			if (completed)
			{
				UtilPlayer.message(caller, F.main("Gem", "You gave " + F.elem(gems + " gems") + " to " + F.name(target.getRealOrDisguisedName()) + "."));

				if (Bukkit.getPlayer(target.getUniqueId()) != null)
				{
					UtilPlayer.message(Bukkit.getPlayer(target.getUniqueId()), F.main("Gem", F.name(caller.getName()) + " gave you " + F.elem(gems + " gems") + "."));
				}
			}
			else
			{
				UtilPlayer.message(caller, F.main("Gem", "There was an error giving " + F.elem(gems + " gems") + " to " + F.name(target.getRealOrDisguisedName()) + "."));
			}
		});
	}
}