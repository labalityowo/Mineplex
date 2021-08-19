package mineplex.core.report.ui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.gui.SimpleGuiItem;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.report.ReportTeam;

/**
 * A gui button which when clicked will forward a report onto the supplied team.
 */
public class ReportAssignTeamButton extends SimpleGuiItem
{
	private static final Map<ReportTeam, ItemStack> DISPLAY_ITEMS = new HashMap<ReportTeam, ItemStack>(){{
		ItemStack rcItem = new ItemBuilder(Material.PAPER)
				.setTitle(C.cAqua + "Forward to RC")
				.addLore("A member of the rules committee will review this report instead.")
				.build();
		put(ReportTeam.RC, rcItem);
	}};

	private final ReportResultPage _page;
	private final ReportTeam _team;

	public ReportAssignTeamButton(ReportResultPage page, ReportTeam team)
	{
		this(page, team, DISPLAY_ITEMS.get(team));
	}

	public ReportAssignTeamButton(ReportResultPage page, ReportTeam team, ItemStack displayItem)
	{
		super(displayItem);
		_page = page;
		_team = team;
	}

	@Override
	public void click(ClickType clickType)
	{
		_page.assignTeam(_team);
	}
}
