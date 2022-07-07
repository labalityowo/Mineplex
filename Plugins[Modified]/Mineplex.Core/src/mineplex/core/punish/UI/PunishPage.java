package mineplex.core.punish.UI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTime;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.punish.Category;
import mineplex.core.punish.Punish;
import mineplex.core.punish.PunishClient;
import mineplex.core.punish.Punishment;
import mineplex.core.punish.PunishmentResponse;
import mineplex.core.shop.confirmation.ConfirmationCallback;
import mineplex.core.shop.confirmation.ConfirmationPage;
import mineplex.core.shop.confirmation.ConfirmationProcessor;
import mineplex.core.shop.page.ShopPageBase;

public abstract class PunishPage extends ShopPageBase<Punish, PunishShop>
{
	protected String _target;
	protected String _reason;

	protected Map<Category, Map<Integer, Integer>> _offenseMap;
	protected List<Punishment> _pastPunishments;

	public PunishPage(Punish punish, PunishShop shop, String name, Player player, String target, String reason)
	{
		super(punish, shop, punish.GetClients(), Managers.require(DonationManager.class), name, player);

		_target = target;
		_reason = reason;

		_offenseMap = new HashMap<>();
		_pastPunishments = new LinkedList<>();

		processOffenses();
	}

	protected ItemStack buildHistoryItem(Punishment punishment)
	{
		boolean canSeeAllDetails = getPlugin().GetClients().Get(getPlayer()).hasPermission(Punish.Perm.PUNISHMENT_COMMAND);

		List<String> lore = new ArrayList<>();

		lore.add(C.Reset + "Punishment Type: " + C.cYellow + punishment.GetCategory().getName());
		lore.add(C.Reset + "Severity: " + C.cYellow + punishment.GetSeverity());

		// Don't let nonstaff see who banned them
		if (canSeeAllDetails)
		{
			lore.add(C.Reset + "Staff: " + C.cYellow + punishment.GetAdmin());
		}

		lore.add(C.Reset + "Date: " + C.cYellow + UtilTime.when(punishment.GetTime()));

		if (!punishment.GetCategory().equals(Category.Warning))
		{
			lore.add(C.Reset + "Length: " + C.cYellow + UtilTime.convertString((long) punishment.GetHours()*UtilTime.TimeUnit.HOURS.getMilliseconds(), 1, UtilTime.TimeUnit.FIT));
		}

		lore.add("");
		lore.addAll(UtilText.splitLine(C.Reset + "Reason: " + C.cYellow + punishment.GetReason(), LineFormat.LORE));

		if (punishment.GetRemoved())
		{
			lore.add("");

			if (canSeeAllDetails)
			{
				lore.add(C.Reset + "Remove Staff: " + C.cYellow + punishment.GetRemoveAdmin());
			}

			lore.addAll(UtilText.splitLine(C.Reset + "Remove Reason: " + C.cYellow + punishment.GetRemoveReason(), LineFormat.LORE));
		}
		else
		{
			if (punishment.GetHours() > 0)
			{
				lore.add("");
				lore.add(C.Reset + "Expires On: " + C.cYellow + UtilTime.when(System.currentTimeMillis() + punishment.GetRemaining()));
				lore.add(C.cYellow + "(" + Punish.getDurationString(punishment.GetRemaining() / UtilTime.TimeUnit.HOURS.getMilliseconds()) + ")");
			}
		}

		ItemStack itemStack = new ItemStack(punishment.GetCategory().getIcon());

		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(C.cGreenB + punishment.GetCategory().getName());
		meta.setLore(lore);
		itemStack.setItemMeta(meta);

		if ((punishment.GetHours() == -1 || punishment.GetRemaining() > 0) && !punishment.GetRemoved() && punishment.GetActive())
		{
			itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		}

		return itemStack;
	}

	private boolean canBeReapplied(Punishment punishment)
	{
		if (!_client.hasPermission(Punish.Perm.PUNISHMENT_REAPPLY))
		{
			return false;
		}

		if (punishment.GetActive())
		{
			return false;
		}

		if (!punishment.GetRemoved())
		{
			return false;
		}

		if (punishment.GetHours() == -1)
		{
			return true;
		}

		if (punishment.GetRemaining() > 0)
		{
			return true;
		}

		return false;
	}

	protected void addHistoryItem(int slot, Punishment punishment)
	{
		ItemStack itemStack = buildHistoryItem(punishment);

		// It's not in the staff ui
		if (_reason == null)
		{
			addItem(slot, itemStack);
			return;
		}

		if ((punishment.GetHours() < 0 || punishment.GetRemaining() > 0) && !punishment.GetRemoved() && punishment.GetActive())
		{
			addButton(slot, itemStack, (p, c) ->
			{
				if (getPlugin().GetClients().Get(getPlayer()).hasPermission(Punish.Perm.BYPASS_REMOVE_CONFIRMATION))
				{
					removePunishment(punishment);
					return;
				}

				getShop().openPageForPlayer(getPlayer(), new ConfirmationPage<>(getPlayer(), this, new ConfirmationProcessor()
				{
					@Override
					public void init(Inventory inventory) {}

					@Override
					public void process(ConfirmationCallback callback)
					{
						removePunishment(punishment);
						callback.resolve("Successfully removed punishment.");
					}
				}, itemStack, "Confirm Punishment Removal"));
			});
		}
		else if (canBeReapplied(punishment))
		{
			ItemStack reapplyItem = new ItemBuilder(itemStack).addLore("", C.cGreen + "Click to reapply punishment").build();

			addButton(slot, reapplyItem, (p, c)->
					getShop().openPageForPlayer(getPlayer(), new ConfirmationPage<>(getPlayer(), this, new ConfirmationProcessor()
					{
						@Override
						public void init(Inventory inventory) {}

						@Override
						public void process(ConfirmationCallback callback)
						{
							reapplyPunishment(punishment);
							callback.resolve("Successfully reapplied punishment.");
						}
					}, itemStack, "Confirm Punishment Reapply")));
		}
		else
		{
			addItem(slot, itemStack);
		}
	}

	// nobody knows how or why this works
	public int getDuration(Category category, int severity)
	{
		if (category == Category.ChatOffense)
		{
			int hours = 0;

			if (severity >= 1)
			{
				hours += calculateTime(2, 2, 48, _offenseMap.get(category).get(1), severity != 1);
			}
			if (severity >= 2)
			{
				hours += calculateTime(24, 24, 168, _offenseMap.get(category).get(2), severity != 2);
			}
			if (severity >= 3)
			{
				hours += calculateTime(720, 720, 720, _offenseMap.get(category).get(3), severity != 3);
			}

			return hours;
		}

		if (category == Category.Exploiting)
		{
			int hours = 0;

			if (severity >= 1)
			{
				hours += calculateTime(4, 4, 96, _offenseMap.get(category).get(1), severity != 1);
			}
			if (severity >= 2)
			{
				hours += calculateTime(48, 48, 336, _offenseMap.get(category).get(2), severity != 2);
			}
			if (severity >= 3)
			{
				return -1;
			}

			return hours;
		}

		if (category == Category.Hacking)
		{
			int hours = 0;

			if (severity >= 1)
			{
				hours += calculateTime(24, 24, 168, _offenseMap.get(category).get(1), severity != 1);
			}
			if (severity >= 2)
			{
				if (_offenseMap.get(category).get(2) > 1)
				{
					hours = 960;
				} else
				{
					hours = 720;
				}
			}
			if (severity >= 3)
			{
				hours = 960;
			}

			return hours;
		}

		if (category == Category.Other || category == Category.PermMute || category == Category.ReportAbuse)
		{
			return -1;
		}

		return 0;
	}

	protected int calculateTime(int baseAmount, int addAmount, int pastLimit, int offenses, boolean zeroBase)
	{
		int amount = 0;

		if (zeroBase)
		{
			baseAmount = 0;
		}

		// At what point does Bonus > pastLimit
		int breakLimitCount = 0;
		while (baseAmount + addAmount * breakLimitCount * breakLimitCount < pastLimit)
		{
			breakLimitCount++;
		}

		amount += Math.min(baseAmount + addAmount * offenses * offenses, pastLimit);
		amount += Math.max(0, (offenses - breakLimitCount) * pastLimit);

		return amount;
	}

	protected void doPunishment(Category category, int severity, long length)
	{
		getPlugin().AddPunishment(_target, category, _reason, getPlayer(), severity, category.isBan(), length);
		getPlayer().closeInventory();
	}

	protected void doPunishment(Category category, int severity)
	{
		doPunishment(category, severity, getDuration(category, severity));
	}

	protected void reapplyPunishment(Punishment punishment)
	{
		if (!canBeReapplied(punishment))
		{
			return;
		}

		_reason = punishment.GetReason() + " - Reapplied";

		if (punishment.GetRemaining()  == -1)
		{
			doPunishment(punishment.GetCategory(), punishment.GetSeverity(), -1);
		}
		else
		{
			doPunishment(punishment.GetCategory(), punishment.GetSeverity(), (long) Math.floor(punishment.GetRemaining() / UtilTime.TimeUnit.HOURS.getMilliseconds()));
		}
	}

	protected void removePunishment(Punishment punishment)
	{
		getPlugin().RemovePunishment(punishment, _target, getPlayer(), _reason, result ->
		{
			PunishmentResponse punishResponse = PunishmentResponse.valueOf(result);

			if (punishResponse != PunishmentResponse.PunishmentRemoved)
			{
				getPlayer().sendMessage(F.main(_plugin.getName(), "There was a problem removing the punishment."));
			}
			else
			{
				punishment.Remove(getPlayer().getName(), _reason);
				getPlayer().closeInventory();
			}
		});
	}

	protected void processOffenses()
	{
		PunishClient client = getPlugin().GetClient(_target);

		for (Category category : Category.values())
		{
			_offenseMap.put(category, new HashMap<>());

			_offenseMap.get(category).put(1, 0);
			_offenseMap.get(category).put(2, 0);
			_offenseMap.get(category).put(3, 0);
		}

		for (Category category : client.GetPunishments().keySet())
		{
			for (Punishment punishment : client.GetPunishments().get(category))
			{
				_pastPunishments.add(punishment);

				//Count by Severity
				if (!punishment.GetRemoved() && punishment.GetSeverity() > 0 && punishment.GetSeverity() < 4)
				{
					_offenseMap.get(category).put(punishment.GetSeverity(), 1 + _offenseMap.get(category).get(punishment.GetSeverity()));
				}
			}
		}

		_pastPunishments.sort((a, b) -> Long.compare(b.GetTime(), a.GetTime()));
	}
}