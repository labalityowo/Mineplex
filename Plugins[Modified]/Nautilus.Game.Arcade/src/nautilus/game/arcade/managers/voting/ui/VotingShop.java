package nautilus.game.arcade.managers.voting.ui;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

import nautilus.game.arcade.ArcadeManager;

public class VotingShop extends ShopBase<ArcadeManager>
{

	public VotingShop(ArcadeManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Voting");
	}

	@Override
	protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player)
	{
		return null;
	}
}
