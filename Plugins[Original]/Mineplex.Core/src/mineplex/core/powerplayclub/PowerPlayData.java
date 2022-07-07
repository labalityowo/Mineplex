package mineplex.core.powerplayclub;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

public class PowerPlayData
{
	/* If this is set, the player's subscription is planned to recur.
	 *
	 * In the case of a yearly subscription, this will be the first day of
	 * the next month, or the exact end-of-subscription date if it's his/her
	 * last month.
	 *
	 * Corollary: If this isn't Optional.empty(), the player is actively subscribed.
	 */
	private final Optional<LocalDate> _nextClaimDate;

	// The source set of subscriptions from which this was built
	private final List<Subscription> _subscriptions;

	// The months where the player hasn't claimed chests/amplifiers
	private final Set<YearMonth> _unclaimedMonths;

	/* The months of PPC cosmetics the player owns / can use.
	 * In the case of, e.g., a monthly subscription, this will include
	 * the current month's cosmetic until _nextClaimDate passes (the
	 * subscription expires), unless the player's subscription recurs.
	 */
	private final Set<YearMonth> _cosmeticMonths;

	public static PowerPlayData fromSubsAndClaims(List<Subscription> subscriptions, List<YearMonth> claimedMonths)
	{
		if (subscriptions.isEmpty())
		{
			return new PowerPlayData(subscriptions, Optional.empty(), new HashSet<>(), new HashSet<>());
		}

		final LocalDate today = LocalDate.now();
		final YearMonth thisMonth = YearMonth.now();

		// Build the list of potential claim dates from subscriptions
		// Note that it's a LinkedList with dates in ascending order
		List<LocalDate> claimDates = subscriptions.stream()
				.flatMap(sub -> buildMonths(sub).stream())
				.sorted()
				.collect(Collectors.toCollection(LinkedList::new));

		// Determine the player's next claim date (which will tell us whether
		// they're subscribed as well)
		final Optional<LocalDate> nextClaimDate;

		// In the case of a yearly subscription, they're likely to have a claim date scheduled
		// (this is not the case for the last month)
		Optional<LocalDate> nextSubClaim = claimDates.stream().filter(date -> date.isAfter(today)).findFirst();
		if (nextSubClaim.isPresent())
		{
			nextClaimDate = nextSubClaim;

		} else
		{
			// In the case of a monthly subscription, we need to extrapolate the next claim date
			nextClaimDate = Optional.of(claimDates.get(claimDates.size() - 1))
					.map(date -> date.plusMonths(1))
					.filter(date -> date.equals(today) || date.isAfter(today)); // and make sure it's today or later
			nextClaimDate.ifPresent(claimDates::add);
		}

		// Determine the months whose cosmetics can be used by this player
		Set<YearMonth> cosmeticMonths = claimDates.stream()
				.map(YearMonth::from)
				.filter(yearMonth -> yearMonth.isBefore(thisMonth) || yearMonth.equals(thisMonth))
				.collect(Collectors.toSet());

		// Remove already-claimed months
		Optional<YearMonth> latestClaimed = claimedMonths.stream().collect(Collectors.maxBy(YearMonth::compareTo));
		latestClaimed.ifPresent(latest ->
		{
			while (!claimDates.isEmpty())
			{
				LocalDate claimDate = claimDates.get(0);
				YearMonth claimMonth = YearMonth.from(claimDate);
				if (latest.equals(claimMonth) || latest.isAfter(claimMonth))
				{
					claimDates.remove(0);

				} else
				{
					break;
				}
			}
		});

		Set<YearMonth> unclaimedMonths = claimDates.stream()
				.filter(date -> date.isBefore(today) || date.equals(today)) // Filter dates yet to come
				.map(YearMonth::from)
				// Only months that have not been claimed or are before the newest claim can be considered unclaimed
				.filter(ym -> !(claimedMonths.contains(ym) || (latestClaimed.isPresent() && latestClaimed.get().isAfter(ym))))
				.collect(Collectors.toSet());

		return new PowerPlayData(subscriptions, nextClaimDate, unclaimedMonths, cosmeticMonths);
	}

	private static List<LocalDate> buildMonths(Subscription subscription)
	{
		switch (subscription._duration)
		{
			case MONTH:
				return Collections.singletonList(subscription._startDate);

			case YEAR:
				List<LocalDate> months = new ArrayList<>();

				// The first and last months have the exact claim dates
				// The latter is to prevent premature claiming
				months.add(subscription._startDate);
				months.add(subscription._startDate.plusMonths(11));

				// The middle months can claim on the first day of the month
				for (int i = 1; i < 11; i++)
				{
					months.add(subscription._startDate.plusMonths(i).withDayOfMonth(1));
				}
				return months;

			default:
				throw new IllegalStateException("Invalid duration");
		}
	}

	public static class Subscription
	{
		public final LocalDate _startDate;
		public final SubscriptionDuration _duration;

		Subscription(LocalDate startDate, SubscriptionDuration duration)
		{
			_startDate = startDate;
			_duration = duration;
		}
	}

	public static enum SubscriptionDuration
	{
		MONTH("Month", 1), YEAR("Year", 12);

		private String _name;
		private int _length;

		SubscriptionDuration(String name, int length)
		{
			_name = name;
			_length = length;
		}

		public String getName()
		{
			return _name;
		}

		public int getLength()
		{
			return _length;
		}
	}

	private PowerPlayData(List<Subscription> subscriptions, Optional<LocalDate> nextClaimDate, Set<YearMonth> unclaimedMonths, Set<YearMonth> cosmeticMonths)
	{
		_subscriptions = subscriptions;
		_nextClaimDate = nextClaimDate;
		_unclaimedMonths = unclaimedMonths;
		_cosmeticMonths = cosmeticMonths;
	}

	public List<Subscription> getSubscriptions()
	{
		return _subscriptions;
	}

	public Optional<LocalDate> getNextClaimDate()
	{
		return _nextClaimDate;
	}

	public Set<YearMonth> getUnclaimedMonths()
	{
		return _unclaimedMonths;
	}

	public Set<YearMonth> getUsableCosmeticMonths()
	{
		return _cosmeticMonths;
	}

	public boolean isSubscribed()
	{
		return _nextClaimDate.isPresent();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.add("_nextClaimDate", _nextClaimDate)
				.add("_unclaimedMonths", _unclaimedMonths)
				.add("_cosmeticMonths", _cosmeticMonths)
				.toString();
	}
}
