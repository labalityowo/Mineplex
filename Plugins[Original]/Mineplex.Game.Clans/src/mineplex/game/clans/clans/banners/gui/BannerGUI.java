package mineplex.game.clans.clans.banners.gui;

import java.util.HashMap;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.game.clans.clans.ClansManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Base class to manage banner guis
 */
public abstract class BannerGUI implements Listener
{
	private Player _viewer;
	private Inventory _inventory;
	private final HashMap<Integer, ItemStack> _items = new HashMap<>();
	
	public BannerGUI(Player viewer, String pageName, int slots)
	{
		_viewer = viewer;
		_inventory = Bukkit.createInventory(viewer, slots, pageName);
		Bukkit.getPluginManager().registerEvents(this, ClansManager.getInstance().getPlugin());
	}
	
	/**
	 * Gets the owner of this specific inventory
	 * @return The owner of this inventory
	 */
	public Player getViewer()
	{
		return _viewer;
	}
	
	/**
	 * Fetches all the items registered as buttons in this inventory gui
	 * @return A list of the items registered as buttons in this inventory gui
	 */
	public HashMap<Integer, ItemStack> getItems()
	{
		return _items;
	}
	
	/**
	 * Fills the gui with buttons
	 */
	public abstract void propagate();
	
	/**
	 * Handles players clicking on buttons
	 * @param slot The slot clicked on
	 * @param type The type of click
	 */
	public abstract void onClick(Integer slot, ClickType type);
	
	/**
	 * Opens this inventory to its viewer
	 */
	public void open()
	{
		_viewer.openInventory(_inventory);
	}
	
	/**
	 * Updates the GUI's visuals to match registered button items
	 */
	public void refresh()
	{
		_inventory.clear();
		for (Integer slot : _items.keySet())
		{
			_inventory.setItem(slot, _items.get(slot));
		}
		for (Integer slot = 0; slot < _inventory.getSize(); slot++)
		{
			if (!_items.containsKey(slot))
			{
				_inventory.setItem(slot, new ItemBuilder(Material.STAINED_GLASS_PANE).setTitle(C.cGray).setData((short)7).build());
			}
		}
		_viewer.updateInventory();
	}
	
	@EventHandler
	public void handleClick(InventoryClickEvent event)
	{
		if (event.getClickedInventory() == null || !event.getClickedInventory().equals(_inventory))
		{
			return;
		}
		if (!_viewer.getName().equals(event.getWhoClicked().getName()))
		{
			return;
		}
		event.setCancelled(true);
		Integer slot = event.getSlot();
		if (!_items.containsKey(slot))
		{
			return;
		}
		onClick(slot, event.getClick());
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent event)
	{
		if (event.getPlayer().getUniqueId().equals(_viewer.getUniqueId()))
		{
			HandlerList.unregisterAll(this);
		}
	}
}