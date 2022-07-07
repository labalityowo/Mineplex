package mineplex.core.menu.builtin;

import java.util.function.Supplier;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.MiniPlugin;
import mineplex.core.menu.Button;
import mineplex.core.menu.Menu;

@Deprecated
public class ButtonOpenInventory<T extends MiniPlugin> extends Button<T>
{
	private final Supplier<Menu<T>> _menuSupplier;

	public ButtonOpenInventory(ItemStack item, T plugin, Supplier<Menu<T>> menu)
	{
		super(item, plugin);
		_menuSupplier = menu;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		_menuSupplier.get().open(player);
	}
}
