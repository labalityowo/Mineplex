package mineplex.core.gadget.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.donation.Donor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.pet.PetType;

public class LockCosmeticsCommand extends CommandBase<GadgetManager>
{

    public LockCosmeticsCommand(GadgetManager plugin)
    {
        super(plugin, GadgetManager.Perm.LOCK_COSMETICS_COMMAND, "lockCosmetics");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		// Adds all cosmetic types
		if (args.length == 0)
		{
			removeCosmetics(null, caller);
		}
		else if (args.length == 1)
		{
			Player player = Bukkit.getPlayer(args[0]);
			if (player != null)
			{
				removeCosmetics(null, player);
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Removed all the cosmetics to " + F.name(player.getName()) + "!"));
			}
		}
	}

	private void removeCosmetics(GadgetType gadgetType, Player caller)
	{
		if (gadgetType == null)
		{
			for (GadgetType type : GadgetType.values())
			{
				removeCosmetics(type, caller);
			}
			removePets(caller);
			return;
		}
		Donor donor = Plugin.getDonationManager().Get(caller);
		int removed = 0;
		for (Gadget gadget : Plugin.getGadgets(gadgetType))
		{
			if (gadget != null)
			{
				int i;
				String[] names = new String[gadget.getAlternativePackageNames().length + 1];
				for (i = 0; i < gadget.getAlternativePackageNames().length; i++)
				{
					names[i] = gadget.getAlternativePackageNames()[i];
				}
				names[i++] = gadget.getName();
				for (String name : names)
				{
					if (donor.ownsUnknownSalesPackage(name))
					{
						donor.removeAllOwnedUnknownSalesPackages(name);
						removed++;
					}
				}
			}
		}
		UtilPlayer.message(caller, F.main(Plugin.getName(), "Removed " + removed + F.elem(" " + gadgetType.getCategoryType()) + UtilText.plural(" item", removed) + "!"));
	}

	private void removePets(Player caller)
	{
		int removed = 0;
		for (PetType pet : PetType.values())
		{
			if (Plugin.getPetManager().Get(caller).getPets().remove(pet) != null)
			{
				removed++;
			}
		}
		UtilPlayer.message(caller, F.main(Plugin.getName(), "Removed " + removed + UtilText.plural(" pet", removed) + "!"));
	}
}