package mineplex.core.communities.data;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;

public class CommunityJoinRequestInfo
{
	public String Name;
	public final UUID UUID;
	public final int AccountId;
	private ItemStack _icon;
	
	public CommunityJoinRequestInfo(String name, UUID uuid, int accountId)
	{
		Name = name;
		UUID = uuid;
		AccountId = accountId;
		
		buildIcon();
	}
	
	private void buildIcon()
	{
		ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM);
		builder.setTitle(C.cGreenB + Name);
		builder.setLore(C.cRed, C.cYellow + "Left Click " + C.cWhite + "Accept", C.cYellow + "Shift-Right Click " + C.cWhite + "Deny");
		builder.setData((short)3);
		builder.setPlayerHead(Name);
		_icon = builder.build();
	}
	
	public void update(String name)
	{
		if (name == null)
		{
			return;
		}
		Name = name;
		buildIcon();
	}
	
	public ItemStack getRepresentation()
	{
		return _icon;
	}
}