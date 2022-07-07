package mineplex.game.clans.shop.pvp;

import org.bukkit.Material;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.game.clans.Clans;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.shop.ClansShopItem;
import mineplex.game.clans.shop.ClansShopPage;
import mineplex.game.clans.shop.pvp.tnt.TNTGenShop;

public class PvpPage extends ClansShopPage<PvpShop>
{
	public PvpPage(ClansManager plugin, PvpShop shop, CoreClientManager clientManager, DonationManager donationManager, org.bukkit.entity.Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Pvp Gear", player);
		
		buildPage();
	}
	
	@Override
	protected void buildPage()
	{
		addShopItem(9, ClansShopItem.GOLD_HELMET, "Mage Helmet");
		addShopItem(18, ClansShopItem.GOLD_CHESTPLATE, "Mage Chestplate");
		addShopItem(27, ClansShopItem.GOLD_LEGGINGS, "Mage Leggings");
		addShopItem(36, ClansShopItem.GOLD_BOOTS, "Mage Boots");
		
		addShopItem(10, ClansShopItem.LEATHER_HELMET, "Assassin Helmet");
		addShopItem(19, ClansShopItem.LEATHER_CHESTPLATE, "Assassin Chestplate");
		addShopItem(28, ClansShopItem.LEATHER_LEGGINGS, "Assassin Leggings");
		addShopItem(37, ClansShopItem.LEATHER_BOOTS, "Assassin Boots");
		
		addShopItem(11, ClansShopItem.CHAINMAIL_HELMET, "Ranger Helmet");
		addShopItem(20, ClansShopItem.CHAINMAIL_CHESTPLATE, "Ranger Chestplate");
		addShopItem(29, ClansShopItem.CHAINMAIL_LEGGINGS, "Ranger Leggings");
		addShopItem(38, ClansShopItem.CHAINMAIL_BOOTS, "Ranger Boots");
		
		addShopItem(12, ClansShopItem.IRON_HELMET, "Knight Helmet");
		addShopItem(21, ClansShopItem.IRON_CHESTPLATE, "Knight Chestplate");
		addShopItem(30, ClansShopItem.IRON_LEGGINGS, "Knight Leggings");
		addShopItem(39, ClansShopItem.IRON_BOOTS, "Knight Boots");
		
		addShopItem(13, ClansShopItem.DIAMOND_HELMET, "Brute Helmet");
		addShopItem(22, ClansShopItem.DIAMOND_CHESTPLATE, "Brute Chestplate");
		addShopItem(31, ClansShopItem.DIAMOND_LEGGINGS, "Brute Leggings");
		addShopItem(40, ClansShopItem.DIAMOND_BOOTS, "Brute Boots");
		
		addShopItem(15, ClansShopItem.IRON_SWORD, "Iron Sword");
		addShopItem(16, ClansShopItem.DIAMOND_SWORD, "Power Sword");
		addShopItem(17, ClansShopItem.GOLD_SWORD,  "Booster Sword");
		
		addShopItem(24, ClansShopItem.IRON_AXE, "Iron Axe");
		addShopItem(25, ClansShopItem.DIAMOND_AXE,  "Power Axe");
		addShopItem(26, ClansShopItem.GOLD_AXE, "Booster Axe");
		
		addShopItem(33, ClansShopItem.BOW,  "Standard Bow");
		addShopItem(34, ClansShopItem.ARROW, (byte) 0, "Arrows", 16);
		
		addButton(51 - 9, new ItemBuilder(Material.IRON_BARDING).setTitle(C.cGreenB + "Standard Mount").setLore(C.cRed, C.cYellow + "Left-Click" + C.cWhite + " to buy " + C.cGreen + "1", C.cWhite + "Costs " + C.cGreen + "150000g").build(), new MountBuyButton<>(this));
		
		if (Clans.HARDCORE)
		{
			addShopItem(52 - 9, ClansShopItem.CANNON, C.cBlue + "Cannon");
			addShopItem(53 - 9, ClansShopItem.OUTPOST, C.cBlue + "Outpost");
		}
		
		addShopItem(51, ClansShopItem.ENCHANTMENT_TABLE, "Class Shop");
		
		if (Clans.HARDCORE)
		{
			addShopItem(52, ClansShopItem.TNT, "TNT");
			addButton(53, new ItemBuilder(ClansShopItem.TNT_GENERATOR.getMaterial()).setTitle(C.cAqua + "Manage TNT Generator").build(), (player, click) ->
				new TNTGenShop(_plugin, _clientManager, _donationManager).attemptShopOpen(player)
			);
		}
	}
}