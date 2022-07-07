package mineplex.core.communities.gui.buttons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.data.Community.PrivacySetting;
import mineplex.core.communities.data.CommunityRole;
import mineplex.core.itemstack.ItemBuilder;

public class CommunityButton extends CommunitiesGUIButton
{
	private Player _viewer;
	private Community _community;
	
	public CommunityButton(Player viewer, Community community)
	{
		super(getDisplay(community, viewer));
		
		_viewer = viewer;
		_community = community;
	}
	
	private static ItemStack getDisplay(Community community, Player viewer)
	{
		ItemStack item = new ItemBuilder(Material.REDSTONE_BLOCK).setTitle(C.cGreenB + community.getName()).addLore(UtilText.splitLinesToArray(new String[] {C.cGreen, C.cYellow + "Members " + C.cWhite + community.getMembers().size(), C.cYellow + "Description " + C.cWhite + community.getDescription(), C.cRed, C.cYellow + "Shift-Left Click " + C.cWhite + "Request To Join"}, LineFormat.LORE)).build();
		
		if (community.getMembers().containsKey(viewer.getUniqueId()))
		{
			ItemBuilder builder = new ItemBuilder(Material.EMERALD_BLOCK).setTitle(C.cGreenB + community.getName()).addLore(UtilText.splitLinesToArray(new String[] {C.cGreen, C.cYellow + "Members " + C.cWhite + community.getMembers().size(), C.cYellow + "Description " + C.cWhite + community.getDescription(), C.cRed, C.cYellow + "Shift-Left Click " + C.cWhite + "Leave Community"}, LineFormat.LORE));
			if (community.getMembers().get(viewer.getUniqueId()).Role == CommunityRole.LEADER)
			{
				builder.addLore(C.cBlue);
				builder.addLore(UtilText.splitLineToArray(C.cGray + "Use " + C.cYellow + "/com disband " + community.getName() + C.cGray + " to disband!", LineFormat.LORE));
			}
			item = builder.build();
		}
		else if (community.getJoinRequests().containsKey(viewer.getUniqueId()))
		{
			item = new ItemBuilder(Material.REDSTONE_BLOCK).setTitle(C.cGold + community.getName()).addLore(UtilText.splitLinesToArray(new String[] {C.cGreen, C.cYellow + "Members " + C.cWhite + community.getMembers().size(), C.cYellow + "Description " + C.cWhite + community.getDescription(), C.cRed, C.cYellow + "Shift-Left Click " + C.cWhite + "Cancel Join Request"}, LineFormat.LORE)).build();
		}
		else if (getCommunityManager().Get(viewer).Invites.contains(community.getId()) || community.getPrivacySetting() == PrivacySetting.OPEN)
		{
			item = new ItemBuilder(Material.REDSTONE_BLOCK).setTitle(C.cGold + community.getName()).addLore(UtilText.splitLinesToArray(new String[] {C.cGreen, C.cYellow + "Members " + C.cWhite + community.getMembers().size(), C.cYellow + "Description " + C.cWhite + community.getDescription(), C.cRed, C.cYellow + "Shift-Left Click " + C.cWhite + "Join Community"}, LineFormat.LORE)).build();
		}
		else if (community.getPrivacySetting() == PrivacySetting.PRIVATE)
		{
			item = new ItemBuilder(Material.REDSTONE_BLOCK).setTitle(C.cGreenB + community.getName()).addLore(UtilText.splitLinesToArray(new String[] {C.cGreen, C.cYellow + "Members " + C.cWhite + community.getMembers().size(), C.cYellow + "Description " + C.cWhite + community.getDescription(), C.cRed, C.cRed + "Closed"}, LineFormat.LORE)).build();
		}
		
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
		if (type == ClickType.SHIFT_LEFT)
		{
			if (_community.getMembers().containsKey(_viewer.getUniqueId()))
			{
				if (_community.getMembers().get(_viewer.getUniqueId()).Role == CommunityRole.LEADER)
				{
					UtilPlayer.message(_viewer, F.main(getCommunityManager().getName(), "You cannot leave " + F.name(_community.getName()) + " without passing on leadership first! If you want to disband your community, type " + F.elem("/com disband " + _community.getName()) + "!"));
					return;
				}
				getCommunityManager().handleLeave(_viewer, _community, _community.getMembers().get(_viewer.getUniqueId()));
			}
			else if (_community.getJoinRequests().containsKey(_viewer.getUniqueId()))
			{
				getCommunityManager().handleCloseJoinRequest(_viewer, _community, _community.getJoinRequests().get(_viewer.getUniqueId()), false);
			}
			else if (getCommunityManager().Get(_viewer).Invites.contains(_community.getId()) || _community.getPrivacySetting() == PrivacySetting.OPEN)
			{
				getCommunityManager().handleJoin(_viewer, _community, getCommunityManager().Get(_viewer).Invites.contains(_community.getId()));
			}
			else
			{
				if (_community.getPrivacySetting() != PrivacySetting.PRIVATE)
				{
					getCommunityManager().handleJoinRequest(_viewer, _community);
				}
			}
		}
	}
}