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

public class ShardCommand extends CommandBase<DonationManager>
{
	public ShardCommand(DonationManager plugin)
	{
		super(plugin, DonationManager.Perm.SHARD_COMMAND, "coin", "shard", "shards");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Shards", "Missing Args: " + F.elem("/shard <player> <amount>")));
			return;
		}

		final String targetName = args[0];

		int amount;

		try
		{
			amount = Integer.parseInt(args[1]);
		}
		catch (NumberFormatException ex)
		{
			UtilPlayer.message(caller, F.main("Shards", "Invalid Shards Amount"));
			return;
		}

		if (targetName.equalsIgnoreCase("@a"))
		{
			rewardAllShards(caller, amount);
		}
		else
		{
			Plugin.getClientManager().getOrLoadClient(targetName, client ->
			{
				if (client != null)
				{
					rewardCoins(caller, client, amount);
				}
				else
				{
					UtilPlayer.message(caller, F.main("Shards", "Could not find player " + F.name(targetName)));
				}
			});
		}
	}

	private void rewardAllShards(Player caller, int shards)
	{
		if (shards > 1000)
		{
			UtilPlayer.message(caller, F.main("Shards", "You can only give everybody 1000 shards at a time."));
			return;
		}

		for (Player player : UtilServer.getPlayers())
		{
			Plugin.rewardCurrency(GlobalCurrency.TREASURE_SHARD, player, caller.getName(), shards);
		}
		
		UtilPlayer.message(caller, F.main("Shards", "Gave everyone " + F.elem(shards + " Treasure Shards")));
	}

	private void rewardCoins(Player caller, CoreClient target, int coins)
	{
		Plugin.rewardCurrency(GlobalCurrency.TREASURE_SHARD, target, caller.getName(), coins, completed ->
		{
			if (completed)
			{
				UtilPlayer.message(caller, F.main("Shards", "You gave " + F.elem(coins + " Treasure Shards") + " to " + F.name(target.getRealOrDisguisedName()) + "."));

				if (Bukkit.getPlayer(target.getUniqueId()) != null)
				{
					UtilPlayer.message(Bukkit.getPlayer(target.getUniqueId()), F.main("Shards", F.name(caller.getName()) + " gave you " + F.elem(coins + " Treasure Shards") + "."));
				}
			}
			else
			{
				UtilPlayer.message(caller, F.main("Shards", "There was an error giving " + F.elem(coins + "Treasure Shards") + " to " + F.name(target.getRealOrDisguisedName()) + "."));
			}
		});
	}
}