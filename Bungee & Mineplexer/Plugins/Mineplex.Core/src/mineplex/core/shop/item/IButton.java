package mineplex.core.shop.item;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public interface IButton
{
	public void onClick(Player player, ClickType clickType);
}
