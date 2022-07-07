package mineplex.game.clans.shop.mining;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.shop.ClansShopPage;
import mineplex.game.clans.shop.ClansShopItem;

public class MiningPage extends ClansShopPage<MiningShop>
{
	public MiningPage(ClansManager plugin, MiningShop shop, CoreClientManager clientManager, DonationManager donationManager, org.bukkit.entity.Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Mining Shop", player, 9);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		addShopItem(1, ClansShopItem.IRON_INGOT);
		addShopItem(2, ClansShopItem.GOLD_INGOT);
		addShopItem(3, ClansShopItem.DIAMOND);
		addShopItem(4, ClansShopItem.LEATHER);
		addShopItem(5, ClansShopItem.COAL);
		addShopItem(6, ClansShopItem.REDSTONE);
		addShopItem(7, ClansShopItem.LAPIS_BLOCK);
	}
}
