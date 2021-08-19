package mineplex.core.powerplayclub;

import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

import mineplex.core.Managers;
import mineplex.core.common.util.BukkitFuture;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.donation.DonationManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.inventory.data.Item;
import mineplex.core.pet.PetClient;
import mineplex.core.pet.PetManager;
import mineplex.core.pet.PetType;

public class PowerPlayClubRewards
{
	public static final int AMPLIFIERS_PER_MONTH = 2;
	public static final int CHESTS_PER_MONTH = 1;

	private static final Map<YearMonth, PowerPlayClubItem> rewards = ImmutableMap.<YearMonth, PowerPlayClubItem>builder()
			.put(YearMonth.of(2016, Month.SEPTEMBER), new UnknownSalesPackageItem("Squid Morph"))
			.put(YearMonth.of(2016, Month.OCTOBER), new UnknownSalesPackageItem("Witch Morph"))
			.put(YearMonth.of(2016, Month.NOVEMBER), new UnknownSalesPackageItem("Turkey Morph"))
			.put(YearMonth.of(2016, Month.DECEMBER), new UnknownSalesPackageItem("Santa Morph"))
			.put(YearMonth.of(2017, Month.JANUARY), new UnknownSalesPackageItem("Over Easy Morph"))
			.put(YearMonth.of(2017, Month.FEBRUARY), new PetItem(PetType.TRUE_LOVE_PET))
			.put(YearMonth.of(2017, Month.MARCH), new UnknownSalesPackageItem("Gold Pot Morph"))
			.put(YearMonth.of(2017, Month.APRIL), new UnknownSalesPackageItem("Bumblebee's Wings"))
			.put(YearMonth.of(2017, Month.MAY), new UnknownSalesPackageItem("King"))
			.put(YearMonth.of(2017, Month.JUNE), new UnknownSalesPackageItem("Bob Ross Morph"))
			.put(YearMonth.of(2017, Month.JULY), new UnknownSalesPackageItem("Freedom Fighter"))
			.put(YearMonth.of(2017, Month.AUGUST), new UnknownSalesPackageItem("Melonhead Morph"))
			.put(YearMonth.of(2017, Month.SEPTEMBER), new UnknownSalesPackageItem("Tornado"))
			.put(YearMonth.of(2017, Month.OCTOBER), new UnknownSalesPackageItem("Ghast Morph"))
			.put(YearMonth.of(2017, Month.NOVEMBER), new UnknownSalesPackageItem("Tic Tac Toe"))
			.put(YearMonth.of(2017, Month.DECEMBER), new UnknownSalesPackageItem("Sledge Mount"))
			.put(YearMonth.of(2018, Month.JANUARY), new UnknownSalesPackageItem("Mob Bomb"))
			.put(YearMonth.of(2018, Month.FEBRUARY), new UnknownSalesPackageItem("Play Catch"))
			.put(YearMonth.of(2018, Month.MARCH), new UnknownSalesPackageItem("Connect 4"))
			.put(YearMonth.of(2018, Month.APRIL), new UnknownSalesPackageItem("Nanny's Umbrella"))
			.put(YearMonth.of(2018, Month.MAY), new UnknownSalesPackageItem("Windup"))
			.put(YearMonth.of(2018, Month.JUNE), new UnknownSalesPackageItem("Chicken Taunt"))
			.put(YearMonth.of(2018, Month.JULY), new UnknownSalesPackageItem("Clacker Boomerang"))
			.put(YearMonth.of(2018, Month.AUGUST), new UnknownSalesPackageItem("Gate of Babylon"))
			.put(YearMonth.of(2018, Month.SEPTEMBER), new UnknownSalesPackageItem("Grappling Hook"))
			.build();

	public interface PowerPlayClubItem
	{
		// The name of the Power Play Club prize to be shown as lore in Carl's GUI
		String getPrizeName();
		// Give the player this reward
		void reward(Player player);
	}

	private static final PowerPlayClubItem MISSING = new PowerPlayClubItem()
	{
		@Override
		public String getPrizeName() { return "Coming soon!"; }

		@Override
		public void reward(Player player) { }
	};

	private static class UnknownSalesPackageItem implements PowerPlayClubItem
	{
		private static final DonationManager _donationManager = Managers.require(DonationManager.class);
		private final String _name;

		UnknownSalesPackageItem(String name)
		{
			_name = name;
		}

		@Override
		public String getPrizeName()
		{
			return _name;
		}

		@Override
		public void reward(Player player)
		{
			_donationManager.Get(player).addOwnedUnknownSalesPackage(_name);
		}
	}

	private static class PetItem implements PowerPlayClubItem
	{
		private final PetType _type;

		PetItem(PetType type)
		{
			_type = type;
		}

		@Override
		public String getPrizeName()
		{
			return _type.getName() + " Pet";
		}

		@Override
		public void reward(Player player)
		{
			PetManager petManager = Managers.get(PetManager.class);
			if (petManager != null)
			{
				PetClient client = petManager.Get(player);
				if (!client.getPets().containsKey(_type))
				{
					client.getPets().put(_type, _type.getName());
				}
			}
		}
	}

	public static List<PowerPlayClubItem> rewardsForMonths(Set<YearMonth> months)
	{
		return months.stream().sorted().map(PowerPlayClubRewards::getReward).collect(Collectors.toList());
	}

	public static Map<YearMonth, PowerPlayClubItem> rewards()
	{
		return rewards;
	}

	public static PowerPlayClubItem getReward(YearMonth month)
	{
		return rewards.getOrDefault(month, MISSING);
	}

	public static void giveAllItems(Player player, InventoryManager inventoryManager, PowerPlayClubRepository repo)
	{
		UtilPlayer.message(player, F.main("Power Play Club", "Verifying subscription.."));

		repo.attemptClaim(player).thenCompose(success ->
		{
			if (!success)
			{
				UtilPlayer.message(player, F.main("Power Play Club", "An unexpected error happened!"));
				return CompletableFuture.completedFuture(null);
			}

			PowerPlayData cached = repo.getCachedData(player);
			int unclaimed = cached.getUnclaimedMonths().size();

			// Give amplifiers and chests
			Item gameAmplifier = inventoryManager.getItem("Game Booster");
			if (gameAmplifier == null)
			{
				UtilPlayer.message(player, F.main("Power Play Club", "An unexpected error happened!"));
			}
			else
			{
				inventoryManager.addItemToInventory(player, gameAmplifier.Name, AMPLIFIERS_PER_MONTH * unclaimed);
				UtilPlayer.message(player, F.main("Power Play Club", "You received " + (AMPLIFIERS_PER_MONTH * unclaimed) + "x " + F.elem("Game Amplifier") + "."));
			}
			Item omegaChest = inventoryManager.getItem("Omega Chest");
			if (omegaChest == null)
			{
				UtilPlayer.message(player, F.main("Power Play Club", "An unexpected error happened!"));
			}
			else
			{
				inventoryManager.addItemToInventory(player, omegaChest.Name, CHESTS_PER_MONTH * unclaimed);
				UtilPlayer.message(player, F.main("Power Play Club", "You received " + (CHESTS_PER_MONTH * unclaimed) + "x " + F.elem("Omega Chest") + "."));
			}

			// Refresh Power Play data on the server
			return repo.loadData(player).thenCompose(BukkitFuture.accept(data -> repo.putCachedData(player, data)));
		});
	}

}
