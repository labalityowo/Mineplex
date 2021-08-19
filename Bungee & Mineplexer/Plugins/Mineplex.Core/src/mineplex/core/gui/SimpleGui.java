package mineplex.core.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import mineplex.core.common.util.UtilPlayer;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class SimpleGui implements ItemRefresher, Listener
{
//	private Map<Integer, GuiItem> _buttonMap = new HashMap<Integer, GuiItem>();
	private GuiItem[] _items;

	private Player _player;
	private Plugin _plugin;
	private int _size;
	private String _title;
	private Inventory _inv;

	public SimpleGui(Plugin plugin, Player player)
	{
		this(plugin, player, null, 0);
	}

	public SimpleGui(Plugin plugin, Player player, int size)
	{
		this(plugin, player, null, size);
	}

	public SimpleGui(Plugin plugin, Player player, String title)
	{
		this(plugin, player, title, 0);
	}

	public SimpleGui(Plugin plugin, Player player, String title, int size)
	{

		Validate.notNull(plugin, "The plugin cannot be null!");
		Validate.notNull(player, "The player cannot be null!");

		this._plugin = plugin;
		this._player = player;

		if (size == 0)
			setSize(9);
		else
			setSize(size);

		if (title == null)
			setTitle(" ");
		else
			setTitle(title);

		updateArray();

		_inv = createInventory();
		refreshInventory();
	}

	private void updateArray()
	{
		_items = new GuiItem[_size];
	}

	public void setItem(int i, GuiItem item)
	{
		Validate.isTrue(i >= 0 && i < _size, "Tried to add a gui item outside of inventory range");

		try
		{
			GuiItem oldItem = getItem(i);
			if (oldItem != null) oldItem.close();

			if (item != null)
			{
				_items[i] = item;
				item.setup();
			}

			refreshItem(i);
		} catch (Exception ex)
		{
			System.err.println("Failed to add item " + item + " to GUI " + this + ": ");
			ex.printStackTrace();
		}
	}

	public GuiItem getItem(int i)
	{
		return _items[i];
	}

	@Override
	public void openInventory()
	{
		refreshInventory();
		UtilPlayer.swapToInventory(_player, _inv);
		Bukkit.getPluginManager().registerEvents(this, _plugin);
	}

	public Inventory createInventory()
	{
		Inventory inv = Bukkit.createInventory(_player, getSize(), getTitle());
		return inv;
	}

	public void refreshInventory()
	{
		for (int i = 0; i < _size; i++)
		{
			refreshItem(i);
		}
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent event)
	{
		if (!event.getWhoClicked().equals(_player) || !event.getInventory().equals(_inv))
			return;

		if (event.getSlot() >= 0 && event.getSlot() < _size)
		{
			GuiItem item = getItem(event.getSlot());
			if (item == null)
				return;

			event.setCancelled(true);

			item.click(event.getClick());
		}
	}

	@EventHandler
	public void teleport(PlayerTeleportEvent event)
	{
		if (!event.getPlayer().equals(_player))
			return;

		close();
	}

	@EventHandler
	public void inventoryClose(InventoryCloseEvent event)
	{
		if (!event.getPlayer().equals(_player))
			return;
		
		close();
	}

	@EventHandler
	public void quit(PlayerQuitEvent event)
	{
		if (!event.getPlayer().equals(_player))
			return;

		close();
	}

	private void close()
	{
//		_inv = null; // TODO - do we really need to null the inventory?
		HandlerList.unregisterAll(this);

		for (int i = 0; i < _size; i++)
		{
			GuiItem item = getItem(i);
			if (item != null) item.close();
		}
	}

	@Override
	@Deprecated
	public void refreshItem(GuiItem item)
	{
		if (_inv == null)
			return;

		for (int i = 0; i < _size; i++)
		{
			if (item.equals(getItem(i)))
				refreshItem(i);
		}
	}

	public void refreshItem(int slot)
	{
		GuiItem gi = getItem(slot);

		ItemStack itemStack = null;
		if (gi != null) itemStack = gi.getObject();

		_inv.setItem(slot, itemStack);
	}

	public int getSize()
	{
		return _size;
	}

	public String getTitle()
	{
		return _title;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public Plugin getPlugin()
	{
		return _plugin;
	}

	public void setTitle(String title)
	{
		this._title = title;
	}

	@Override
	public Inventory getInventory()
	{
		return _inv;
	}

	public void setSize(int size)
	{
		Validate.isTrue(size % 9 == 0, "The size " + size + " is not divisible by 9");
		this._size = size;
	}
}
