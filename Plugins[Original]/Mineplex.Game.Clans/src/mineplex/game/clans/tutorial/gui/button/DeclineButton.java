package mineplex.game.clans.tutorial.gui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.shop.item.IButton;

public class DeclineButton implements IButton
{
	@Override
	public void onClick(Player player, ClickType clickType)
	{
		player.closeInventory();
	}
}
