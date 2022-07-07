package mineplex.core.menu;

import mineplex.core.MiniPlugin;
import mineplex.core.itemstack.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class to manage dynamic creation of GUI's
 *
 * @deprecated Allows abuse of the F6 Minecraft 1.8 streaming bug, use {@link mineplex.core.shop.page.ShopPageBase} instead.
 */
@Deprecated
public abstract class Menu<T extends MiniPlugin>
{

	private final ItemStack PANE = new ItemBuilder(Material.STAINED_GLASS_PANE).setTitle(" ").setData(DyeColor.LIGHT_BLUE.getWoolData()).build();

	protected static final Button[] EMPTY = new Button[54];
	protected static final Map<UUID, Menu> MENUS = new HashMap<>();
	private final String _name;
	private final T _plugin;
	private boolean _useClose = false;
	private Button[] _buttons;
	private Player _player;

	public Menu(String name, T plugin)
	{
		_name = name;
		_buttons = EMPTY;
		_plugin = plugin;
	}

	public static Menu get(UUID name)
	{
		return MENUS.get(name);
	}

	public T getPlugin()
	{
		return _plugin;
	}

	public String getName()
	{
		return _name;
	}

	/**
	 * Open and setup the inventory for the player to view
	 * Store a reference to it inside a map for retrieving later
	 *
	 * @param player The player who we wish to show the GUI to
	 */
	public void open(Player player)
	{
		_player = player;
		setButtons(setUp(player));

		if (MENUS.get(player.getUniqueId()) != null)
		{
			MENUS.remove(player.getUniqueId());
		}

		MENUS.put(player.getUniqueId(), this);

		int size = (_buttons.length + 8) / 9 * 9;
		Inventory inventory = Bukkit.createInventory(player, size, _name);

		for (int i = 0; i < _buttons.length; i++)
		{
			if (_buttons[i] == null)
			{
				continue;
			}

			ItemStack item = _buttons[i].getItemStack();

			inventory.setItem(i, item);
		}
		player.openInventory(inventory);
	}

	/**
	 * Set up the GUI with buttons
	 *
	 * @return The setup button array
	 */
	protected abstract Button[] setUp(Player player);

	public Button[] getButtons()
	{
		return _buttons;
	}

	public void setButtons(Button[] buttons)
	{
		_buttons = buttons;
	}

	/**
	 * Retrieve the button based off the slot
	 *
	 * @param slot The slot in the inventory
	 * @return The button corresponding to that slot
	 */
	public Button getButton(int slot)
	{
		try
		{
			return _buttons[slot];
		} catch (ArrayIndexOutOfBoundsException e)
		{
			//There isn't a button there, so no need to throw an error
			//e.printStackTrace();
			return null;
		}
	}

	/**
	 * Replace a button, or create a new button dynamically
	 * Update the players GUI
	 *
	 * @param slot   The slot to set the new button
	 * @param button The reference to the button
	 */
	public void setButton(int slot, Button button)
	{
		try
		{
			_buttons[slot] = button;
		} catch (ArrayIndexOutOfBoundsException ignored)
		{
			ignored.printStackTrace();
		}
		update();
	}

	/**
	 * Refresh the players view, allows to change what the player sees, without opening and closing the GUI
	 */
	public void update()
	{
		InventoryView view = _player.getOpenInventory();

		if (view == null)
		{
			return;
		}

		if (!view.getTitle().equalsIgnoreCase(_name))
		{
			return;
		}

		Inventory inventory = view.getTopInventory();
		for (int i = 0; i < _buttons.length; i++)
		{
			if (_buttons[i] == null)
			{
				continue;
			}

			ItemStack item = _buttons[i].getItemStack();

			inventory.setItem(i, item);
		}
	}

	/**
	 * Reset this players current menu's buttons and refresh the page
	 */
	public void resetAndUpdate()
	{
		InventoryView view = _player.getOpenInventory();

		if (view == null)
		{
			return;
		}

		if (!view.getTitle().equalsIgnoreCase(_name))
		{
			return;
		}

		Inventory inventory = view.getTopInventory();
		Button[] buttons = setUp(_player);
		for (int i = 0; i < buttons.length; i++)
		{
			if (buttons[i] == null)
			{
				continue;
			}

			ItemStack item = buttons[i].getItemStack();

			inventory.setItem(i, item);
		}
	}

	protected Button[] pane(Button[] buttons)
	{
		for (int i = 0; i < 9; i++)
		{
			if (buttons[i] == null)
			{
				buttons[i] = new IconButton(PANE);
			}

			if (buttons[i + buttons.length - 9] == null)
			{
				buttons[i + buttons.length - 9] = new IconButton(PANE);
			}

			if (i == 0 || i == 8)
			{
				for (int a = 0; a < buttons.length; a += 9)
				{
					if (buttons[i + a] == null)
					{
						buttons[i + a] = new IconButton(PANE);
					}
				}
			}
		}
		return buttons;
	}

	public void onClose(Player player)
	{

	}

	public static Menu remove(UUID uniqueId)
	{
		return MENUS.remove(uniqueId);
	}

	public Player getPlayer()
	{
		return _player;
	}

	public boolean isUseClose()
	{
		return _useClose;
	}

	public void setUseClose(boolean useClose)
	{
		_useClose = useClose;
	}
}
