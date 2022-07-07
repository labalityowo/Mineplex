package mineplex.staffServer.salespackage.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.staffServer.salespackage.SalesPackageManager;

public class GemHunterCommand extends CommandBase<SalesPackageManager>
{
	public GemHunterCommand(SalesPackageManager plugin)
	{
		super(plugin, SalesPackageManager.Perm.SALES_COMMAND, "gemhunter");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args == null || args.length != 2)
			return;
		
		final String playerName = args[0];
		final int amount = Integer.parseInt(args[1]);
		int tempExp = 0;

		if (amount == 4)
			tempExp = 70000;
		else if (amount == 8)
			tempExp = 220000;
		
		final long experience = tempExp;
		
		Plugin.getClientManager().loadClientByName(playerName, client ->
		{
			if (client != null)
			{
				Plugin.getDonationManager().purchaseUnknownSalesPackage(client, "Gem Hunter Level " + amount, GlobalCurrency.GEM, 0, false, null);
				Plugin.getStatsManager().incrementStat(client.getAccountId(), "Global.GemsEarned", experience);
				caller.sendMessage(F.main(Plugin.getName(), "Added Level " + amount + " Gem Hunter to " + playerName + "'s account!"));
			}
			else
			{
				caller.sendMessage(F.main(Plugin.getName(), "Couldn't find " + playerName + "'s account!"));
			}
		});
	}
}