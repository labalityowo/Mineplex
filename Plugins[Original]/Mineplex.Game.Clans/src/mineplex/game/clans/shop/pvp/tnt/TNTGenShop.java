package mineplex.game.clans.shop.pvp.tnt;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClansManager;

public class TNTGenShop extends ShopBase<ClansManager>
{
	public TNTGenShop(ClansManager plugin, CoreClientManager clientManager, mineplex.core.donation.DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "TNT Generator");
	}

	@Override
	protected ShopPageBase<ClansManager, ? extends ShopBase<ClansManager>> buildPagesFor(Player player)
	{
		return new TNTGenPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
}
