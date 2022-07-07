package mineplex.hub.server.ui.server;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.hub.server.ServerManager;
import mineplex.serverdata.data.ServerGroup;

public class CakeWarsServerSelectionPage extends ServerModeSelectionPage
{

	public CakeWarsServerSelectionPage(ServerManager plugin, ServerSelectionShop shop, CoreClientManager clientManager, DonationManager donationManager, ServerGroup serverGroup, Player player)
	{
		super(plugin, shop, clientManager, donationManager, serverGroup, player);
	}

	@Override
	protected void buildPage()
	{
		super.buildPage();

		addButton(11, new ItemBuilder(Material.CAKE)
				.setTitle(C.cYellowB + "Cake Wars " + C.cGoldB + "Standard")
				.addLore(
						"",
						"4v4v4v4!",
						"",
						CLICK_TO_PLAY
				)
				.build(), getButtonFor("CW4", "Cake Wars Standard"));

		addButton(15, new ItemBuilder(Material.SUGAR)
				.setTitle(C.cYellowB + "Cake Wars " + C.cGoldB + "Duos")
				.addLore(
						"",
						"8 Teams of 2!",
						"",
						CLICK_TO_PLAY
				)
				.build(), getButtonFor("CW2", "Cake Wars Duos"));
	}
}
