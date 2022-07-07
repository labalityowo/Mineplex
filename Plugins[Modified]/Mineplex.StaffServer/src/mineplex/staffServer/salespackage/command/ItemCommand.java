package mineplex.staffServer.salespackage.command;

import java.util.UUID;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.server.util.TransactionResponse;
import mineplex.staffServer.salespackage.SalesPackageManager;

public class ItemCommand extends CommandBase<SalesPackageManager>
{
	public ItemCommand(SalesPackageManager plugin)
	{
		super(plugin, SalesPackageManager.Perm.SALES_COMMAND, "item");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args == null)
			return;

		final String playerName = args[0];
		int amountSpecified = Integer.parseInt(args[1]);
		final String category = args[2];
		String tempName = args[3];

		for (int i = 4; i < args.length; i++)
		{
			tempName += " " + args[i];
		}

		final String itemName = tempName;
		final int amount = amountSpecified;

		if (!Plugin.getInventoryManager().validItem(itemName))
		{
			caller.sendMessage(F.main(Plugin.getName(), "You have entered an invalid Item."));
			return;
		}

		Plugin.getClientManager().loadClientByName(playerName, client ->
		{
			final UUID uuid = Plugin.getClientManager().loadUUIDFromDB(playerName);

			if (uuid != null)
			{
				Plugin.getDonationManager().purchaseUnknownSalesPackage(client, (amount == 1 ? itemName : itemName + " " + amount), GlobalCurrency.GEM, 0, false, data ->
				{
					if (category.equalsIgnoreCase("ITEM"))
					{
						Plugin.getInventoryManager().addItemToInventoryForOffline(new Callback<Boolean>()
						{
							public void run(Boolean success)
							{
								if (success)
								{
									UtilPlayer.message(caller, F.main(Plugin.getName(), playerName + " received " + amount + " " + itemName + "."));
								}
								else
								{
									UtilPlayer.message(caller, F.main(Plugin.getName(), "ERROR processing " + playerName + " " + amount + " " + itemName + "."));
								}
							}
						}, uuid, itemName, amount);
					}
					else
					{
						if (data == TransactionResponse.Success || data == TransactionResponse.AlreadyOwns)
						{
							UtilPlayer.message(caller, F.main(Plugin.getName(), playerName + " received " + amount + " " + itemName + "."));
						}
						else if (data == TransactionResponse.Failed || data == TransactionResponse.InsufficientFunds)
						{
							UtilPlayer.message(caller, F.main(Plugin.getName(), "ERROR processing " + playerName + " " + amount + " " + itemName + "."));
						}
					}
				});
			}
			else
			{
				caller.sendMessage(F.main(Plugin.getName(), "Couldn't find " + playerName + "'s account!"));
			}
		});
	}
}