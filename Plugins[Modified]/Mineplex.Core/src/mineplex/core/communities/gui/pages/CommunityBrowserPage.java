package mineplex.core.communities.gui.pages;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.communities.data.ICommunity;
import mineplex.core.communities.events.CommunityBrowserUpdateEvent;
import mineplex.core.communities.events.CommunityDisbandEvent;
import mineplex.core.communities.CommunityManager;
import mineplex.core.communities.gui.buttons.CommunityBrowserButton;
import mineplex.core.communities.gui.buttons.ActionButton;
import mineplex.core.itemstack.ItemBuilder;

public class CommunityBrowserPage extends CommunitiesGUIPage
{
	private static final int COMMUNITIES_PER_PAGE = 27;
	
	private int _page = 1;
	private boolean _loading = false;
	
	private List<Integer> _displaying = new ArrayList<>();
	
	public CommunityBrowserPage(Player viewer)
	{
		super("Community Browser", 6, viewer);
		
		setup(1, true);
		open();
	}
	
	private void setup(int page, boolean initial)
	{
		if (_loading)
		{
			return;
		}

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
			ActionButton browserButton = new ActionButton(new ItemBuilder(Material.COMPASS).setTitle(C.cGreenB + "Browse Communities").build(), clickType -> {});
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

		final CommunityManager manager = getCommunityManager();
		final List<Integer> browserIds = manager.getBrowserIds();

		_displaying.clear();

		// Generate the list of ids we're going to display
		for (int i = (page - 1) * COMMUNITIES_PER_PAGE; i < (page - 1) * COMMUNITIES_PER_PAGE + COMMUNITIES_PER_PAGE && i < browserIds.size(); i++)
		{
			_displaying.add(manager.getBrowserIds().get(i));
		}

		_loading = true;

		// load the missing browser communities
		manager.loadBrowserCommunities(_displaying, () ->
		{
			_loading = false;

			int slot = 18;
			boolean cleared = false;

			for (int i = (page - 1) * COMMUNITIES_PER_PAGE; i < (page - 1) * COMMUNITIES_PER_PAGE + COMMUNITIES_PER_PAGE && i < browserIds.size(); i++)
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

				int id = browserIds.get(i);
				ICommunity community = manager.getBrowserCommunity(id);
				if (community == null)
				{
					Inv.setItem(slot, new ItemStack(Material.INK_SACK, 1, DyeColor.GRAY.getDyeData()));
				} else
				{
					CommunityBrowserButton button = new CommunityBrowserButton(Viewer, community);
					Buttons.put(slot, button);
					Inv.setItem(slot, button.Button);
				}

				slot++;
			}

			Viewer.updateInventory();
		});
	}
	
	@EventHandler
	public void onBrowserUpdate(CommunityBrowserUpdateEvent event)
	{
		setup(1, true);
	}
	
	@EventHandler
	public void onCommunityDisband(CommunityDisbandEvent event)
	{
		if (_displaying.contains(event.getCommunity().getId()))
		{
			setup(1, true);
		}
	}
}