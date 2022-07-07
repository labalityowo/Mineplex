package mineplex.hub.modules.salesannouncements;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;

public class SalesAnnouncementData
{
	private final Integer _id;
	private final PermissionGroup[] _displayTo;
	private final String _message;
	private boolean _enabled;
	
	public SalesAnnouncementData(Integer id, PermissionGroup[] displayTo, String message, boolean enabled)
	{
		_id = id;
		_displayTo = displayTo;
		_message = message;
		_enabled = enabled;
	}
	
	public Integer getId()
	{
		return _id;
	}
	
	public PermissionGroup[] getDisplayTo()
	{
		return _displayTo;
	}
	
	public boolean shouldDisplayTo(PermissionGroup rank)
	{
		return Arrays.asList(_displayTo).contains(rank);
	}
	
	public String getMessage(boolean raw)
	{
		return raw ? _message : ChatColor.translateAlternateColorCodes('&', _message);
	}
	
	public ItemStack getButtonForm()
	{
		Material type = Material.REDSTONE_BLOCK;
		String lore = C.cRed + "Click to Enable, Right-Click to Delete";
		String excerpt = getMessage(false);
		if (excerpt.length() > 9)
		{
			excerpt = excerpt.substring(0, 9) + C.Reset + "...";
		}
		if (_enabled)
		{
			type = Material.EMERALD_BLOCK;
			lore = C.cGreen + "Click to Disable, Right-Click to Delete";
		}
		
		return new ItemBuilder(type).setTitle("ID: " + getId()).setLore(excerpt, C.cRed + " ", lore).build();
	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
	
	public void setEnabled(boolean enabled)
	{
		_enabled = enabled;
	}
}