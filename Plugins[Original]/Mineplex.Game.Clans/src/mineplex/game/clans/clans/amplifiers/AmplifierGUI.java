package mineplex.game.clans.clans.amplifiers;

import java.util.HashMap;
import java.util.Map;

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

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.game.clans.clans.amplifiers.AmplifierManager.AmplifierType;

/**
 * GUI manager for amplifiers
 */
public class AmplifierGUI implements Listener
{
	private Player _viewer;
	private AmplifierManager _manager;
	private Inventory _inventory;
	private final Map<Integer, ItemStack> _items = new HashMap<>();
	private final Map<Integer, AmplifierType> _boundSlots = new HashMap<>();
	private AmplifierType _selected;
	
	public AmplifierGUI(Player viewer, AmplifierManager manager)
	{
		_viewer = viewer;
		_manager = manager;
		_inventory = Bukkit.createInventory(viewer, 27, C.cClansNether + "Rune Amplifiers");
		Bukkit.getPluginManager().registerEvents(this, manager.getPlugin());
		
		propagate();
		open();
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
	public Map<Integer, ItemStack> getItems()
	{
		return _items;
	}
	
	/**
	 * Fills the gui with buttons
	 */
	public void propagate()
	{
		getItems().clear();
		_boundSlots.clear();
		int[] slots = {12, 14};
		int i = 0;
		for (AmplifierType type : AmplifierType.values())
		{
			int owned = _manager.getAmountOwned(getViewer(), type);
			owned = Math.max(owned, 0);
			int slot = slots[i++];
			getItems().put(slot, new ItemBuilder(Material.NETHER_STAR)
					.setTitle(type.getDisplayName())
					.addLore(C.cYellow + "Summons a " + C.cClansNether + "Nether Portal" + C.cYellow + " in Shops")
					.addLore(C.cYellow + "And doubles the chance of Rune drops.")
					.addLore(C.cRed + " ")
					.addLore(C.cGreen + ">Click to Activate<")
					.addLore(C.cBlue + " ")
					.addLore(C.cDAqua + "You own " + F.greenElem(String.valueOf(owned)) + C.cDAqua + " " + type.getCleanDisplayName() + "s")
					.build()
			);
			_boundSlots.put(slot, type);
		}
		refresh();
	}
	
	/**
	 * Fills the confirmation menu with buttons
	 */
	public void propagateConfirmation()
	{
		getItems().clear();
		_boundSlots.clear();
		getItems().put(12, new ItemBuilder(Material.STAINED_GLASS_PANE).setData((short) 5).setTitle(C.cGreen + "Confirm").build());
		getItems().put(14, new ItemBuilder(Material.STAINED_GLASS_PANE).setData((short) 14).setTitle(C.cRed + "Cancel").build());
		refresh();
	}
	
	/**
	 * Handles players clicking on buttons
	 * @param slot The slot clicked on
	 * @param type The type of click
	 */
	public void onClick(Integer slot, ClickType type)
	{
		if (_boundSlots.containsKey(slot))
		{
			if (_manager.hasActiveAmplifier())
			{
				UtilPlayer.message(getViewer(), F.main(_manager.getName(), "An amplifier is already active!"));
				_manager.runSyncLater(() ->
				{
					getViewer().closeInventory();
				}, 1L);
			}
			else
			{
				_selected = _boundSlots.get(slot);
				if (_manager.getAmountOwned(getViewer(), _selected) > 0)
				{
					propagateConfirmation();
				}
				else
				{
					UtilPlayer.message(getViewer(), F.main(_manager.getName(), "You do not have enough of that amplifier! Purchase some at http://www.mineplex.com/shop!"));
				}
			}
			return;
		}
		
		if (slot == 12)
		{
			_manager.runSyncLater(() ->
			{
				_manager.useAmplifier(getViewer(), _selected);
				getViewer().closeInventory();
			}, 1L);
		}
		if (slot == 14)
		{
			_selected = null;
			propagate();
		}
	}
	
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