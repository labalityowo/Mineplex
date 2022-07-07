package mineplex.core.achievement.leveling.ui;

import mineplex.core.account.CoreClientManager;
import mineplex.core.achievement.leveling.LevelingManager;
import mineplex.core.achievement.leveling.ui.page.LevelRewardMainPage;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import org.bukkit.entity.Player;

public class LevelRewardShop extends ShopBase<LevelingManager>
{

	public LevelRewardShop(LevelingManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Level Reward");
	}

	@Override
	protected ShopPageBase<LevelingManager, ? extends ShopBase<LevelingManager>> buildPagesFor(Player player)
	{
		return new LevelRewardMainPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
}
