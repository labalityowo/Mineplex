package mineplex.core.cosmetic.ui.page;

import java.util.List;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.types.Gadget;

public class ListGadgetPage extends GadgetPage
{

	private final List<Gadget> _gadgetList;

	ListGadgetPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player, List<Gadget> gadgetList)
	{
		super(plugin, shop, clientManager, donationManager, name, player);

		_gadgetList = gadgetList;
		buildPage();
	}

	@Override
	protected List<Gadget> getGadgetsToDisplay()
	{
		return _gadgetList;
	}
}
