package mineplex.game.clans.shop.building;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.shop.ClansShopItem;
import mineplex.game.clans.shop.ClansShopPage;

public class BuildingPage extends ClansShopPage<BuildingShop>
{
	public BuildingPage(ClansManager plugin, BuildingShop shop, CoreClientManager clientManager, DonationManager donationManager, org.bukkit.entity.Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Building Supplies", player);
		
		buildPage();
	}
	
	@Override
	protected void buildPage()
	{
		addShopItem(1, ClansShopItem.STONE);
		addShopItem(2, ClansShopItem.SMOOTH_BRICK);
		addShopItem(3, ClansShopItem.CRACKED_STONE_BRICK, (byte) 2);
		addShopItem(4, ClansShopItem.COBBLESTONE);
		
		addShopItem(10, ClansShopItem.LOG, (byte) 0);
		addShopItem(11, ClansShopItem.LOG, (byte) 1);
		addShopItem(12, ClansShopItem.LOG, (byte) 2);
		addShopItem(13, ClansShopItem.LOG, (byte) 3);
		addShopItem(14, ClansShopItem.LOG_2, (byte) 0);
		addShopItem(15, ClansShopItem.LOG_2, (byte) 1);
		
		addShopItem(19, ClansShopItem.SAND);
		addShopItem(20, ClansShopItem.GLASS);
		addShopItem(21, ClansShopItem.SANDSTONE);
		addShopItem(22, ClansShopItem.DIRT);
		addShopItem(23, ClansShopItem.NETHER_BRICK);
		addShopItem(24, ClansShopItem.QUARTZ_BLOCK);
		addShopItem(25, ClansShopItem.CLAY);
	}
}
