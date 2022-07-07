package mineplex.game.clans.shop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.InventoryUtil;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.event.ClansPlayerBuyItemEvent;
import mineplex.game.clans.clans.event.ClansPlayerSellItemEvent;
import mineplex.game.clans.economy.GoldManager;

public class ShopItemButton<T extends ShopPageBase<?, ?>> implements IButton
{
	private int _buyPrice;
	private int _sellPrice;
	private ItemStack _item;
	private T _page;
	
	public ShopItemButton(T page, int buyPrice, int sellPrice, Material material, byte data, int amount, String displayName)
	{
		_page = page;
		_sellPrice = sellPrice;
		_buyPrice = buyPrice;
		_item = new ItemStack(material, amount, data);
		
		if (displayName != null)
		{
			ItemMeta meta = _item.getItemMeta();
			if (meta == null)
			{
				meta = Bukkit.getItemFactory().getItemMeta(material);
			}
			
			meta.setDisplayName(C.Reset + displayName);
			
			_item.setItemMeta(meta);
		}
	}
	
	public ShopItemButton(T page, int buyPrice, int sellPrice, Material material)
	{
		this(page, buyPrice, sellPrice, material, (byte) 0, 1, null);
	}
	
	@Override
	public void onClick(final Player player, ClickType clickType)
	{
		boolean shiftClick = (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT);
		
		if (clickType == ClickType.SHIFT_RIGHT || clickType == ClickType.RIGHT)
		{
			int amount = 1; // # of items removed/sold from inventory
			
			if (!hasItem(player, _item))
			{
				_page.playDenySound(player);
				notify(player, "You do not have any of the appropriate item in your inventory.");
				return;
			}
			
			ClansPlayerSellItemEvent event = new ClansPlayerSellItemEvent(player, _page, ShopItemButton.this, _item, getAmount(player, _item) * _sellPrice, getAmount(player, _item));
			UtilServer.getServer().getPluginManager().callEvent(event);
			
			if (event.isCancelled())
			{
				return;
			}
			
			if (shiftClick)
			{
				amount = InventoryUtil.getCountOfObjectsRemoved((CraftInventory) player.getInventory(), 36, _item);
			}
			else
			{
				InventoryUtil.removeItem((CraftInventory) player.getInventory(), 36, _item);
			}
			
			int reward = amount * _sellPrice;
			
			GoldManager.getInstance().addGold(player, reward);
			GoldManager.notify(player, String.format("You sold %d items for %dg", amount, reward));
			_page.playAcceptSound(player);
		}
		else if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.LEFT)
		{
			final int amount = !(UtilItem.isArmor(_item.getType()) || UtilItem.isTool(_item.getType()) || _item.getType() == Material.BOW) && shiftClick ? 64 : 1;
			final int cost = amount * _buyPrice;
			int goldCount = GoldManager.getInstance().getGold(player);
			
			if (goldCount >= cost)
			{
				final ItemStack eventItem = _item.clone();
				eventItem.setAmount(amount);
				
				GoldManager.getInstance().deductGold(success ->
				{
					if (success)
					{
						ClansPlayerBuyItemEvent event = new ClansPlayerBuyItemEvent(player, _page, ShopItemButton.this, eventItem, cost, amount);
						UtilServer.getServer().getPluginManager().callEvent(event);

						if (event.isCancelled())
						{
							GoldManager.getInstance().addGold(player, cost);
							return;
						}

						_item = event.getItem();
						final int finalCost = event.getCost();
						final int finalAmount = event.getAmount();

						giftItem(player, finalAmount);
						GoldManager.notify(player, String.format("You have purchased %d item(s) for %dg", finalAmount, finalCost));

						_page.playAcceptSound(player);
					}
					else
					{
						GoldManager.notify(player, "You cannot afford that item! Please relog to update your gold count.");
						_page.playDenySound(player);
					}

					_page.refresh();
				}, player, cost);
			}
			else
			{
				GoldManager.notify(player, "You cannot afford that item.");
				_page.playDenySound(player);
			}
		}
		
		_page.refresh();
	}
	
	private boolean hasItem(Player player, ItemStack item)
	{
		return InventoryUtil.first((CraftInventory) player.getInventory(), 36, item, true) != -1;
	}
	
	private int getAmount(Player player, ItemStack item)
	{
		int amount = 0;
		for (ItemStack stack : player.getInventory().getContents())
		{
			if (stack == null)
			{
				continue;
			}
			
			if (stack.equals(item))
			{
				amount++;
			}
		}
		
		return amount;
	}
	
	private void notify(Player player, String message)
	{
		UtilPlayer.message(player, F.main("Shop", message));
	}
	
	private void giftItem(Player player, int amount)
	{
		ItemStack item = _item.clone();
		item.setAmount(amount);
		player.getInventory().addItem(item);
	}
}