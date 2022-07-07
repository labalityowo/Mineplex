package mineplex.hub.modules.salesannouncements;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;

public class RankSelectionButton extends SalesAnnouncementGUIButton
{
	private SalesAnnouncementCreationPage _page;
	private PermissionGroup _rank;
	
	public RankSelectionButton(PermissionGroup rank, SalesAnnouncementCreationPage page)
	{
		super(new ItemBuilder(Material.REDSTONE_BLOCK).setTitle(rank.getDisplay(true, false, false, true)).addLore(C.cRed + "Click to Toggle On").build());
		_rank = rank;
		_page = page;
	}

	@Override
	public void update() {}

	@Override
	public void handleClick(ClickType type)
	{
		if (_page.Selected.contains(_rank))
		{
			Button = new ItemBuilder(Material.REDSTONE_BLOCK).setTitle(_rank.getDisplay(true, false, false, true)).addLore(C.cRed + "Click to Toggle On").build();
			_page.Selected.remove(_rank);
			_page.updateButtons(true);
		}
		else
		{
			Button = new ItemBuilder(Material.EMERALD_BLOCK).setTitle(_rank.getDisplay(true, false, false, true)).addLore(C.cGreen + "Click to Toggle Off").build();
			_page.Selected.add(_rank);
			_page.updateButtons(true);
		}
	}
}