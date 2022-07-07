package mineplex.staffServer.ui.ppc;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.powerplayclub.PowerPlayClubRewards;
import mineplex.core.powerplayclub.PowerPlayData;
import mineplex.core.shop.page.MultiPageManager;
import mineplex.staffServer.customerSupport.CustomerSupport;
import mineplex.staffServer.ui.SupportPage;
import mineplex.staffServer.ui.SupportShop;

public class SupportPowerplayPage extends SupportPage
{
	private MultiPageManager<PowerPlayData.Subscription> _multiPageManager;

	public SupportPowerplayPage(CustomerSupport plugin, SupportShop shop, Player player, CoreClient target, SupportPage previousPage)
	{
		super(plugin, shop, player, target, previousPage, "PPC");

		_multiPageManager = new MultiPageManager<>(this, this::getSubscriptions, this::buildPowerPlayItem);

		// 1 row is 7 items
		_multiPageManager.setElementsPerPage(21);

		buildPage();
	}

	private long getMilliseconds(YearMonth yearMonth)
	{
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.YEAR, yearMonth.getYear());
		cal.set(Calendar.MONTH, yearMonth.getMonthValue());

		return cal.getTimeInMillis();
	}

	List<YearMonth> getYearMonthRange(YearMonth start, int months)
	{
		List<YearMonth> range = new ArrayList<>();

		for (int i = 0; i < months; i++)
		{
			range.add(start.plusMonths(i));
		}

		return range;
	}

	public void buildPowerPlayItem(PowerPlayData.Subscription subscription, int slot)
	{
		// We don't care about the claims since they are generated below
		PowerPlayData subData = PowerPlayData.fromSubsAndClaims(Collections.singletonList(subscription), Collections.emptyList());

		LocalDate endDate = subscription._startDate.plusMonths(subscription._duration.getLength());

		List<YearMonth> relevantMonths = getYearMonthRange(YearMonth.from(subscription._startDate), subscription._duration.getLength());

		Set<YearMonth> unclaimedMonths = getPowerPlayData().getUnclaimedMonths()
				.stream()
				.filter(relevantMonths::contains)
				.collect(Collectors.toSet());

		ItemBuilder builder = new ItemBuilder(subData.isSubscribed() ? Material.GOLD_BLOCK : Material.IRON_BLOCK)
				.setTitle(C.cGreenB + subscription._duration.getName() + "ly Subscription")
				.addLore("")
				.addLore(C.cGray + "Start Date: " + C.cYellow + subscription._startDate.getMonth().getDisplayName(TextStyle.FULL, Locale.US) + " " + subscription._startDate.getDayOfMonth() + " " + subscription._startDate.getYear())
				.addLore(C.cGray + "End Date: " + C.cYellow + endDate.getMonth().getDisplayName(TextStyle.FULL, Locale.US) + " " + endDate.getDayOfMonth() + " " + endDate.getYear())
				.addLore(C.cGray + "Duration: " + C.cYellow + subscription._duration.getName())
				.addLore("")
				.addLore(C.cGreenB + "Subscription Cosmetics");

		PowerPlayClubRewards.rewards().entrySet().stream()
				.filter(entry -> subData.getUsableCosmeticMonths().contains(entry.getKey()))
				.sorted(Comparator.comparingLong(a -> getMilliseconds(a.getKey())))
				.forEach(entry ->
				{
					YearMonth yearMonth = entry.getKey();
					builder.addLore(C.cWhite + "  " + entry.getValue().getPrizeName() + " " + C.cGold + yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.US) + " " + yearMonth.getYear());
				});

		builder.addLore("");

		if (unclaimedMonths.isEmpty())
		{
			builder.addLore(C.cAqua + "All current rewards claimed");
		}
		else
		{
			builder.addLore(C.cRedB + "Unclaimed rewards");
			builder.addLore("  " + C.cWhite + "Cosmetics: " + C.cYellow + String.join(", ",
					unclaimedMonths
						.stream()
						.map(ym -> ym.getMonthValue() + "/" + ym.getYear())
						.collect(Collectors.toList())
			));
			builder.addLore("  " + C.cWhite + (PowerPlayClubRewards.AMPLIFIERS_PER_MONTH * subData.getUnclaimedMonths().size()) + " Game Amplifier");
			builder.addLore("  " + C.cWhite + (PowerPlayClubRewards.CHESTS_PER_MONTH * subData.getUnclaimedMonths().size()) + " Omega Chest");
		}

		addItem(slot, builder.build());
	}

	public PowerPlayData getPowerPlayData()
	{
		return getShop().getPowerPlayData().get(_target.getAccountId());
	}

	public List<PowerPlayData.Subscription> getSubscriptions()
	{
		List<PowerPlayData.Subscription> subscriptions = getPowerPlayData().getSubscriptions();

		subscriptions.sort((a, b) -> Long.compare(b._startDate.toEpochDay(), a._startDate.toEpochDay()));

		return subscriptions;
	}

	private ItemStack getSubscriptionIcon(PowerPlayData.SubscriptionDuration duration)
	{
		return new ItemBuilder(Material.GOLD_INGOT)
				.setAmount(duration.getLength())
				.setTitle(C.cGreenB + "Give " + duration.getLength() + " Month Subscription")
				.addLore(C.mBody + "Click to give" + C.cYellow + " 1 " + duration.getName())
				.addLore(C.mBody + "of PPC to " + C.cYellow + _target.getName())
				.build();
	}

	private void addSubscriptionButton(int slot, PowerPlayData.SubscriptionDuration duration)
	{
		addButton(slot, getSubscriptionIcon(duration), (p, c) ->
		{
			getPlugin().getPowerPlayRepo().addSubscription(_target.getAccountId(), LocalDate.now(), duration.getName().toLowerCase());
			getShop().loadPowerPlay(getPlayer(), _target.getAccountId(), (success) ->
			{
				message("Gave a" + C.cYellow + " 1 " + duration.getName() + C.mBody + " PPC subscription to " + C.cYellow + _target.getName());

				if (success)
				{
					refresh();
				}
				else
				{
					goBack();
				}
			});
		});
	}

	@Override
	protected void buildPage()
	{
		super.buildPage();

		_multiPageManager.buildPage();

		addSubscriptionButton(getSlotIndex(5, 3), PowerPlayData.SubscriptionDuration.MONTH);
		addSubscriptionButton(getSlotIndex(5, 5), PowerPlayData.SubscriptionDuration.YEAR);
	}
}
