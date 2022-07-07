package mineplex.game.clans.shop.bank;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.DisplayButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.economy.GoldManager;
import mineplex.game.clans.shop.ClansShopItem;

public class BankPage extends ShopPageBase<ClansManager, BankShop>
{
	public static final int GEM_CONVERSION = 1000;	// The number of gems that can be converted into gold
	public static final int TOKEN_VALUE = ClansShopItem.GOLD_TOKEN.getBuyPrice();	// Value of a GoldToken (in gold) that can be stored/cashed in here
	private ClanInfo _clanInfo;

	public BankPage(ClansManager plugin, BankShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Bank Shop", player, 9);
		_clanInfo = getPlugin().getClan(getPlayer());

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		buildCashIn();
		
		if (hasEnoughGold())
		{
			buildTokenPurchasable();
		}
		else
		{
			buildTokenUnpurchasable();
		}
		
		if (GoldManager.getInstance().canTransferGems(getPlayer()))
		{
			buildTransferGems();
		}
		else
		{
			buildTransferGemsCooldown();
		}
	}

	private void buildCashIn()
	{
		int playerGold = getPlayerGold();
		
		CashInButton button = new CashInButton(this);
		String title = "Cash In Gold Token";
		String playerGoldString = ChatColor.RESET + F.value("Your Gold", playerGold + "g");
		String description = ChatColor.RESET + C.cWhite + "Click with GoldToken to exchange for gold!";
		ShopItem shopItem = new ShopItem(Material.FURNACE, title, new String[] {" ", playerGoldString, description}, 0, true, true);
		addButton(5, shopItem, button);
	}
	
	private void buildTransferGems()
	{
		int playerGold = getPlayerGold();
		int playerGems = getPlayerGems();
		int conversionCount = (int) (GEM_CONVERSION * GoldManager.GEM_CONVERSION_RATE);

		GemTransferButton button = new GemTransferButton(this, GEM_CONVERSION);
		String title = ChatColor.GOLD + C.Bold + "Convert Gems To Gold!";
		String playerGoldString = ChatColor.RESET + F.value("Your Gold", playerGold + "g");
		String playerGemString = ChatColor.RESET + F.value("Your Gems", playerGems + " gems");
		String purchaseString = ChatColor.RESET + F.value("Conversion Rate", GEM_CONVERSION + " gems for " + conversionCount + " gold");
		String goldString = ChatColor.RESET + C.cWhite + "Convert gems into gold coins once per day!";
		ShopItem shopItem = new ShopItem(Material.EMERALD, title, new String[] {" ", playerGoldString, playerGemString, purchaseString, goldString}, 0, true, true);
		addButton(4, shopItem, button);
	}
	
	private void buildTransferGemsCooldown()
	{
		DisplayButton button = new DisplayButton();
		String title = ChatColor.RED + C.Bold + "Conversion Cooldown!";
		String purchaseString = ChatColor.RESET + C.cWhite + "You have already converted gems into coins today";
		ShopItem shopItem = new ShopItem(Material.REDSTONE_BLOCK, title, new String[] {" ", purchaseString, " "}, 0, true, true);
		addButton(4, shopItem, button);
	}

	private void buildTokenPurchasable()
	{
		int playerGold = getPlayerGold();

		StoreGoldButton button = new StoreGoldButton(this);
		String title = ChatColor.GOLD + C.Bold + "Purchase Gold Token!";
		String playerGoldString = ChatColor.RESET + F.value("Your Gold", playerGold + "g");
		String purchaseString = ChatColor.RESET + F.value("Token Value", TOKEN_VALUE + "g");
		String goldString = ChatColor.RESET + C.cWhite + "Store your bank gold in the form of a gold token!";
		ShopItem shopItem = new ShopItem(Material.RABBIT_FOOT, title, new String[] {" ", playerGoldString, purchaseString, goldString}, 0, true, true);
		addButton(3, shopItem, button);
	}
	
	private void buildTokenUnpurchasable()
	{
		int playerGold = getPlayerGold();
		int goldCost = TOKEN_VALUE;

		DisplayButton button = new DisplayButton();
		String title = ChatColor.RED + C.Bold + "Missing Gold!";
		String playerGoldString = ChatColor.RESET + F.value("Your Gold", playerGold + "g");
		String purchaseString = ChatColor.RESET + C.cWhite + "You don't have enough gold";
		String goldString = ChatColor.RESET + C.cWhite + "You need " + C.cYellow + goldCost + C.cWhite + " gold to purchase a token.";
		ShopItem shopItem = new ShopItem(Material.RABBIT_FOOT, title, new String[] {" ", playerGoldString, purchaseString, goldString}, 0, true, true);
		addButton(3, shopItem, button);
	}

	public boolean hasEnoughGold()
	{
		return getPlayerGold() >= TOKEN_VALUE;
	}
	
	private int getPlayerGems()
	{
		return GoldManager.getInstance().getGems(getPlayer());
	}

	private int getPlayerGold()
	{
		return GoldManager.getInstance().getGold(getPlayer());
	}
}
