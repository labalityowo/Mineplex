package mineplex.hub.hubgame.ui;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.hub.hubgame.HubGameManager;

public class HubGameShop extends ShopBase<HubGameManager>
{

	public HubGameShop(HubGameManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Hub Game");
	}

	@Override
	protected ShopPageBase<HubGameManager, ? extends ShopBase<HubGameManager>> buildPagesFor(Player player)
	{
		return null;
	}
}
