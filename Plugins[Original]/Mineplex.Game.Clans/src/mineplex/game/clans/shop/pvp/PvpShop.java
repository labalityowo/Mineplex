package mineplex.game.clans.shop.pvp;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClansManager;

public class PvpShop extends ShopBase<ClansManager>
{
	public PvpShop(ClansManager plugin, CoreClientManager clientManager, mineplex.core.donation.DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Pvp Gear");
	}

	@Override
	protected ShopPageBase<ClansManager, ? extends ShopBase<ClansManager>> buildPagesFor(Player player)
	{
		return new PvpPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
}