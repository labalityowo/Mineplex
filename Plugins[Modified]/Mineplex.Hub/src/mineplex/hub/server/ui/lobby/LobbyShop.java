package mineplex.hub.server.ui.lobby;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.hub.server.ServerManager;

public class LobbyShop extends ShopBase<ServerManager>
{

	public LobbyShop(ServerManager plugin, CoreClientManager clientManager, DonationManager donationManager, String name)
	{
		super(plugin, clientManager, donationManager, name);
	}

	@Override
	protected ShopPageBase<ServerManager, ? extends ShopBase<ServerManager>> buildPagesFor(Player player)
	{
		return new LobbyMenu(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}

	public void updatePages()
	{
		getPageMap().values().forEach(ShopPageBase::refresh);
	}
}
