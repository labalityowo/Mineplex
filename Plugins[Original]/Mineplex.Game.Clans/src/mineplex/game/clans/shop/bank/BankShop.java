package mineplex.game.clans.shop.bank;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClansManager;

public class BankShop extends ShopBase<ClansManager>
{
	public BankShop(ClansManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Bank Shop");
	}

	@Override
	protected ShopPageBase<ClansManager, ? extends ShopBase<ClansManager>> buildPagesFor(Player player)
	{
		return new BankPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
}
