package mineplex.core.report.ui;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.gui.SimpleGuiItem;
import mineplex.core.itemstack.ItemBuilder;

/**
 * A gui button which when clicked aborts the report the user is currently handling
 */
public class ReportAbortButton extends SimpleGuiItem
{
	private static final ItemStack ITEM = new ItemBuilder(Material.BEDROCK)
			.setTitle(C.cRed + "Abort")
			.addLore("Another member of staff may handle this report instead.")
			.build();

	private final ReportResultPage _page;

	public ReportAbortButton(ReportResultPage page)
	{
		super(ITEM);
		_page = page;
	}

	@Override
	public void click(ClickType clickType)
	{
		_page.abortReport();
	}
}
