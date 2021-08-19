package mineplex.staffServer.ui.rank;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.MultiPageManager;
import mineplex.staffServer.customerSupport.CustomerSupport;
import mineplex.staffServer.repository.BonusEntry;
import mineplex.staffServer.ui.SupportPage;
import mineplex.staffServer.ui.SupportShop;

public class SupportRankBonusPage extends SupportPage
{
	private MultiPageManager<BonusEntry> _multiPageManager;

	public SupportRankBonusPage(CustomerSupport plugin, SupportShop shop, Player player, CoreClient target, SupportPage previousPage)
	{
		super(plugin, shop, player, target, previousPage, "Rank Bonus");

		getBonusLog().sort((a, b) -> Long.compare(b.getCalendar().getTimeInMillis(), a.getCalendar().getTimeInMillis()));

		_multiPageManager = new MultiPageManager<>(this, this::getBonusLog, this::buildEntry);

		buildPage();
	}

	private List<BonusEntry> getBonusLog()
	{
		return getShop().getBonusLog().get(_target.getAccountId());
	}

	private void buildEntry(BonusEntry entry, int slot)
	{
		addItem(slot, new ShopItem(Material.BOOK, new SimpleDateFormat("MMMM").format(entry.getCalendar().getTime()), new String[] {
				"",
				C.cGray + "Time: " + C.cYellow + new SimpleDateFormat("MMMM dd YYYY").format(entry.getCalendar().getTime()),
				C.cGray + "Item Received: " + C.cYellow + entry.getItemName(),
				C.cGray + "Amount Received: " + C.cYellow + entry.getItemCount()
		}, entry.getCalendar().get(Calendar.MONTH) + 1, false, true));
	}

	private ItemStack getLogIcon()
	{
		int entries = Math.min(6, getBonusLog().size());
		ItemBuilder builder = new ItemBuilder(Material.SIGN).setTitle(C.cGreenB + "Last " + entries + " Entries").addLore("");

		if (entries == 0)
		{
			builder.addLore(C.mBody + "No entries to display.");
		}

		for (int i = 0; i < entries; i++)
		{
			BonusEntry entry = getBonusLog().get(i);
			builder.addLore(C.mBody + "Received " + C.cYellow + entry.getItemCount() + " " + entry.getItemName() + C.mBody + " on " + C.cYellow + new SimpleDateFormat("MMMM dd YYYY").format(entry.getCalendar().getTime()));
		}

		return builder.build();
	}

	@Override
	protected void buildPage()
	{
		super.buildPage();

		_multiPageManager.buildPage();

		addItem(getSlotIndex(5, 4), getLogIcon());
	}
}
