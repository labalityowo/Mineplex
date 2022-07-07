package mineplex.gemhunters.shop;

import mineplex.core.Managers;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.gemhunters.economy.EconomyModule;
import mineplex.gemhunters.util.SimpleNPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SellingNPC extends SimpleNPC
{

	private static final ItemStack CANCEL = new ItemBuilder(Material.REDSTONE_BLOCK)
			.setTitle(C.cRedB + "Cancel")
			.addLore("", "Click to cancel and return your items.")
			.build();
	private static final ItemStack BUFFER = new ItemBuilder(Material.STAINED_GLASS_PANE, (byte) 15)
			.setTitle(" ")
			.build();

	private final EconomyModule _economy;

	private final Set<TradeableItem> _selling;
	private final Map<Player, Inventory> _inv;

	private int _total;

	public SellingNPC(JavaPlugin plugin, Location spawn, Class<? extends LivingEntity> type, String name, boolean vegetated, Set<TradeableItem> selling)
	{
		super(plugin, spawn, type, name, null, vegetated);

		_economy = Managers.require(EconomyModule.class);

		_selling = selling;
		_inv = new HashMap<>();
	}

	@Override
	@EventHandler
	public void npcClick(PlayerInteractEntityEvent event)
	{
		super.npcClick(event);

		if (event.getRightClicked().equals(_entity))
		{
			event.setCancelled(true);

			Player player =  event.getPlayer();
			Inventory inv = UtilServer.getServer().createInventory(null, 54, _entity.getCustomName());

			inv.setItem(0, CANCEL);
			inv.setItem(8, getConfirm());

			for (int i = 9; i < 18; i++)
			{
				inv.setItem(i, BUFFER);
			}

			_inv.put(player, inv);
			player.openInventory(inv);
		}
	}


	@EventHandler
	public void inventoryClick(InventoryClickEvent event)
	{
		if (event.getInventory() == null)
		{
			return;
		}

		Player player = (Player) event.getWhoClicked();
		Inventory inv = _inv.get(player);

		if (inv == null | !event.getInventory().equals(inv))
		{
			return;
		}

		ItemStack itemStack = event.getCurrentItem();
		ItemStack cursor = event.getCursor();

		if (itemStack == null || cursor == null)
		{
			return;
		}

		Material type = itemStack.getType();

		if (type == Material.EMERALD_BLOCK || type == Material.REDSTONE_BLOCK || type == Material.STAINED_GLASS_PANE)
		{
			if (type == Material.EMERALD_BLOCK)
			{
				finalise(player);
			}
			else if (type == Material.REDSTONE_BLOCK)
			{
				cancel(player);
			}

			event.setCancelled(true);
			return;
		}

		TradeableItem currentItem = fromItemStack(itemStack);
		TradeableItem cursorItem = fromItemStack(cursor);

		if (currentItem == null && cursorItem == null)
		{
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 0.7F);
			player.sendMessage(F.main("Shop", "You cannot sell that item."));
			return;
		}

		UtilServer.runSyncLater(() -> recalculatePrice(inv), 1);
	}

	@EventHandler
	public void inventoryClose(InventoryCloseEvent event)
	{
		Player player = (Player) event.getPlayer();

		if (_inv.containsKey(player))
		{
			cancel(player);
		}
	}

	private void recalculatePrice(Inventory inv)
	{
		int price = 0;

		for (ItemStack itemStack : inv.getContents())
		{
			TradeableItem tradeableItem = fromItemStack(itemStack);

			if (tradeableItem == null)
			{
				continue;
			}

			price += tradeableItem.getCost() * itemStack.getAmount();
		}

		_total = price;
		inv.setItem(8, getConfirm());
	}

	private void finalise(Player player)
	{
		recalculatePrice(_inv.remove(player));
		player.closeInventory();
		player.playSound(player.getLocation(), Sound.VILLAGER_YES, 1, 0.7F);
		_economy.addToStore(player, "Sold Items", _total);
	}

	private void cancel(Player player)
	{
		Inventory inv = _inv.remove(player);

		for (ItemStack itemStack : inv.getContents())
		{
			TradeableItem tradeableItem = fromItemStack(itemStack);

			if (tradeableItem == null)
			{
				continue;
			}

			player.getInventory().addItem(itemStack);
		}

		player.closeInventory();
	}

	private TradeableItem fromItemStack(ItemStack itemStack)
	{
		if (itemStack == null)
		{
			return null;
		}

		for (TradeableItem item : _selling)
		{
			ItemStack itemStack2 = item.getLootItem().getItemStack();

			if (itemStack.getType() == itemStack2.getType())
			{
				return item;
			}
		}

		return null;
	}

	private ItemStack getConfirm()
	{
		return new ItemBuilder(Material.EMERALD_BLOCK)
				.setTitle(C.cGreenB + "Confirm")
				.addLore("", "Click to sell these current items", "at a price of " + F.currency(GlobalCurrency.GEM, _total) + ".")
				.build();
	}
}
