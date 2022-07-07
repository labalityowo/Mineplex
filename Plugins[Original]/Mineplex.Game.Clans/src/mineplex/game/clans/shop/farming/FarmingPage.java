package mineplex.game.clans.shop.farming;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.shop.ClansShopPage;
import mineplex.game.clans.shop.ClansShopItem;

public class FarmingPage  extends ClansShopPage<FarmingShop>
{
	public FarmingPage(ClansManager plugin, FarmingShop shop, CoreClientManager clientManager, DonationManager donationManager, org.bukkit.entity.Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Organic Produce", player, 18);
		
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		addShopItem(1, ClansShopItem.POTATO_ITEM);
		addShopItem(2, ClansShopItem.MELON);
		addShopItem(3, ClansShopItem.BREAD);
		addShopItem(4, ClansShopItem.COOKED_BEEF);
		addShopItem(5, ClansShopItem.GRILLED_PORK);
		addShopItem(6, ClansShopItem.COOKED_CHICKEN);
		addShopItem(7, ClansShopItem.FEATHER);
		addShopItem(8, ClansShopItem.CARROT_ITEM);
		addShopItem(10, ClansShopItem.MUSHROOM_SOUP);
		addShopItem(11, ClansShopItem.SUGAR_CANE);
		addShopItem(12, ClansShopItem.PUMPKIN);
		addShopItem(13, ClansShopItem.STRING);
		addShopItem(14, ClansShopItem.BONE);
		addShopItem(15, ClansShopItem.ROTTEN_FLESH);
		addShopItem(16, ClansShopItem.SPIDER_EYE);
	}
}
