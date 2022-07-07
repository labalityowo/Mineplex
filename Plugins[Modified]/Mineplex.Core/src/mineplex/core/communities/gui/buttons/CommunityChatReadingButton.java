package mineplex.core.communities.gui.buttons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.communities.data.Community;
import mineplex.core.itemstack.ItemBuilder;

public class CommunityChatReadingButton extends CommunitiesGUIButton
{
	private Player _viewer;
	private Community _community;
	
	public CommunityChatReadingButton(Player viewer, Community community)
	{
		super(getDisplay(community, viewer));
		
		_viewer = viewer;
		_community = community;
	}
	
	private static ItemStack getDisplay(Community community, Player viewer)
	{
		ItemStack item = new ItemBuilder(Material.BOOK_AND_QUILL).setTitle(C.cGreenB + "Toggle Chat Visibility").build();
		
		return item;
	}

	@Override
	public void update()
	{
		Button = getDisplay(_community, _viewer);
	}

	@Override
	public void handleClick(ClickType type)
	{
		if (_community.getMembers().containsKey(_viewer.getUniqueId()))
		{
			getCommunityManager().handleToggleReadingChat(_viewer, _community);
		}
	}
}