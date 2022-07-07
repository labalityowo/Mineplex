package mineplex.hub.modules.salesannouncements;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;

public class RankSelectionFinalizeButton extends SalesAnnouncementGUIButton
{
	private SalesAnnouncementCreationPage _page;
	
	public RankSelectionFinalizeButton(SalesAnnouncementCreationPage page)
	{
		super(new ItemBuilder(Material.REDSTONE_BLOCK).setTitle(C.cRed + "Click to Finalize").addLore(C.cRed + "You must select at least one rank!").build());
		_page = page;
	}

	@Override
	public void update()
	{
		if (_page.Selected.isEmpty())
		{
			Button = new ItemBuilder(Material.REDSTONE_BLOCK).setTitle(C.cRed + "Click to Finalize").addLore(C.cRed + "You must select at least one rank!").build();
		}
		else
		{
			Button = new ItemBuilder(Material.EMERALD_BLOCK).setTitle(C.cGreen + "Click to Finalize").build();
		}
	}

	@Override
	public void handleClick(ClickType type)
	{
		if (!_page.Selected.isEmpty())
		{
			_page.finalizeSelection();
		}
	}
}