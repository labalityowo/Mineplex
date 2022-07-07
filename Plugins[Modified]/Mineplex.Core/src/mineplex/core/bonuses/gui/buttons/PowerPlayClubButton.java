package mineplex.core.bonuses.gui.buttons;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.bonuses.BonusManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gui.GuiItem;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.powerplayclub.PowerPlayClubRepository;
import mineplex.core.powerplayclub.PowerPlayClubRewards;
import mineplex.core.powerplayclub.PowerPlayData;
import mineplex.core.shop.item.ShopItem;

public class PowerPlayClubButton implements GuiItem
{

	private ItemStack _item;
	private Player _player;
	private PowerPlayClubRepository _powerPlayClubRepository;
	private InventoryManager _inventoryManager;

	public PowerPlayClubButton(Player player, BonusManager manager)
	{
		_player = player;
		_powerPlayClubRepository = manager.getPowerPlayClubRepository();
		_inventoryManager = manager.getInventoryManager();
	}

	@Override
	public void setup()
	{
		setItem();
	}

	@Override
	public void close()
	{

	}

	@Override
	public void click(ClickType clickType)
	{
		if (isAvailable())
		{
			_player.closeInventory();
			_player.playSound(_player.getLocation(), Sound.NOTE_PLING, 1, 1.6f);
			PowerPlayClubRewards.giveAllItems(_player, _inventoryManager, _powerPlayClubRepository);
		}
		else
		{
			_player.playSound(_player.getLocation(), Sound.ITEM_BREAK, 1, 10);
			if (_powerPlayClubRepository.getCachedData(_player).isSubscribed())
			{
				UtilPlayer.message(_player, F.main("Power Play Club", "Already claimed! Come back next month!"));
			}
			else
			{
				UtilPlayer.message(_player, F.main("Power Play Club", "You have no months left! Buy more months at " + F.greenElem("www.mineplex.com/shop") + "!"));
			}
		}
	}

	@Override
	public ItemStack getObject()
	{
		return _item;
	}

	private void setItem()
	{
		final Material material;
		final String itemName;
		final List<String> lore = new ArrayList<>();


		PowerPlayData cached = _powerPlayClubRepository.getCachedData(_player);
		Optional<LocalDate> maybeNextClaimDate = cached.getNextClaimDate();
		Set<YearMonth> unclaimed = cached.getUnclaimedMonths();

		if (!cached.getUsableCosmeticMonths().isEmpty())
		{
			lore.addAll(buildCosmeticsLore(cached.getUsableCosmeticMonths()));
		}

		if (!unclaimed.isEmpty())
		{
			// Player has unclaimed rewards, even if s/he's not currently subscribed
			material = Material.GOLD_INGOT;
			itemName = C.cGreenB + "Power Play Club";

			lore.addAll(buildOtherRewardsLore(unclaimed.size()));
			lore.add(C.cGold + "Click to claim!");

		} else if (maybeNextClaimDate.isPresent()) // Player is still subscribed, and has claimed everything so far
		{
			LocalDate nextClaimDate = maybeNextClaimDate.get();

			material = Material.REDSTONE_BLOCK;
			itemName = C.cRedB + "Power Play Club";

			lore.add(C.cYellow + "Come back " + C.cGreen + nextClaimDate.getMonth().getDisplayName(TextStyle.FULL, Locale.US) + " " + nextClaimDate.getDayOfMonth());
			lore.add(C.cWhite + "  " + PowerPlayClubRewards.AMPLIFIERS_PER_MONTH + " Game Amplifier");
			lore.add(C.cWhite + "  " + PowerPlayClubRewards.CHESTS_PER_MONTH + " Omega Chest");

		} else
		{
			// Player isn't subscribed; show them the rewards for this current month and tell them to subscribe
			material = Material.REDSTONE_BLOCK;
			itemName = C.cRedB + "Power Play Club";

			lore.add(C.cYellow + YearMonth.now().getMonth().getDisplayName(TextStyle.FULL, Locale.US) + "'s Cosmetic");
			lore.add(C.cWhite + "  " + PowerPlayClubRewards.getReward(YearMonth.now()).getPrizeName());
			lore.add(" ");
			lore.addAll(buildOtherRewardsLore(1));
			lore.add(C.cRed + "Get Power Play Club months at");
			lore.add(C.cAqua + "mineplex.com/shop");
		}

		_item = new ShopItem(material, (byte)0, itemName, lore.toArray(new String[lore.size()]), 1, false, false);
	}

	private List<String> buildCosmeticsLore(Set<YearMonth> cosmeticsMonths)
	{
		List<String> lore = new ArrayList<>();
		lore.add(C.cYellow + "Unlocked cosmetics");
		PowerPlayClubRewards.rewards().entrySet().stream()
				.filter(entry -> cosmeticsMonths.contains(entry.getKey()))
				.sorted(Comparator.comparing(Map.Entry::getKey))
				.forEach(entry ->
				{
					YearMonth yearMonth = entry.getKey();
					lore.add(C.cWhite + "  " + entry.getValue().getPrizeName() + " " + C.cGold + yearMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.US) + " " + yearMonth.getYear());
				});
		lore.add(" ");
		return lore;
	}

	private List<String> buildOtherRewardsLore(int unclaimed)
	{
		List<String> lore = new ArrayList<>();
		lore.add(C.cYellow + "Other Rewards");
		lore.add("  " + C.cWhite + (PowerPlayClubRewards.AMPLIFIERS_PER_MONTH * unclaimed) + " Game Amplifier");
		lore.add("  " + C.cWhite + (PowerPlayClubRewards.CHESTS_PER_MONTH * unclaimed) + " Omega Chest");
		lore.add(" ");
		return lore;
	}

	private boolean isAvailable()
	{
		return !_powerPlayClubRepository.getCachedData(_player).getUnclaimedMonths().isEmpty();
	}

	public static boolean isAvailable(Player player, PowerPlayClubRepository repo)
	{
		PowerPlayData data = repo.getCachedData(player);
		return data != null && !data.getUnclaimedMonths().isEmpty();
	}

}
