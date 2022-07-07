package mineplex.core.punish.UI.history;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.util.C;
import mineplex.core.punish.Punish;
import mineplex.core.punish.Punishment;
import mineplex.core.punish.UI.PunishPage;
import mineplex.core.punish.UI.PunishShop;
import mineplex.core.punish.UI.staff.PunishStaffPage;
import mineplex.core.shop.item.ShopItem;

public class PunishHistoryPage extends PunishPage
{
	private static final int ELEMENTS_PER_PAGE = 28;

	private int _page;

	private boolean _inStaffGui;
	private PunishStaffPage _previousPage;

	public PunishHistoryPage(Punish punish, PunishShop shop, Player player, String target, boolean inStaffGui, String reason, PunishStaffPage previousPage)
	{
		super(punish, shop, "History - " + target, player, target, reason);

		_inStaffGui = inStaffGui;
		_previousPage = previousPage;

		buildPage();
	}

	private void buildBackButton()
	{
		if (!_inStaffGui || _previousPage == null)
		{
			return;
		}

		ItemStack icon = new ItemStack(Material.BED);

		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(C.cGreenB + "Go Back");
		meta.setLore(Arrays.asList(new String[0]));

		icon.setItemMeta(meta);

		addButton(4, icon, (p, c) -> getShop().openPageForPlayer(getPlayer(), _previousPage));
	}

	@Override
	protected void buildPage()
	{
		List<Punishment> punishments = _pastPunishments;

		if (punishments == null)
		{
			return;
		}

		int slot = 10;
		int startIndex = _page * ELEMENTS_PER_PAGE;
		int endIndex = startIndex + ELEMENTS_PER_PAGE;

		punishments = punishments.subList(startIndex, Math.min(endIndex, punishments.size()));

		for (Punishment punishment : punishments)
		{
			addHistoryItem(slot, punishment);

			if (++slot % 9 == 8)
			{
				slot += 2;
			}
		}

		if (_page != 0)
		{
			addButton(45, new ShopItem(Material.ARROW, C.cGreen + "Previous Page", new String[0], 1, false), (player, clickType) ->
			{
				_page--;
				refresh();
			});
		}
		if (endIndex <= _pastPunishments.size())
		{
			addButton(53, new ShopItem(Material.ARROW, C.cGreen + "Next Page", new String[0], 1, false), (player, clickType) ->
			{
				_page++;
				refresh();
			});
		}

		buildBackButton();
	}
}
