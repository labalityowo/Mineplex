package mineplex.core.report.ui;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.util.C;
import mineplex.core.gui.SimpleGuiItem;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.report.ReportResultType;

/**
 * Represents a button which can be clicked to determine the result of a report.
 */
public class ReportResultButton extends SimpleGuiItem
{
	// initialize button display itemsR
	private static final Map<ReportResultType, ItemStack> ITEM_STACKS = new EnumMap<ReportResultType, ItemStack>(ReportResultType.class)
	{{
		ItemStack itemAccept = new ItemBuilder(Material.WOOL)
				.setData(DyeColor.GREEN.getData())
				.setTitle(C.cGreen + "Accept Report")
				.addLore("%suspect% is guilty without a doubt.")
				.build();
		put(ReportResultType.ACCEPTED, itemAccept);

		ItemStack itemDeny = new ItemBuilder(Material.WOOL)
				.setData(DyeColor.YELLOW.getData())
				.setTitle(C.cYellow + "Deny Report")
				.addLore("There is not enough evidence against %suspect%.")
				.build();
		put(ReportResultType.DENIED, itemDeny);

		ItemStack itemAbuse = new ItemBuilder(Material.WOOL)
				.setData(DyeColor.RED.getData())
				.setTitle(C.cRed + "Flag Abuse")
				.addLore("The reporter(s) were abusing the report system.")
				.build();
		put(ReportResultType.ABUSIVE, itemAbuse);
	}};

	private ReportResultPage _page;
	private ReportResultType _resultType;

	public ReportResultButton(ReportResultPage page, ReportResultType resultType)
	{
		this(page, resultType, ITEM_STACKS.get(resultType));
	}

	public ReportResultButton(ReportResultPage page, ReportResultType resultType, ItemStack displayItem)
	{
		super(displayItem);
		_page = page;
		_resultType = resultType;
	}

	@Override
	public void setup()
	{
		// replace all occurrences of "%suspect%" in the lore with the actual name
		ItemMeta itemMeta = getItemMeta();
		List<String> lore = itemMeta.getLore();

		for (int i = 0; i < lore.size(); i++)
		{
			lore.set(i, lore.get(i).replace("%suspect%", _page.getSuspectName()));
		}

		itemMeta.setLore(lore);
		setItemMeta(itemMeta);
	}

	@Override
	public void click(ClickType clickType)
	{
		_page.closeReport(_resultType);
	}
}
