package mineplex.core.shop.item;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.shop.item.IButton;

public class DisplayButton implements IButton
{

	public DisplayButton()
	{
		
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		// Display button doesn't activate any on click events
	}

}
