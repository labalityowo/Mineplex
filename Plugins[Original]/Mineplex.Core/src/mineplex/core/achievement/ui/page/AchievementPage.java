package mineplex.core.achievement.ui.page;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.account.CoreClientManager;
import mineplex.core.achievement.Achievement;
import mineplex.core.achievement.AchievementCategory;
import mineplex.core.achievement.AchievementData;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.achievement.ui.AchievementShop;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.donation.DonationManager;
import mineplex.core.elo.EloManager;
import mineplex.core.elo.EloManager.EloDivision;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.stats.PlayerStats;
import mineplex.core.stats.StatsManager;

public class AchievementPage extends ShopPageBase<AchievementManager, AchievementShop>
{
	private static final int ACHIEVEMENT_MIDDLE_INDEX = 31;

	private AchievementCategory _category;
	private StatsManager _statsManager;
	private EloManager _eloManager;

	private String _targetName;
	private PlayerStats _targetStats;

	public AchievementPage(AchievementManager plugin, StatsManager statsManager, AchievementCategory category, AchievementShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player, String targetName, PlayerStats targetStats)
	{
		super(plugin, shop, clientManager, donationManager, category.getFriendlyName(), player);

		_statsManager = statsManager;
		_category = category;
		_eloManager = plugin.getEloManager();

		_targetName = targetName;
		_targetStats = targetStats;

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		List<Achievement> achievements = getAchievements();
		int currentIndex = Math.max(ACHIEVEMENT_MIDDLE_INDEX - (achievements.size() / 2), 27);
		boolean hasAllAchievements = true;
		int achievementCount = 0;
		boolean overflow = false;

		ArrayList<String> masterAchievementLore = new ArrayList<>();
		masterAchievementLore.add(" ");

		for (Achievement achievement : achievements)
		{
			AchievementData data = getPlugin().get(_targetStats, achievement);
			boolean singleLevel = achievement.isSingleLevel();
			boolean levelNames = achievement.hasLevelNames();
			boolean hasUnlocked = data.getLevel() >= achievement.getMaxLevel();

			if (!hasUnlocked)
			{
				hasAllAchievements = false;
			}

			{
				Material material = hasUnlocked ? Material.EXP_BOTTLE : Material.GLASS_BOTTLE;
				String itemName = (hasUnlocked ? C.cGreen : C.cRed) + achievement.getName();

				if (!singleLevel)
				{
					if (!levelNames)
					{
						itemName += ChatColor.WHITE + " Level " + data.getLevel() + "/" + achievement.getMaxLevel();
					}
					else
					{
						itemName += ChatColor.GOLD + " " + (data.getLevel() == 0 ? achievement.getDefaultLevelName() : achievement.getLevelNames()[Math.min(data.getLevel(), achievement.getLevelNames().length) - 1]);
					}
				}

				ArrayList<String> lore = new ArrayList<>();
				lore.add(" ");
				for (String descLine : achievement.getDesc())
				{
					lore.add(ChatColor.RESET + descLine);
				}

				if (!hasUnlocked && achievement.isOngoing())
				{
					lore.add(" ");
					lore.add(C.cYellow + (singleLevel ? "Progress: " : "Next Level: ") + C.cWhite + data.getExpRemainder() + "/" + data.getExpNextLevel());
				}
				
				if (!hasUnlocked && singleLevel && achievement.getGemReward() > 0)
				{
					lore.add(" ");
					lore.add(C.cYellow + "Reward: " + C.cGreen + achievement.getGemReward() + " Gems");
				}

				if (!hasUnlocked && achievement.getLevelUpRewards().length > 0)
				{
					int[][] rewards = achievement.getLevelUpRewards();

					if (data.getLevel() < rewards.length)
					{
						int[] thisRewards = rewards[data.getLevel()];
						int greaterThanZero = 0;

						for (int reward : thisRewards)
						{
							if (reward > 0)
							{
								greaterThanZero++;
							}
						}

						lore.add(" ");

						if (greaterThanZero == 1)
						{
							String rewardString = C.cYellow + "Reward: ";

							for (int i = 0; i < thisRewards.length; i++)
							{
								int reward = thisRewards[i];

								if (reward > 0)
								{
									rewardString += getFormattedReward(reward, i);
									break;
								}
							}

							lore.add(rewardString);
						}
						else
						{
							lore.add(C.cYellow + "Rewards:");

							for (int i = 0; i < thisRewards.length; i++)
							{
								int reward = thisRewards[i];

								if (reward > 0)
								{
									lore.add(C.cWhite + " - " + getFormattedReward(reward, i));
								}
							}
						}
					}
				}
				
				if (hasUnlocked && data.getLevel() == achievement.getMaxLevel())
				{
					lore.add(" ");
					lore.add(C.cAqua + "Complete!");
				}
					

				addItem(currentIndex, new ShopItem(material, (byte) (hasUnlocked ? 0 : 0), itemName, lore.toArray(new String[0]), 1, false, false));
			}

			masterAchievementLore.add((hasUnlocked ? C.cGreen : C.cRed) + achievement.getName());

			currentIndex++;

			if (++achievementCount == 9)
			{
				currentIndex = ACHIEVEMENT_MIDDLE_INDEX + 9 - (achievements.size() - 9) / 2;
				overflow = true;
			}
		}

		// Master Achievement
		if (!_category.getFriendlyName().startsWith("Global") && achievementCount > 0)
		{
			String itemName = C.Bold + _category.getFriendlyName() + " Master Achievement";
			masterAchievementLore.add(" ");
			if (getPlayer().getName().equalsIgnoreCase(_targetName))
			{
				if (_category.getReward() != null)
					masterAchievementLore.add(C.cYellow + "Reward: " + ChatColor.RESET + _category.getReward());
				else
					masterAchievementLore.add(C.cYellow + "Reward: " + ChatColor.RESET + "Coming Soon...");
			}
			
			addItem(overflow ? 49 : 40, new ShopItem(hasAllAchievements ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK, (byte) 0, itemName, masterAchievementLore.toArray(new String[0]), 1, false, true));
		}

		addBackButton();
		addStats();
		addDivisionDisplay();
	}

	private void addBackButton()
	{
		addButton(4, new ShopItem(Material.BED, C.cGray + " \u21FD Go Back", new String[]{}, 1, false), new IButton()
		{
			public void onClick(Player player, ClickType clickType)
			{
				AchievementMainPage page;
				if (_category.getGameCategory() == AchievementCategory.GameCategory.ARCADE)
					page = new ArcadeMainPage(getPlugin(), _statsManager, getShop(), getClientManager(), getDonationManager(), "Arcade Games", player, _targetName, _targetStats);
				else
					page = new AchievementMainPage(getPlugin(), _statsManager, getShop(), getClientManager(), getDonationManager(), _targetName + "'s Stats", player, _targetName, _targetStats);

				getShop().openPageForPlayer(getPlayer(), page);
				player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
			}
		});
	}

	private void addStats()
	{
		// Don't show if this category has no stats to display
		if (_category.getStatsToDisplay().length == 0)
			return;

		Material material = Material.BOOK;
		String itemName = C.Bold + _category.getFriendlyName() + " Stats";
		List<String> lore = new ArrayList<String>();
		lore.add(" ");
		_category.addStats(getClientManager(), _statsManager, lore, getPlayer(), _targetName, _targetStats);

		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + itemName);
		meta.setLore(lore);
		item.setItemMeta(meta);

		setItem(22, item);
	}
	
	private void addDivisionDisplay()
	{
		Player target = UtilPlayer.searchExact(_targetName);

		if (target != null)
		{
			if (_category.getFriendlyName().startsWith("Global"))
				return;
			if (!_category.DisplayDivision || _category.GameId.length < 1)
				return;
			if (_category.GameId.length > 1)
			{
				for (int i = 0; i < _category.GameId.length; i++)
				{
					int id = _category.GameId[i];
					int elo = _eloManager.getElo(target, id);
					ItemStack disp = EloDivision.getDivision(elo).getVisual(elo);
					setItem(44 + i + 1, disp);
				}
			} else
			{
				int id = _category.GameId[0];
				int elo = _eloManager.getElo(target, id);
				ItemStack disp = EloDivision.getDivision(elo).getVisual(elo);
				setItem(49, disp);
			}
		}
	}

	public List<Achievement> getAchievements()
	{
		List<Achievement> achievements = new ArrayList<Achievement>();

		for (Achievement achievement : Achievement.values())
		{
			if (achievement.getCategory() == _category)
				achievements.add(achievement);
		}

		return achievements;
	}

	private String getFormattedReward(int reward, int type)
	{
		switch (type)
		{
			case 0:
				return C.cGreen + reward + " Gems";
			case 1:
				return C.cGold + reward + " Crowns";
			case 2:
				return C.cAqua + reward + " Mineplex XP";
		}

		return null;
	}
}
