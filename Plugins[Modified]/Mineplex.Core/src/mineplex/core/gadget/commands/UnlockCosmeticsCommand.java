package mineplex.core.gadget.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.donation.Donor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.pet.PetType;

public class UnlockCosmeticsCommand extends CommandBase<GadgetManager>
{
	
	public UnlockCosmeticsCommand(GadgetManager plugin)
	{
		super(plugin, GadgetManager.Perm.UNLOCK_COSMETICS_COMMAND, "unlockcosmetics", "ulcs");
	}

    @Override
    public void Execute(Player caller, String[] args)
    {
		// Adds all cosmetic types
        if (args.length == 0)
        {
            addCosmetics(null, caller);
        }
        else
		{
			if (args[0].equalsIgnoreCase("all"))
			{
				for (Player player : UtilServer.getPlayers())
				{
					if (player != null)
					{
						addCosmetics(null, player);
						UtilPlayer.message(caller, F.main(Plugin.getName(), "Added all the cosmetics to " + F.name(player.getName()) + "!"));
					}
				}
			}
			else
			{
				for (String arg : args)
				{
					Player player = Bukkit.getPlayer(arg);
					if (player != null)
					{
						addCosmetics(null, player);
						UtilPlayer.message(caller, F.main(Plugin.getName(), "Added all the cosmetics to " + F.name(player.getName()) + "!"));
					}
				}
			}
		}
	}

	private void addCosmetics(GadgetType gadgetType, Player caller)
	{
		if (gadgetType == null)
		{
			for (GadgetType type : GadgetType.values())
			{
				addCosmetics(type, caller);
			}
			addPets(caller);
			return;
		}
		Donor donor = Plugin.getDonationManager().Get(caller);
		int added = 0;
		for (Gadget gadget : Plugin.getGadgets(gadgetType))
		{
			if (gadget.isHidden())
			{
				continue;
			}

			boolean hasGadget = false;
			int i;
			String[] names = new String[gadget.getAlternativePackageNames().length + 1];
			for (i = 0; i < gadget.getAlternativePackageNames().length; i++)
			{
				names[i] = gadget.getAlternativePackageNames()[i];
			}
			names[i] = gadget.getName();
			for (String name : names)
			{
				if (donor.ownsUnknownSalesPackage(name))
				{
					hasGadget = true;
				}
			}
			if (!hasGadget)
			{
				donor.addOwnedUnknownSalesPackage(gadget.getName());
				added++;
			}
		}
		UtilPlayer.message(caller, F.main(Plugin.getName(), "Added " + added + F.elem(" " + gadgetType.getCategoryType()) + UtilText.plural(" item", added) + "!"));
	}

	private void addPets(Player caller)
	{
		int added = 0;
		for (PetType pet : PetType.values())
		{
			if (!Plugin.getPetManager().Get(caller).getPets().containsKey(pet))
			{
				Plugin.getPetManager().Get(caller).getPets().put(pet, pet.getName());
				added++;
			}
		}
		UtilPlayer.message(caller, F.main(Plugin.getName(), "Added " + added + UtilText.plural(" pet", added) + "!"));
	}
}