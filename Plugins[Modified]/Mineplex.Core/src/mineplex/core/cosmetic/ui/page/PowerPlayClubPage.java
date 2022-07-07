package mineplex.core.cosmetic.ui.page;

import java.util.List;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.OutfitGadget;

public class PowerPlayClubPage extends GadgetPage
{

	PowerPlayClubPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Power Play Club Rewards", player);

		buildPage();
	}

	@Override
	protected void toggleGadget(Player player, Gadget gadget)
	{
		// This allows us to have one item for the entire suit in the PowerPlayClubPage
		if (gadget instanceof OutfitGadget)
		{
			toggleSet(player, gadget);
		}
		else
		{
			super.toggleGadget(player, gadget);
		}
	}

	@Override
	protected List<Gadget> getGadgetsToDisplay()
	{
		return getPlugin().getGadgetManager().getPowerPlayGadgets();
	}
}
