package mineplex.hub.server.ui.server;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.hub.server.ServerManager;
import mineplex.serverdata.data.ServerGroup;

public class ServerSelectionShop extends ShopBase<ServerManager>
{

	public ServerSelectionShop(ServerManager plugin, CoreClientManager clientManager, DonationManager donationManager, String name)
	{
		super(plugin, clientManager, donationManager, name);
	}

	@Override
	protected ShopPageBase<ServerManager, ? extends ShopBase<ServerManager>> buildPagesFor(Player player)
	{
		return null;
	}

	public void openServerPage(Player player, ServerGroup serverGroup)
	{
		ShopPageBase<ServerManager, ? extends ShopBase<ServerManager>> page;

		switch (serverGroup.getPrefix())
		{
			case "SKY":
			case "HG":
			case "SSM":
				page = new ServerTeamsSelectionPage(getPlugin(), this, getClientManager(), getDonationManager(), serverGroup, player);
				break;
			case "DOM":
				page = new ChampionsServerSelectionPage(getPlugin(), this, getClientManager(), getDonationManager(), serverGroup, player);
				break;
			case "CW4":
				page = new CakeWarsServerSelectionPage(getPlugin(), this, getClientManager(), getDonationManager(), serverGroup, player);
				break;
			case "MPS":
				page = new MPSServerSelectionPage(getPlugin(), this, getClientManager(), getDonationManager(), serverGroup, player);
				break;
			default:
				page = getDirectServerPage(player, serverGroup.getServerNpcName(), serverGroup);
				break;
		}

		openPageForPlayer(player, page);
	}

	public ServerSelectionPage getDirectServerPage(Player player, String name, ServerGroup group)
	{
		return new ServerSelectionPage(getPlugin(), this, getClientManager(), getDonationManager(), name, group, player);
	}

	public void updatePages()
	{
		getPageMap().values().forEach(ShopPageBase::refresh);
	}

	@Override
	protected void openShopForPlayer(Player player)
	{
		getPlugin().getHubManager().GetVisibility().addHiddenPlayer(player);
	}

	@Override
	protected void closeShopForPlayer(Player player)
	{
		getPlugin().getHubManager().GetVisibility().removeHiddenPlayer(player);
	}
}
