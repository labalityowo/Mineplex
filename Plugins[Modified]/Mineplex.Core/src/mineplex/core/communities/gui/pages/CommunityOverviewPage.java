package mineplex.core.communities.gui.pages;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.events.CommunityDisbandEvent;
import mineplex.core.communities.events.CommunityMemberDataUpdateEvent;
import mineplex.core.communities.gui.buttons.ActionButton;
import mineplex.core.communities.gui.buttons.CommunityVisualizationButton;
import mineplex.core.itemstack.ItemBuilder;

public class CommunityOverviewPage extends CommunitiesGUIPage
{
	private static final int COMMUNITIES_PER_PAGE = 27;
	
	private int _page = 1;
	
	public CommunityOverviewPage(Player viewer)
	{
		super("Your Communities", 6, viewer);
		
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
			//1
			ActionButton communitiesButton = new ActionButton(new ItemBuilder(Material.EMERALD).setTitle(C.cGreenB + "Your Communities").build(), clickType -> {});
			Buttons.put(1, communitiesButton);
			Inv.setItem(1, communitiesButton.Button);
			//4
			ActionButton browserButton = new ActionButton(new ItemBuilder(Material.COMPASS).setTitle(C.cGreenB + "Browse Communities").build(), clickType ->
			{
				new CommunityBrowserPage(Viewer).open();
			});
			Buttons.put(4, browserButton);
			Inv.setItem(4, browserButton.Button);
			//7
			ActionButton invitesButton = new ActionButton(new ItemBuilder(Material.PAPER).setTitle(C.cGreenB + "Community Invites").build(), clickType ->
			{
				new CommunityInvitesPage(Viewer);
			});
			Buttons.put(7, invitesButton);
			Inv.setItem(7, invitesButton.Button);
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
		
		int slot = 18;
		boolean cleared = false;
		for (int i = (page - 1) * COMMUNITIES_PER_PAGE; i < (page - 1) * COMMUNITIES_PER_PAGE + COMMUNITIES_PER_PAGE && i < getCommunityManager().Get(Viewer).getTotalCommunities(); i++)
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
			List<Community> coms = getCommunityManager().Get(Viewer).getCommunities();
			coms.sort((c1, c2) ->
			{
				if (c1.getMembers().get(Viewer.getUniqueId()).Role == c2.getMembers().get(Viewer.getUniqueId()).Role)
				{
					return c1.getName().compareTo(c2.getName());
				}

				if (c1.getMembers().get(Viewer.getUniqueId()).Role.ordinal() < c2.getMembers().get(Viewer.getUniqueId()).Role.ordinal())
				{
					return -1;
				}
				return 1;
			});
			CommunityVisualizationButton button = new CommunityVisualizationButton(Viewer, coms.get(i), false);
			Buttons.put(slot, button);
			Inv.setItem(slot, button.Button);
			
			slot++;
		}
		
		Viewer.updateInventory();
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

	@EventHandler
	public void onCommunityDisband(CommunityDisbandEvent event)
	{
		if (getCommunityManager().Get(Viewer).Invites.contains(event.getCommunity().getId()))
		{
			setup(1, true);
		}
	}
}