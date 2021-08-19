package mineplex.core.menu;

import mineplex.core.MiniPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * An abstract class for managing buttons inside of menus
 */
@Deprecated
public abstract class Button<T extends MiniPlugin>
{

	private ItemStack _item;
	private T _plugin;

	public Button(ItemStack item, T plugin)
	{
		_item = item;
		_plugin = plugin;
	}

	public Button(ItemStack itemStack)
	{
		_item = itemStack;
		_plugin = null;
	}

	/**
	 * The method called when a players clicks the slot
	 *
	 * @param player    The player who clicked
	 * @param clickType Tge type of click
	 */
	public abstract void onClick(Player player, ClickType clickType);

	/**
	 * Called when clicking on a specific item is needed, rather than just the slot
	 * Empty by default
	 *
	 * @param player    The player who clicked
	 * @param clickType Tge type of click
	 * @param item The ItemStack clicked
	 */
	public void onClick(Player player, ClickType clickType, ItemStack item)
	{

	}

	public boolean useItemClick()
	{
		return false;
	}

	public ItemStack getItemStack()
	{
		return _item;
	}

	public void setItemStack(ItemStack item)
	{
		_item = item;
	}

	public T getPlugin()
	{
		return _plugin;
	}

}
