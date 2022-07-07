package mineplex.core.cosmetic.ui.page.gamemodifiers.moba;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.cosmetic.ui.page.GadgetPage;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.gadgets.gamemodifiers.moba.skins.HeroSkinGadgetData;
import mineplex.core.shop.item.ShopItem;

public class HeroSkinGadgetPage extends GadgetPage
{

	private final GadgetPage _previousMenu;
	private final List<HeroSkinGadgetData> _gadgetData;

	HeroSkinGadgetPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player, GadgetPage previousMenu, List<HeroSkinGadgetData> gadgetData)
	{
		super(plugin, shop, clientManager, donationManager, name, player);

		_previousMenu = previousMenu;
		_gadgetData = gadgetData;

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		int slot = 10;

		for (HeroSkinGadgetData gadgetData : _gadgetData)
		{
			addGadget(gadgetData.getGadget(), slot);

			if (++slot % 9 == 8)
			{
				slot += 2;
			}
		}

		addButton(4, new ShopItem(Material.BED, C.cGreen + "Go Back", new String[0], 1, false), (player, clickType) -> getShop().openPageForPlayer(getPlayer(), _previousMenu));
	}
}
