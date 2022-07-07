package mineplex.clanshub.salesannouncements;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public abstract class SalesAnnouncementGUIButton
{
	public ItemStack Button = null;
	
	public SalesAnnouncementGUIButton(ItemStack button)
	{
		Button = button;
	}
	
	public abstract void update();
	
	public abstract void handleClick(ClickType type);
}