package mineplex.core.report.ui;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.gui.SimpleGuiItem;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.report.ReportCategory;

/**
 * Represents a clickable button in a {@link ReportCreatePage} which determines the type of infraction a player has committed.
 */
public class ReportCategoryButton extends SimpleGuiItem
{
	// initialize the display items we use
	private static final Map<ReportCategory, ItemStack> ITEM_STACKS = new EnumMap<ReportCategory, ItemStack>(ReportCategory.class)
	{{
		ItemStack itemHack = new ItemBuilder(Material.INK_SACK)
				.setData((short) 1)
				.setTitle(C.cRedB + "Hacking")
				.addLore(C.cGray + "X-ray, Forcefield, Speed, Fly etc")
				.build();
		put(ReportCategory.HACKING, itemHack);

		ItemStack itemChatAbuse = new ItemBuilder(Material.INK_SACK)
				.setData((short) 11)
				.setTitle(C.cYellowB + "Chat Abuse")
				.addLore(C.cGray + "Verbal Abuse, Spam, Harassment, Trolling, etc")
				.build();
		put(ReportCategory.CHAT_ABUSE, itemChatAbuse);

		ItemStack itemGameplay = new ItemBuilder(Material.INK_SACK)
				.setData((short) 2)
				.setTitle(C.cGreenB + "Gameplay")
				.addLore(C.cGray + "Map and Bug Exploits")
				.build();
		put(ReportCategory.GAMEPLAY, itemGameplay);
	}};

	private final ReportCategoryCallback _callback;
	private final ReportCategory _category;

	public ReportCategoryButton(ReportCategoryCallback callback, ReportCategory reportCategory)
	{
		this(callback, reportCategory, ITEM_STACKS.get(reportCategory));
	}

	public ReportCategoryButton(ReportCategoryCallback callback, ReportCategory reportCategory, ItemStack itemStack)
	{
		super(itemStack);
		_callback = callback;
		_category = reportCategory;
	}

	public ReportCategory getCategory()
	{
		return _category;
	}

	@Override
	public void click(ClickType clickType)
	{
		_callback.click(this);
	}
}
