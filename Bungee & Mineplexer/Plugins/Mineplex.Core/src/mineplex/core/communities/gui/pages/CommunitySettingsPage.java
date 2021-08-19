package mineplex.core.communities.gui.pages;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.events.CommunityDisbandEvent;
import mineplex.core.communities.events.CommunityJoinRequestsUpdateEvent;
import mineplex.core.communities.events.CommunityMemberDataUpdateEvent;
import mineplex.core.communities.events.CommunityMembershipUpdateEvent;
import mineplex.core.communities.data.CommunityRole;
import mineplex.core.communities.data.CommunitySetting;
import mineplex.core.communities.events.CommunitySettingUpdateEvent;
import mineplex.core.communities.gui.buttons.ActionButton;
import mineplex.core.communities.gui.buttons.CommunitiesGUIButton;
import mineplex.core.communities.gui.buttons.CommunityButton;
import mineplex.core.communities.gui.buttons.CommunityChatReadingButton;
import mineplex.core.communities.gui.buttons.CommunitySettingButton;
import mineplex.core.itemstack.ItemBuilder;

public class CommunitySettingsPage extends CommunitiesGUIPage
{
	private Community _community;
	
	public CommunitySettingsPage(Player viewer, Community community)
	{
		super(community.getName() + C.cBlue, 6, viewer);
		_community = community;
		
		setup();
		open();
	}
	
	private void setup()
	{
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
				ActionButton requestsButton = new ActionButton(new ItemBuilder(Material.PAPER).setTitle(C.cGreenB + "Join Requests").build(), clickType ->
				{
					new CommunityJoinRequestsPage(Viewer, _community).open();
				});
				Buttons.put(2, requestsButton);
				Inv.setItem(2, requestsButton.Button);
				ActionButton settingsButton = new ActionButton(new ItemBuilder(Material.REDSTONE_COMPARATOR).setTitle(C.cGreenB + "Community Settings").build(), clickType -> {});
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
			CommunitySettingButton gameButton = new CommunitySettingButton(Viewer, _community, CommunitySetting.FAVORITE_GAME);
			Buttons.put(20, gameButton);
			Inv.setItem(20, gameButton.Button);
			
			CommunitySettingButton privacyButton = new CommunitySettingButton(Viewer, _community, CommunitySetting.PRIVACY);
			Buttons.put(22, privacyButton);
			Inv.setItem(22, privacyButton.Button);
			
			CommunityChatReadingButton chatButton = new CommunityChatReadingButton(Viewer, _community);
			Buttons.put(24, chatButton);
			Inv.setItem(24, chatButton.Button);
			
			CommunitySettingButton delayButton = new CommunitySettingButton(Viewer, _community, CommunitySetting.CHAT_DELAY);
			Buttons.put(38, delayButton);
			Inv.setItem(38, delayButton.Button);
			
			CommunitySettingButton communityColorButton = new CommunitySettingButton(Viewer, _community, CommunitySetting.CHAT_NAME_COLOR);
			Buttons.put(40, communityColorButton);
			Inv.setItem(40, communityColorButton.Button);
			
			CommunitySettingButton playerColorButton = new CommunitySettingButton(Viewer, _community, CommunitySetting.CHAT_PLAYER_COLOR);
			Buttons.put(41, playerColorButton);
			Inv.setItem(41, playerColorButton.Button);
			
			CommunitySettingButton messageColorButton = new CommunitySettingButton(Viewer, _community, CommunitySetting.CHAT_MESSAGE_COLOR);
			Buttons.put(42, messageColorButton);
			Inv.setItem(42, messageColorButton.Button);
		}
		
		Viewer.updateInventory();
	}
	
	@EventHandler
	public void onRequestsUpdate(CommunityJoinRequestsUpdateEvent event)
	{
		if (event.getCommunity().getId() != _community.getId())
		{
			return;
		}
		CommunitiesGUIButton button = Buttons.get(4);
		button.update();
		Inv.setItem(4, button.Button);
		Viewer.updateInventory();
	}
	
	@EventHandler
	public void onMembershipUpdate(CommunityMembershipUpdateEvent event)
	{
		if (event.getCommunity().getId() != _community.getId())
		{
			return;
		}
		
		if (!_community.getMembers().containsKey(Viewer.getUniqueId()) || _community.getMembers().get(Viewer.getUniqueId()).Role.ordinal() > CommunityRole.COLEADER.ordinal())
		{
			new CommunityMembersPage(Viewer, _community).open();
		}
	}
	
	@EventHandler
	public void onSettingsUpdate(CommunitySettingUpdateEvent event)
	{
		if (event.getCommunity().getId() != _community.getId())
		{
			return;
		}
		setup();
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
		setup();
	}
}