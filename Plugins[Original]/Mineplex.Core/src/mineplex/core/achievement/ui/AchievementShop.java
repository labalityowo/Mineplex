package mineplex.core.achievement.ui;

import mineplex.core.stats.PlayerStats;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.achievement.ui.page.AchievementMainPage;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.stats.StatsManager;

/**
 * Created by Shaun on 8/21/2014.
 */
public class AchievementShop extends ShopBase<AchievementManager>
{
	private StatsManager _statsManager;

	public AchievementShop(AchievementManager plugin, StatsManager statsManager, CoreClientManager clientManager, DonationManager donationManager, String name)
	{
		super(plugin, clientManager, donationManager, name);
		_statsManager = statsManager;
	}

	@Override
	protected ShopPageBase<AchievementManager, ? extends ShopBase<AchievementManager>> buildPagesFor(Player player)
	{
		return BuildPagesFor(player, player.getName(), getPlugin().getStatsManager().Get(player));
	}

	protected ShopPageBase<AchievementManager, ? extends ShopBase<AchievementManager>> BuildPagesFor(Player player, String targetName, PlayerStats targetStats)
	{
		return new AchievementMainPage(getPlugin(), _statsManager, this, getClientManager(), getDonationManager(), targetName + "'s Stats", player, targetName, targetStats);
	}

	public boolean attemptShopOpen(Player player, String targetName, PlayerStats targetStats)
	{
		if (!getOpenedShop().contains(player.getUniqueId()))
		{
			if (!canOpenShop(player))
				return false;

			getOpenedShop().add(player.getUniqueId());

			openShopForPlayer(player);
			if (!getPlayerPageMap().containsKey(player.getUniqueId()))
			{
				getPlayerPageMap().put(player.getUniqueId(), BuildPagesFor(player, targetName, targetStats));
			}

			openPageForPlayer(player, getOpeningPageForPlayer(player));

			return true;
		}

		return false;
	}
}
