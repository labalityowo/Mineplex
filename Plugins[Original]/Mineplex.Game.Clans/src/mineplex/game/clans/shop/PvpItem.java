package mineplex.game.clans.shop;

import org.bukkit.Material;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilItem;
import mineplex.core.shop.item.ShopItem;

public class PvpItem extends ShopItem
{
	private static String LEFT_CLICK_BUY = C.cYellow + "Left-Click" + C.cWhite + " to Buy " + C.cGreen + 1;
	private static String RIGHT_CLICK_SELL = C.cYellow + "Right-Click" + C.cWhite + " to Sell " + C.cGreen + 1;
	
	private int _price;
	private int _sellPrice;
	private int _bulkCount;
	
	public PvpItem(Material type, byte data, int displayAmount, String name, int price, int bulkCount)
	{
		super(type, data, name, new String[] { C.cWhite + " ", LEFT_CLICK_BUY, C.cWhite + "Costs " + C.cGreen + price + "g", C.cWhite + " ", C.cYellow + "Shift Left-Click" + C.cWhite + " to Buy " + C.cGreen + bulkCount, C.cWhite + "Costs " + C.cGreen + (price * bulkCount) + "g", C.cWhite + " ", RIGHT_CLICK_SELL, C.cWhite + "Earns " + C.cGreen + (int) (price / 2) + "g", C.cWhite + " ", C.cYellow + "Shift Right-Click" + C.cWhite + " to Sell " + C.cGreen + "All", }, 0, false, false);
		
		_price = price;
		_sellPrice = (int) (price / 2);
		_bulkCount = bulkCount;
	}
	
	public PvpItem(Material type, byte data, int displayAmount, String name, int buyPrice, int sellPrice, int bulkCount)
	{
		super(type, data, name, new String[] {
				C.cWhite + " ",
				LEFT_CLICK_BUY,
				C.cWhite + "Costs " + C.cGreen + (buyPrice == 0 ? "Free (Tutorial)" : buyPrice + "g"),
				C.cWhite + " ",
				UtilItem.isArmor(type) || UtilItem.isTool(type) || type == Material.BOW ? "" : C.cYellow + "Shift Left-Click" + C.cWhite + " to Buy " + C.cGreen + bulkCount,
				UtilItem.isArmor(type) || UtilItem.isTool(type) || type == Material.BOW ? "" : C.cWhite + "Costs " + C.cGreen + (buyPrice * bulkCount) + "g", C.cWhite + " ",
				RIGHT_CLICK_SELL,
				C.cWhite + "Earns " + C.cGreen + (sellPrice == 0 ? "Free" : sellPrice + ""),
				C.cWhite + " ",
				C.cYellow + "Shift Right-Click" + C.cWhite + " to Sell " + C.cGreen + "All",
		}, 0, false, false);
		
		_price = buyPrice;
		_sellPrice = sellPrice;
		_bulkCount = bulkCount;
	}
	
	public PvpItem(Material type, byte data, int displayAmount, String name, int price)
	{
		super(type, data, name, new String[] { C.cWhite + " ", LEFT_CLICK_BUY, C.cWhite + "Costs " + C.cGreen + price + "g", C.cWhite + " ", RIGHT_CLICK_SELL, C.cWhite + "Earns " + C.cGreen + (int) (price / 2) + "g", C.cWhite + " ", C.cYellow + "Shift Right-Click" + C.cWhite + " to Sell " + C.cGreen + "All", }, 0, false, false);
		
		_price = price;
		_bulkCount = -1;
	}
	
	public int getPrice()
	{
		return _price;
	}
	
	public int getSellPrice()
	{
		return _sellPrice;
	}
	
	public int getBulkCount()
	{
		return _bulkCount;
	}
}
