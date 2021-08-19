package mineplex.core.achievement.leveling.ui.page;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.leveling.LevelingManager;
import mineplex.core.achievement.leveling.rewards.LevelReward;
import mineplex.core.achievement.leveling.ui.LevelRewardShop;
import mineplex.core.achievement.leveling.ui.button.LevelRewardButton;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.page.ShopPageBase;

public class LevelRewardMainPage extends ShopPageBase<LevelingManager, LevelRewardShop>
{

	private static final int VIEW_ALL_INDEX = 22;
	private static final ItemStack VIEW_ALL = new ItemBuilder(Material.BOOK)
			.setTitle(C.cGreen + "View All Rewards")
			.addLore("Click to view all rewards.")
			.build();
	private static final ItemStack MAX_LEVEL = new ItemBuilder(Material.FIREWORK)
			.setTitle(C.cPurpleB + "Congratulations!")
			.addLore(
					"You have achieved the maximum level",
					"possible on Mineplex! From all of us",
					"here at Mineplex, we thank you for",
					"dedicating so much time to the server.",
					"",
					"Unless you're an " + PermissionGroup.ADMIN.getDisplay(true, true, true, false) + C.mBody + " in which case",
					"you cheated " + C.cPurple + "<3" + C.cGray + "."
			)
			.build();

	public LevelRewardMainPage(LevelingManager plugin, LevelRewardShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Level Rewards", player, 3 * 9);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		int current = _plugin.getFirstUnclaimedLevel(_player);

		// Claimed all the rewards
		if (current == -1)
		{
			addButtonNoAction(13, MAX_LEVEL);

			for (int i = 0; i < getSize(); i++)
			{
				if (i == 13 || i == VIEW_ALL_INDEX)
				{
					continue;
				}

				addButtonNoAction(i, new ItemBuilder(Material.STAINED_GLASS_PANE, (byte) UtilMath.r(15))
						.setTitle(C.cBlack)
						.build());
			}
			return;
		}

		int previousA = getPreviousLevel(current);
		int previousB = getPreviousLevel(previousA);
		int nextA = getNextLevel(current);
		int nextB = getNextLevel(nextA);

		List<LevelReward> previousARewards = _plugin.getLevelRewards(previousA);
		List<LevelReward> previousBRewards = _plugin.getLevelRewards(previousB);
		List<LevelReward> currentRewards = _plugin.getLevelRewards(current);
		List<LevelReward> nextARewards = _plugin.getLevelRewards(nextA);
		List<LevelReward> nextBRewards = _plugin.getLevelRewards(nextB);

		// Order of display
		// previousB -> previousA -> current -> nextA -> nextB

		addButton(11, previousB, previousBRewards);
		addButton(12, previousA, previousARewards);
		addButton(13, current, currentRewards);
		addButton(14, nextA, nextARewards);
		addButton(15, nextB, nextBRewards);

		addButton(VIEW_ALL_INDEX, VIEW_ALL, (player, clickType) -> _shop.openPageForPlayer(player, new LevelRewardViewAllPage(getPlugin(), getShop(), this, getClientManager(), getDonationManager(), player)));
	}

	private void addButton(int slot, int level, List<LevelReward> rewards)
	{
		if (level < 0 || rewards == null)
		{
			return;
		}

		addButton(slot, _plugin.getLevelItem(_player, rewards, level), new LevelRewardButton(this, level));
	}

	private int getPreviousLevel(int level)
	{
		int previous = 0;

		for (Entry<Integer, List<LevelReward>> entry : _plugin.getLevelRewards().entrySet())
		{
			int currentLevel = entry.getKey();

			if (currentLevel == level)
			{
				return previous;
			}

			previous = currentLevel;
		}

		return -1;
	}

	private int getNextLevel(int level)
	{
		boolean found = false;

		for (Entry<Integer, List<LevelReward>> entry : _plugin.getLevelRewards().entrySet())
		{
			int currentLevel = entry.getKey();

			if (currentLevel == level)
			{
				found = true;
			}
			else if (found)
			{
				return currentLevel;
			}
		}

		return -1;
	}
}
