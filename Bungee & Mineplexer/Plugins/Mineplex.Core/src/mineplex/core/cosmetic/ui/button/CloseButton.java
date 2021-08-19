package mineplex.core.cosmetic.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.shop.item.IButton;

public class CloseButton implements IButton
{
	public void onClick(Player player, ClickType clickType)
	{
		player.closeInventory();
	}
}
