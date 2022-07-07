package mineplex.core.achievement.ui.page;

import java.util.ArrayList;

import mineplex.core.stats.PlayerStats;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.account.CoreClientManager;
import mineplex.core.achievement.AchievementCategory;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.achievement.ui.AchievementShop;
import mineplex.core.achievement.ui.button.CategoryButton;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.stats.StatsManager;

public class ArcadeMainPage extends AchievementMainPage
{
	public ArcadeMainPage(AchievementManager plugin, StatsManager statsManager, AchievementShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player, String targetName, PlayerStats targetStats)
	{
		super(plugin, statsManager, shop, clientManager, donationManager, name, player, 9 * 5, targetName, targetStats);
	}

	@Override
	protected void buildPage()
	{
		int slot = 9;

		for (AchievementCategory category : AchievementCategory.values())
		{
			if (category.getGameCategory() != AchievementCategory.GameCategory.ARCADE)
				continue;

			CategoryButton button = new CategoryButton(getShop(), getPlugin(), _statsManager, category, getDonationManager(), getClientManager(), _targetName, _targetStats);

			ArrayList<String> lore = new ArrayList<String>();
			lore.add(" ");
			category.addStats(getClientManager(), _statsManager, lore, 2, getPlayer(), _targetName, _targetStats);
			lore.add(" ");
			addAchievements(category, lore, 9);
			lore.add(" ");
			lore.add(ChatColor.RESET + "Click for more details!");

			ShopItem shopItem = new ShopItem(category.getIcon(), category.getIconData(), C.Bold + category.getFriendlyName(), lore.toArray(new String[0]), 1, false, false);
			addButton(slot, shopItem, button);

			slot += ((slot + 1) % 9 == 0) ? 1 : 2;
		}

		addBackButton();
	}

	private void addBackButton()
	{
		addButton(4, new ShopItem(Material.BED, C.cGray + " \u21FD Go Back", new String[]{}, 1, false), new IButton()
		{
			public void onClick(Player player, ClickType clickType)
			{
				getShop().openPageForPlayer(getPlayer(), new AchievementMainPage(getPlugin(), _statsManager, getShop(), getClientManager(), getDonationManager(), _targetName + "'s Stats", player, _targetName, _targetStats));
				player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
			}
		});
	}

}
