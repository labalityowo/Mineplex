package mineplex.core.communities.gui.pages;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.events.CommunityDisbandEvent;
import mineplex.core.communities.data.CommunityJoinRequestInfo;
import mineplex.core.communities.events.CommunityJoinRequestsUpdateEvent;
import mineplex.core.communities.events.CommunityMemberDataUpdateEvent;
import mineplex.core.communities.events.CommunityMembershipUpdateEvent;
import mineplex.core.communities.data.CommunityRole;
import mineplex.core.communities.gui.buttons.ActionButton;
import mineplex.core.communities.gui.buttons.CommunityButton;
import mineplex.core.communities.gui.buttons.CommunityChatReadingButton;
import mineplex.core.communities.gui.buttons.CommunityJoinRequestButton;
import mineplex.core.itemstack.ItemBuilder;

public class CommunityJoinRequestsPage extends CommunitiesGUIPage
{
	private static final int REQUESTS_PER_PAGE = 27;
	
	private Community _community;
	private int _page = 1;
	
	public CommunityJoinRequestsPage(Player viewer, Community community)
	{
		super(community.getName() + C.cBlack, 6, viewer);
		_community = community;
		
		setup(1, true);
		open();
	}
	
	private void setup(int page, boolean initial)
	{
		if (initial)
		{
			Buttons.clear();
			Inv.clear();
		}
		{
			//0
			ActionButton membersButton = new ActionButton(new ItemBuilder(Material.SKULL_ITEM).setData((short)3).setTitle(C.cGreenB + "Members").build(), clickType ->
			{
				new CommunityMembersPage(Viewer, _community).open();
			});
			Buttons.put(0, membersButton);
			Inv.setItem(0, membersButton.Button);
			//4
			CommunityButton communityButton = new CommunityButton(Viewer, _community);
			Buttons.put(4, communityButton);
			Inv.setItem(4, communityButton.Button);
			//8
			ActionButton returnButton = new ActionButton(new ItemBuilder(Material.BED).setTitle(C.cGray + "\u21FD Go Back").build(), clickType ->
			{
				new CommunityOverviewPage(Viewer).open();
			});
			Buttons.put(8, returnButton);
			Inv.setItem(8, returnButton.Button);
			//CoLeader+
			if (_community.getMembers().containsKey(Viewer.getUniqueId()) && _community.getMembers().get(Viewer.getUniqueId()).Role.ordinal() <= CommunityRole.COLEADER.ordinal())
			{
				ActionButton requestsButton = new ActionButton(new ItemBuilder(Material.PAPER).setTitle(C.cGreenB + "Join Requests").build(), clickType -> {});
				Buttons.put(2, requestsButton);
				Inv.setItem(2, requestsButton.Button);
				ActionButton settingsButton = new ActionButton(new ItemBuilder(Material.REDSTONE_COMPARATOR).setTitle(C.cGreenB + "Community Settings").build(), clickType ->
				{
					new CommunitySettingsPage(Viewer, _community);
				});
				Buttons.put(6, settingsButton);
				Inv.setItem(6, settingsButton.Button);
			}
			else if (_community.getMembers().containsKey(Viewer.getUniqueId()))
			{
				CommunityChatReadingButton chatButton = new CommunityChatReadingButton(Viewer, _community);
				Buttons.put(6, chatButton);
				Inv.setItem(6, chatButton.Button);
			}
		}
		{
			ActionButton back = new ActionButton(new ItemBuilder(Material.ARROW).setTitle(C.cGreen + "Previous Page").build(), clickType ->
			{
				if (_page == 1)
				{
					return;
				}
				setup(_page - 1, false);
			});
			ActionButton next = new ActionButton(new ItemBuilder(Material.ARROW).setTitle(C.cGreen + "Next Page").build(), clickType ->
			{
				setup(_page + 1, false);
			});
			Buttons.put(45, back);
			Inv.setItem(45, back.Button);
			Buttons.put(53, next);
			Inv.setItem(53, next.Button);
		}
		List<CommunityJoinRequestInfo> requests = new LinkedList<>();
		for (CommunityJoinRequestInfo info : _community.getJoinRequests().values())
		{
			requests.add(info);
		}
		requests.sort((info1, info2) ->
		{
			return info1.Name.compareToIgnoreCase(info2.Name);
		});
		
		int slot = 18;
		boolean cleared = false;
		for (int i = (page - 1) * REQUESTS_PER_PAGE; i < (page - 1) * REQUESTS_PER_PAGE + REQUESTS_PER_PAGE && i < requests.size(); i++)
		{
			if (!cleared && !initial)
			{
				cleared = true;
				_page = page;
				for (int clear = 18; clear < 45; clear++)
				{
					Buttons.remove(clear);
					Inv.setItem(clear, null);
				}
			}
			CommunityJoinRequestButton button = new CommunityJoinRequestButton(Viewer, _community, requests.get(i));
			Buttons.put(slot, button);
			Inv.setItem(slot, button.Button);
			
			slot++;
		}
		
		Viewer.updateInventory();
	}
	
	@EventHandler
	public void onMembershipUpdate(CommunityMembershipUpdateEvent event)
	{
		if (event.getCommunity().getId() != _community.getId())
		{
			return;
		}
		
		if (_community.getMembers().containsKey(Viewer.getUniqueId()) && _community.getMembers().get(Viewer.getUniqueId()).Role.ordinal() <= CommunityRole.COLEADER.ordinal())
		{
			setup(1, true);
		}
		else
		{
			new CommunityMembersPage(Viewer, _community).open();
		}
	}
	
	@EventHandler
	public void onRequestsUpdate(CommunityJoinRequestsUpdateEvent event)
	{
		if (event.getCommunity().getId() != _community.getId())
		{
			return;
		}
		setup(1, true);
	}
	
	@EventHandler
	public void onCommunityDisband(CommunityDisbandEvent event)
	{
		if (_community.getId() == event.getCommunity().getId())
		{
			Viewer.closeInventory();
		}
	}
	
	@EventHandler
	public void onMembershipUpdate(CommunityMemberDataUpdateEvent event)
	{
		if (!event.getPlayer().getUniqueId().toString().equalsIgnoreCase(Viewer.getUniqueId().toString()))
		{
			return;
		}
		
		setup(1, true);
	}
}