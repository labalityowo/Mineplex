package mineplex.core.party.ui.menus;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.menu.IconButton;
import mineplex.core.party.InviteData;
import mineplex.core.party.PartyManager;
import mineplex.core.party.ui.PartyMenu;
import mineplex.core.party.ui.button.tools.invite.BackButton;
import mineplex.core.party.ui.button.tools.invite.DenyAllButton;
import mineplex.core.party.ui.button.tools.invite.FilterButton;
import mineplex.core.party.ui.button.tools.invite.InviteButton;
import mineplex.core.party.ui.button.tools.invite.NextPageButton;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The menu showing all pending invites for a player
 */
public class PartyInvitesMenu extends PartyMenu
{

	private final IconButton NO_INVITES = new IconButton(new ItemBuilder(Material.STAINED_GLASS_PANE)
	  .setData(DyeColor.RED.getWoolData())
	  .setTitle(C.cRedB + "No Invites")
	  .build());

	private final int INV_SIZE = 54;
	private final int SLOTS_PER_PAGE = 27;
	private final int STARTING_SLOT = 19;
	private final int BACK_BUTTON_SLOT = 0;
	private final int DENY_ALL_BUTTON_SLOW = 4;
	private final int FILTER_BUTTON_SLOT = 8;
	private final int NEXT_PAGE_SLOT = 53;
	private int _currentPage;
	private int _pagesNeeded;

	private Map<Integer, List<InviteData>> _pagesOfData;
	private List<InviteData> _data;

	private String _filterBy;

	public PartyInvitesMenu(PartyManager plugin)
	{
		super("Invites", plugin);
	}

	public PartyInvitesMenu(PartyManager plugin, String filterBy)
	{
		this(plugin);
		_filterBy = filterBy;
	}

	@Override
	protected Button[] setUp(Player player)
	{
		Button[] buttons = new Button[INV_SIZE];
		boolean showFiltered = false;

		buttons[BACK_BUTTON_SLOT] = new BackButton(getPlugin());

		List<InviteData> all = getPlugin().getInviteManager().getAllInvites(player);

		if (all == null || all.isEmpty())
		{
			for (int i = 10; i < 44; i++)
			{
				buttons[i] = NO_INVITES;
			}
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, .6f);
			return pane(buttons);
		}

		all.sort(Comparator.comparing(InviteData::getInviterName));

		buttons[DENY_ALL_BUTTON_SLOW] = new DenyAllButton(getPlugin());

		if (_filterBy == null || _filterBy.isEmpty())
		{
			buttons[FILTER_BUTTON_SLOT] = new FilterButton(getPlugin());
		} else
		{
			showFiltered = true;
			buttons[FILTER_BUTTON_SLOT] = new FilterButton(_filterBy, getPlugin());
		}

		if (showFiltered)
		{
			all = all.stream().filter(inviteData -> inviteData.getInviterName().contains(_filterBy)).collect(Collectors.toList());
		}

		if (showFiltered && all.isEmpty())
		{
			for (int i = 10; i < 44; i++)
			{
				buttons[i] = new IconButton(new ItemBuilder(Material.STAINED_GLASS_PANE)
				  .setData(DyeColor.RED.getWoolData())
				  .setTitle(C.cRedB + "No Invites Found")
				  .setLore(" ", C.cGray + "The filter " + F.name(_filterBy) + " had no results.")
				  .build());
			}
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, .6f);
			return pane(buttons);
		}

		_data = all;

		if (all.size() > SLOTS_PER_PAGE)
		{
			int pagesNeeded = 0;
			int size = all.size();
			while (size > SLOTS_PER_PAGE)
			{
				pagesNeeded++;
				size -= SLOTS_PER_PAGE;
			}
			buttons[NEXT_PAGE_SLOT] = new NextPageButton(this, getPlugin());

			_pagesNeeded = pagesNeeded;
			_pagesOfData = new HashMap<>();

			int page = 0;

			List<InviteData> newData = Lists.newArrayList();
			int total = all.size();
			for (int i = 0; i < pagesNeeded; i++)
			{
				for (int s = 0; s < total; s++)
				{
					newData.add(all.remove(i));
					if (i > SLOTS_PER_PAGE)
					{
						_pagesOfData.put(page++, newData);
						newData.clear();
						total -= SLOTS_PER_PAGE;
						break;
					}
				}
			}
			//Add pages
		} else
		{
			for (int i = 0; i < all.size(); i++)
			{
				String to = all.get(i).getInviterName();
				buttons[STARTING_SLOT + i] = new InviteButton(to, getPlugin());
			}
		}


		return pane(buttons);
	}

	public void setButton(int slot, Button button)
	{
		getButtons()[slot] = button;
	}

	public List<InviteData> getDataForPage(int page)
	{
		return _pagesOfData.get(page);
	}

	public int getStartingSlot()
	{
		return STARTING_SLOT;
	}

	public int getCurrentPage()
	{
		return _currentPage;
	}

	public void setCurrentPage(int currentPage)
	{
		_currentPage = currentPage;
	}

	public List<InviteData> getData()
	{
		return _data;
	}

	public int getPagesNeeded()
	{
		return _pagesNeeded;
	}
}