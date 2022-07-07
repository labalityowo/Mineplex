package mineplex.core.achievement.ui.page;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.achievement.Achievement;
import mineplex.core.achievement.AchievementCategory;
import mineplex.core.achievement.AchievementData;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.achievement.ui.AchievementShop;
import mineplex.core.achievement.ui.button.ArcadeButton;
import mineplex.core.achievement.ui.button.CategoryButton;
import mineplex.core.achievement.ui.button.UHCButton;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemLayout;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.stats.PlayerStats;
import mineplex.core.stats.StatsManager;

public class AchievementMainPage extends ShopPageBase<AchievementManager, AchievementShop>
{
	protected StatsManager _statsManager;

	protected String _targetName;
	protected PlayerStats _targetStats;

	public AchievementMainPage(AchievementManager plugin, StatsManager statsManager, AchievementShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player, String targetName, PlayerStats targetStats)
	{
		this(plugin, statsManager, shop, clientManager, donationManager, name, player, 9 * 5, targetName, targetStats);
	}

	public AchievementMainPage(AchievementManager plugin, StatsManager statsManager, AchievementShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player, int size, String targetName, PlayerStats targetStats)
	{
		super(plugin, shop, clientManager, donationManager, name, player, size);

		_targetName = targetName;
		_targetStats = targetStats;
		_statsManager = statsManager;

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		ArrayList<Integer> pageLayout = new ItemLayout(
		"OXOXOXOXO",
		"OXOXOXOXO",
		"OXOXOXOXO",
		"OXOXOXOXO",
		"OXOXOXOXO").getItemSlots();
		int listSlot = 0;
 
		for (AchievementCategory category : AchievementCategory.values())
		{
			if (category.getGameCategory() == AchievementCategory.GameCategory.ARCADE || category.getGameCategory() == AchievementCategory.GameCategory.UHC)
				continue;

			CategoryButton button = new CategoryButton(getShop(), getPlugin(), _statsManager, category, getDonationManager(),
					getClientManager(), _targetName, _targetStats);

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(" ");
			category.addStats(getClientManager(), _statsManager, lore, category == AchievementCategory.GLOBAL ? 10 : 2,
					getPlayer(), _targetName, _targetStats);
			lore.add(" ");
			addAchievements(category, lore, 9);
			lore.add(ChatColor.RESET + "Click for more details!");

			ShopItem shopItem = new ShopItem(category.getIcon(), category.getIconData(), C.Bold + category.getFriendlyName(),
					lore.toArray(new String[0]), 1, false, false);
			addButton(pageLayout.get(listSlot++), shopItem, button);
		}

		addArcadeButton(pageLayout.get(listSlot++));
		addUHCButton(pageLayout.get(listSlot++));
	}

	protected void addArcadeButton(int slot)
	{
		ArcadeButton button = new ArcadeButton(getShop(), getPlugin(), _statsManager, getDonationManager(), getClientManager(), _targetName, _targetStats);
		ShopItem shopItem = new ShopItem(Material.BOW, (byte) 0, C.Bold + "Arcade Games", new String[] {" ", ChatColor.RESET + "Click for more!"}, 1, false, false);

		addButton(slot, shopItem, button);
	}
	
	protected void addUHCButton(int slot)
	{
		UHCButton button = new UHCButton(getShop(), getPlugin(), _statsManager, getDonationManager(), getClientManager(), _targetName, _targetStats);
		ShopItem shopItem = new ShopItem(Material.GOLDEN_APPLE, (byte) 0, C.Bold + "UHC", new String[] {" ", ChatColor.RESET + "Click for more!"}, 1, false, false);

		addButton(slot, shopItem, button);
	}

	protected void addAchievements(AchievementCategory category, List<String> lore, int max)
	{
		int achievementCount = 0;
		for (int i = 0; i < Achievement.values().length; i++)
		{
			Achievement achievement = Achievement.values()[i];
			if (achievement.getCategory() == category)
			{
				if (achievement.hasLevelNames())
				{
					AchievementData data = getPlugin().get(_targetStats, achievement);
					String display = (data.getLevel() == 0 ? achievement.getDefaultLevelName() : achievement.getLevelNames()[data.getLevel() - 1]);
					lore.add(C.cGold + achievement.getName() + ": " + display);
					achievementCount++;
					continue;
				}
				// Don't display achievements that have multiple levels
				if (achievement.getMaxLevel() > 1)
					continue;

				AchievementData data = getPlugin().get(_targetStats, achievement);
				boolean finished = data.getLevel() >= achievement.getMaxLevel();

				lore.add((finished ? C.cGreen : C.cRed) + achievement.getName());

				achievementCount++;
			}
		}

		if (achievementCount > 0)
		{
			lore.add(" ");
		}
	}
}