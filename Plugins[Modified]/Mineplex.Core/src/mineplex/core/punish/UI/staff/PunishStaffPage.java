package mineplex.core.punish.UI.staff;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.account.CoreClient;
import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilSkull;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTime;
import mineplex.core.punish.Category;
import mineplex.core.punish.Punish;
import mineplex.core.punish.Punishment;
import mineplex.core.punish.UI.PunishPage;
import mineplex.core.punish.UI.PunishShop;
import mineplex.core.punish.UI.history.PunishHistoryPage;
import mineplex.core.shop.confirmation.ConfirmationCallback;
import mineplex.core.shop.confirmation.ConfirmationPage;
import mineplex.core.shop.confirmation.ConfirmationProcessor;
import mineplex.core.shop.item.ShopItem;

public class PunishStaffPage extends PunishPage
{
	private static final String _punishGuidelinesInfo = C.mBody + "Refer to the guidelines for info.";
	private static final int _pastPunishCount = 6;
	private static final int _pastPunishColumn = 8;
	private static final int _customButtonColumn = 0;
	// Row, Col
	private static final Pair<Integer, Integer> _moreHistoryButton = Pair.create(5, 8);

	private boolean _wasDisguised;
	private String _originalName;
	private String _disguisedName;

	public PunishStaffPage(Punish punish, PunishShop shop, Player player, String target, String reason, boolean wasDisguised, String originalName, String disguisedName)
	{
		super(punish, shop, "Punish - " + target, player, target, reason);

		this._wasDisguised = wasDisguised;
		this._originalName = originalName;
		this._disguisedName = disguisedName;

		buildPage();
	}

	private int getMaxSeverity(Category category)
	{
		return (getPlugin().GetClients().Get(_player).hasPermission(Punish.Perm.FULL_PUNISHMENT_ACCESS)) ? category.getMaxSeverity() : 1;
	}

	private ItemStack buildPlayerSkull()
	{
		return UtilSkull.getPlayerHead(_target, C.cGreenB + _target, UtilText.splitLine(C.Reset + "Reason: " + C.mBody + _reason, LineFormat.LORE));
	}

	private ShopItem buildDummyCategoryIcon(Category category)
	{
		return new ShopItem(category.getIcon(), category.getName(), new String[]{_punishGuidelinesInfo}, 1, false, true);
	}

	private ItemStack buildSeverityIcon(Category category, int severity)
	{
		int data;
		if (severity == 1)
		{
			// Green block 5 / dye is 2
			data = 2;
		} else if (severity == 2)
		{
			// Yellow block 4 / dye is 11
			data = 11;
		} else if (severity == 3)
		{
			// Red block 14 / dye is 1
			data = 1;
		} else
		{
			return null;
		}

		ItemStack itemStack = new ItemStack(Material.INK_SACK, 1, (byte) data);

		ItemMeta itemMeta = itemStack.getItemMeta();

		itemMeta.setDisplayName(getSevChatColor(severity) + "Severity " + severity);

		itemMeta.setLore(Arrays.asList(
				C.Reset + "Past Offenses: " + C.cYellow + _offenseMap.get(category).get(severity),
				C.Reset + "Duration: " + C.cYellow + getDurationString(category, severity),
				"",
				_punishGuidelinesInfo));

		itemStack.setItemMeta(itemMeta);

		return itemStack;
	}

	// Build one of the three primary categories...
	private void buildMainCategory(Category category, int columnOffset)
	{
		addItem(getSlot(1, columnOffset), buildDummyCategoryIcon(category));

		for (int sev = 1; sev <= getMaxSeverity(category); sev++)
		{
			AtomicInteger aSeverity = new AtomicInteger(sev);
			AtomicReference<Category> aCategory = new AtomicReference<>(category);

			addButton(getSlot(sev + 1, columnOffset), buildSeverityIcon(category, sev), (p, c)-> doPunishmentCustom(aCategory.get(), aSeverity.get()));
		}
	}

	private String getSevChatColor(int sev)
	{
		if (sev == 1)
		{
			return C.cGreenB;
		}

		if (sev == 2)
		{
			return C.cYellowB;
		}

		if (sev == 3)
		{
			return C.cRedB;
		}

		return C.cGreenB;
	}

	private ItemStack buildMoreHistoryButton()
	{
		ItemStack item = new ItemStack(Material.SIGN, Math.min(_pastPunishments.size(), 64));

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(C.cGreenB + "More History");
		meta.setLore(Arrays.asList(
				C.cYellow + _target + C.mBody + " has " + C.cYellow + _pastPunishments.size(),
				C.mBody + "punishments on their",
				C.mBody + "record.",
				"",
				C.cGreen + "Click to view them!"
			));

		item.setItemMeta(meta);

		return item;
	}

	private void buildHistory()
	{
		for (int i = 0; i < Math.min(_pastPunishments.size(), _pastPunishCount); i++)
		{
			Punishment punishment = _pastPunishments.get(i);

			addHistoryItem(getSlot(i, _pastPunishColumn), punishment);
		}

		if (_pastPunishments.size() > _pastPunishCount)
		{
			addButton(getSlot(_moreHistoryButton.getLeft(),
					_moreHistoryButton.getRight()),
					buildMoreHistoryButton(),
					(p, c)-> getShop().openPageForPlayer(getPlayer(), new PunishHistoryPage(getPlugin(), getShop(), getPlayer(), _target, true, _reason, this)));
		}
	}

	// Builds perm, warn, report ban
	private void buildCustomButtons()
	{
		CoreClient coreClient = getPlugin().GetClients().Get(_player);

		if (coreClient.hasPermission(Category.Warning.getNeededPermission()))
		{
			addButton(getSlot(1, _customButtonColumn), buildDummyCategoryIcon(Category.Warning), (p, c) -> doPunishmentCustom(Category.Warning, 1));
		}

		if (coreClient.hasPermission(Category.Other.getNeededPermission()))
		{
			addButton(getSlot(4, _customButtonColumn), buildDummyCategoryIcon(Category.Other), (p, c) -> doPunishmentCustom(Category.Other, 1));
		}

		if (coreClient.hasPermission(Category.PermMute.getNeededPermission()))
		{
			addButton(getSlot(3, _customButtonColumn), buildDummyCategoryIcon(Category.PermMute), (p, c) -> doPunishmentCustom(Category.PermMute, 1));
		}

		if (coreClient.hasPermission(Category.ReportAbuse.getNeededPermission()))
		{
			addButton(getSlot(2, _customButtonColumn), buildDummyCategoryIcon(Category.ReportAbuse), (p, c) -> doPunishmentCustom(Category.ReportAbuse, 1));
		}
	}

	private ItemStack buildDisguiseIcon()
	{
		if (!_wasDisguised || _disguisedName == null || _originalName == null)
		{
			return null;
		}

		ItemStack item = new ItemStack(Material.NETHER_STAR);

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(C.cGreenB + "Disguise Warning");
		meta.setLore(Arrays.asList(UtilText.splitLinesToArray(new String[]
				{
						ChatColor.RESET + "The player you are attempting to punish is disguised.",
						ChatColor.RESET + "Original Name: " + ChatColor.GREEN + ChatColor.BOLD + _originalName,
						ChatColor.RESET + "Disguised Name: " + ChatColor.GREEN + ChatColor.BOLD + _disguisedName,
				}, LineFormat.LORE)));
		item.setItemMeta(meta);

		return item;
	}

	private String getDurationString(Category category, int severity)
	{
		int hours = getDuration(category, severity);

		if (hours == -1)
		{
			return "Permanent";
		}

		return UtilTime.MakeStr((long) hours * 3600000L);
	}

	private void doPunishmentCustom(Category category, int severity)
	{
		if (_wasDisguised)
		{
			getShop().openPageForPlayer(getPlayer(), new ConfirmationPage<>(getPlayer(), this, new ConfirmationProcessor()
			{
				@Override
				public void init(Inventory inventory)
				{

				}

				@Override
				public void process(ConfirmationCallback callback)
				{
					doPunishment(category, severity);
				}
			}, buildDisguiseIcon()));
		}
		else
		{
			doPunishment(category, severity);
		}
	}

	@Override
	protected void buildPage()
	{
		// First row, center
		addItem(4, buildPlayerSkull());

		buildMainCategory(Category.ChatOffense, 2);
		buildMainCategory(Category.Exploiting, 4);
		buildMainCategory(Category.Hacking, 6);

		buildHistory();
		buildCustomButtons();

		if (_wasDisguised)
		{
			addItem(getSlot(5, 4), buildDisguiseIcon());
		}
	}
}
