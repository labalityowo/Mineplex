package mineplex.core.gadget.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.server.util.TransactionResponse;

public class UnlockCosmeticCommand extends CommandBase<GadgetManager>
{

	public UnlockCosmeticCommand(GadgetManager plugin)
	{
		super(plugin, GadgetManager.Perm.UNLOCK_COSMETIC_COMMAND, "unlockCosmetic");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 2)
		{
			caller.sendMessage(F.main(Plugin.getName(), "/" + _aliasUsed + " <player> <cosmetic>"));
			return;
		}

		Plugin.getClientManager().getOrLoadClient(args[0], client ->
		{
			if (client == null)
			{
				caller.sendMessage(F.main(Plugin.getName(), F.name(args[0]) + " does not exist."));
				return;
			}

			StringBuilder cosmetic = new StringBuilder();

			for (int i = 1; i < args.length; i++)
			{
				cosmetic.append(args[i]).append(" ");
			}

			String cosmeticString = cosmetic.toString().trim();

			for (Gadget gadget : Plugin.getAllGadgets())
			{
				boolean thisGadget = gadget.getName().equals(cosmeticString);

				for (String alternative : gadget.getAlternativePackageNames())
				{
					if (alternative.equals(cosmeticString))
					{
						thisGadget = true;
						break;
					}
				}

				if (thisGadget)
				{
					Plugin.getDonationManager().purchaseUnknownSalesPackage(client, cosmeticString, GlobalCurrency.TREASURE_SHARD, 0, true, response ->
					{
						if (response != TransactionResponse.Success)
						{
							caller.sendMessage(F.main(Plugin.getName(), "Failed to unlock " + F.name(cosmeticString) + " for " + F.name(client.getName()) + "."));
						}
						else
						{
							caller.sendMessage(F.main(Plugin.getName(), "Unlocked " + F.name(cosmeticString) + " for " + F.name(client.getName()) + "."));
						}
					});
					return;
				}
			}

			caller.sendMessage(F.main(Plugin.getName(), "The cosmetic " + F.name(cosmeticString) + " does not exist."));
		});
	}
}