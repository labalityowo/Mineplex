package mineplex.core.communities.gui.pages;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.communities.CommunityManager;
import mineplex.core.communities.data.CommunityMemberData;
import mineplex.core.communities.data.ICommunity;
import mineplex.core.communities.events.CommunityDisbandEvent;
import mineplex.core.communities.events.CommunityMemberDataUpdateEvent;
import mineplex.core.communities.gui.buttons.ActionButton;
import mineplex.core.communities.gui.buttons.CommunityVisualizationButton;
import mineplex.core.itemstack.ItemBuilder;

public class CommunityInvitesPage extends CommunitiesGUIPage
{
	private static final int COMMUNITIES_PER_PAGE = 27;
	
	private int _page = 1;
	
	public CommunityInvitesPage(Player viewer)
	{
		super("Community Invites", 6, viewer);
		
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
			ActionButton communitiesButton = new ActionButton(new ItemBuilder(Material.EMERALD).setTitle(C.cGreenB + "Your Communities").build(), clickType ->
			{
				new CommunityOverviewPage(Viewer).open();
			});
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
			ActionButton invitesButton = new ActionButton(new ItemBuilder(Material.PAPER).setTitle(C.cGreenB + "Community Invites").build(), clickType -> {});
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

		final CommunityManager manager = getCommunityManager();
		final List<Integer> invites = manager.Get(Viewer).Invites;

		manager.loadBrowserCommunities(invites, () ->
		{
			int slot = 18;
			boolean cleared = false;
			for (int i = (page - 1) * COMMUNITIES_PER_PAGE; i < (page - 1) * COMMUNITIES_PER_PAGE + COMMUNITIES_PER_PAGE && i < invites.size(); i++)
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

				int communityId = invites.get(i);
				ICommunity community = manager.getBrowserCommunity(communityId);
				if (community == null)
				{
					Inv.setItem(slot, new ItemStack(Material.INK_SACK, 1, DyeColor.GRAY.getDyeData()));
				} else
				{
					CommunityVisualizationButton button = new CommunityVisualizationButton(Viewer, community, true);
					Buttons.put(slot, button);
					Inv.setItem(slot, button.Button);
				}

				slot++;
			}

			Viewer.updateInventory();
		});
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