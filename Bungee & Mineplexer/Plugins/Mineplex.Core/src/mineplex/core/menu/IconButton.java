package mineplex.core.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Representation of a purely cosmetic icon
 * IE: When clicked, this button does not execute any action
 */
@Deprecated
public class IconButton extends Button
{

	public IconButton(ItemStack item)
	{
		super(item);
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{

	}
}
