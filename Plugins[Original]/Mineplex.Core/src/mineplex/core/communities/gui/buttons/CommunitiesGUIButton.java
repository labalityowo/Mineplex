package mineplex.core.communities.gui.buttons;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.communities.CommunityManager;

public abstract class CommunitiesGUIButton
{
	public ItemStack Button = null;
	
	public CommunitiesGUIButton(ItemStack button)
	{
		Button = button;
	}
	
	public static CommunityManager getCommunityManager()
	{
		return Managers.get(CommunityManager.class);
	}
	
	public abstract void update();
	
	public abstract void handleClick(ClickType type);
}