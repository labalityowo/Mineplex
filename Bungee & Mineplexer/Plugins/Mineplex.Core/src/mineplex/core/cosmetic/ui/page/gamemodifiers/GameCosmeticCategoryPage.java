package mineplex.core.cosmetic.ui.page.gamemodifiers;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilUI;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.cosmetic.ui.page.GadgetPage;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticCategory;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticType;
import mineplex.core.gadget.types.GameModifierGadget;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.item.ShopItem;

public class GameCosmeticCategoryPage extends GadgetPage
{

	private final List<GameCosmeticCategory> _categories;

	public GameCosmeticCategoryPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player, GameCosmeticType type, List<GameCosmeticCategory> categories)
	{
		super(plugin, shop, clientManager, donationManager, type.getName(), player, null);

		_categories = categories;

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		int[] slots = UtilUI.getIndicesFor(_categories.size(), 2);
		int index = 0;

		for (GameCosmeticCategory category : _categories)
		{
			int own = 0;
			int total = 0;
			for (GameModifierGadget gadget : category.getGadgets())
			{
				if (gadget.ownsGadget(getPlayer()))
				{
					own++;
				}
				total++;
			}

			ItemBuilder builder = new ItemBuilder(category.getItemStack());

			builder.setTitle(C.cGreenB + category.getCategoryName());
			builder.addLore(
					"",
					C.cWhite + "You own " + own + "/" + total,
					"",
					C.cGreen + "Left-Click to view " + category.getCategoryName() + " cosmetics"
			);

			GadgetPage page = category.getGadgetPage(this);

			addButton(slots[index++], builder.build(), (player, clickType) ->
			{
				if (page != null)
				{
					getShop().openPageForPlayer(player, page);
				}
			});
		}

		addButton(4, new ShopItem(Material.BED, C.cGreen + "Go Back", new String[0], 1, false), (player, clickType) -> getShop().openPageForPlayer(getPlayer(), new GameCosmeticsPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), player)));
	}
}
