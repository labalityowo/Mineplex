package mineplex.core.cosmetic.ui.page.gamemodifiers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.cosmetic.ui.page.GadgetPage;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticCategory;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.shop.item.ShopItem;

public class GameCosmeticGadgetPage extends GadgetPage
{

	private final GameCosmeticCategoryPage _previousMenu;
	private final GameCosmeticCategory _category;

	public GameCosmeticGadgetPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player, GameCosmeticCategoryPage previousMenu, GameCosmeticCategory category)
	{
		super(plugin, shop, clientManager, donationManager, name, player);

		_previousMenu = previousMenu;
		_category = category;

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		super.buildPage();

		if (_previousMenu != null)
		{
			addButton(4, new ShopItem(Material.BED, C.cGreen + "Go Back", new String[0], 1, false), (player, clickType) -> getShop().openPageForPlayer(getPlayer(), _previousMenu));
		}
		else
		{
			removeButton(4);
		}
	}

	@Override
	protected List<Gadget> getGadgetsToDisplay()
	{
		return new ArrayList<>(_category.getGadgets());
	}
}
