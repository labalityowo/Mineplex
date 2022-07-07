package mineplex.hub.server.ui.server;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.hub.server.ServerManager;
import mineplex.serverdata.data.ServerGroup;

public class ServerModeSelectionPage extends ShopPageBase<ServerManager, ServerSelectionShop>
{

	protected static final String CLICK_TO_PLAY = C.cDGreen + "►► " + C.cGreen + "Click to Play!";

	protected final ServerGroup _serverGroup;

	ServerModeSelectionPage(ServerManager plugin, ServerSelectionShop shop, CoreClientManager clientManager, DonationManager donationManager, ServerGroup serverGroup, Player player)
	{
		super(plugin, shop, clientManager, donationManager, serverGroup.getServerNpcName(), player, 27);

		_serverGroup = serverGroup;
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		addButton(4, new ItemBuilder(Material.BED)
				.setTitle(C.cGreen + "Go Back")
				.addLore("Click to go back to the", "Quick Compass!")
				.build(), (player, clickType) -> getPlugin().getQuickShop().attemptShopOpen(player));
	}

	protected IButton getButtonFor(String serverGroupPrefix, String name)
	{
		return (player, clickType) -> getShop().openPageForPlayer(player, getShop().getDirectServerPage(player, name, getPlugin().getServerGroupByPrefix(serverGroupPrefix)));
	}
}
