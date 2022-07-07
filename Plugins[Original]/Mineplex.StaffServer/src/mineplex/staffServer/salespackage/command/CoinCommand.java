package mineplex.staffServer.salespackage.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.staffServer.salespackage.SalesPackageManager;

public class CoinCommand extends CommandBase<SalesPackageManager>
{
	public CoinCommand(SalesPackageManager plugin)
	{
		super(plugin, SalesPackageManager.Perm.SALES_COMMAND, "coin", "shard", "shards");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args == null || args.length != 2)
			return;
		
		final String playerName = args[0];
		final int amount = Integer.parseInt(args[1]);
		
		Plugin.getClientManager().getOrLoadClient(playerName, client ->
		{
			if (client != null)
			{
				Plugin.getDonationManager().rewardCurrency(GlobalCurrency.TREASURE_SHARD, client, caller.getName(), amount, completed ->
				{
					if (completed)
					{
						caller.sendMessage(F.main(Plugin.getName(), "Added " + amount + " shards to " + playerName + "'s account!"));
					}
					else
					{
						UtilPlayer.message(caller, F.main(Plugin.getName(), "There was an error giving " + F.elem(amount + "Shards") + " to " + F.name(playerName) + "."));
					}
				});
			}
			else
				caller.sendMessage(F.main(Plugin.getName(), "Couldn't find " + playerName + "'s account!"));
		});
	}
}