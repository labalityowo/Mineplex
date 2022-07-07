package mineplex.game.clans.shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.Clans;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.event.ClansShopAddButtonEvent;

public abstract class ClansShopPage<T extends ShopBase<ClansManager>> extends ShopPageBase<ClansManager, T>
{
	
	/**
	 * Constructor
	 * 
	 * @param plugin
	 * @param shop
	 * @param clientManager
	 * @param donationManager
	 * @param name
	 * @param player
	 * @param slots
	 */
	public ClansShopPage(ClansManager plugin, T shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player, int slots)
	{
		super(plugin, shop, clientManager, donationManager, name, player, slots);
	}
	
	/**
	 * Constructor
	 * 
	 * @param plugin
	 * @param shop
	 * @param clientManager
	 * @param donationManager
	 * @param name
	 * @param player
	 */
	public ClansShopPage(ClansManager plugin, T shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player)
	{
		super(plugin, shop, clientManager, donationManager, name, player);
	}
	
	public void addShopItem(int slot, Material material, int buyPrice, int sellPrice)
	{
		addShopItem(slot, material, buyPrice, sellPrice, Clans.prettifyName(material));
	}
	
	public void addShopItem(int slot, Material material, int buyPrice, int sellPrice, byte data)
	{
		addShopItem(slot, material, buyPrice, sellPrice, data, Clans.prettifyName(material), 1);
	}
	
	public void addShopItem(int slot, Material material, int buyPrice, int sellPrice, String displayName)
	{
		addShopItem(slot, material, buyPrice, sellPrice, (byte) 0, displayName, 1);
	}
	
	public void addShopItem(int slot, Material material, int buyPrice, int sellPrice, byte data, String displayName, int amount)
	{
		ClansShopAddButtonEvent event = new ClansShopAddButtonEvent(getPlayer(), getShop(), slot, material, buyPrice, sellPrice, data, displayName, amount);
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled()) return;
		
		slot = event.getSlot();
		material = event.getMaterial();
		buyPrice = event.getBuyPrice();
		sellPrice = event.getSellPrice();
		data = event.getData();
		displayName = event.getDisplayName();
		amount = event.getAmount();
		
		if (!event.isCancelled())
		{
			PvpItem item = new PvpItem(material, data, 1, displayName, buyPrice, sellPrice, 64);
			addButton(slot, item, new ShopItemButton<ClansShopPage<?>>(this, buyPrice, sellPrice, material, data, amount, displayName));
		}
	}
	
	public void addShopItem(int index, ClansShopItem item, String displayName)
	{
		addShopItem(index, item, (byte) item.getData(), displayName, 1);
	}
	
	public void addShopItem(int index, ClansShopItem item)
	{
		addShopItem(index, item, (byte) item.getData());
	}
	
	public void addShopItem(int index, ClansShopItem item, byte data)
	{
		addShopItem(index, item.getMaterial(), item.getBuyPrice(), item.getSellPrice(), data);
	}
	
	public void addShopItem(int index, ClansShopItem item, byte data, String displayName, int amount)
	{
		addShopItem(index, item.getMaterial(), item.getBuyPrice(), item.getSellPrice(), data, displayName, amount);
	}
}
