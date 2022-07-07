package mineplex.hub.server.ui.server;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.hub.server.ServerManager;
import mineplex.serverdata.data.ServerGroup;

public class ChampionsServerSelectionPage extends ServerModeSelectionPage
{

	public ChampionsServerSelectionPage(ServerManager plugin, ServerSelectionShop shop, CoreClientManager clientManager, DonationManager donationManager, ServerGroup serverGroup, Player player)
	{
		super(plugin, shop, clientManager, donationManager, serverGroup, player);
	}

	@Override
	protected void buildPage()
	{
		super.buildPage();

		addButton(11, new ItemBuilder(Material.BEACON)
				.setTitle(C.cYellowB + "Champions " + C.cGoldB + "Dominate")
				.addLore(
						"",
						"Select your champion and battle in a 5v5",
						"custom non-vanilla PvP game!",
						"Capture points & pick up gems to win.",
						"",
						CLICK_TO_PLAY
				)
				.build(), getButtonFor("DOM", "Dominate"));

		addButton(15, new ItemBuilder(Material.BANNER)
				.setTitle(C.cYellowB + "Champions " + C.cGoldB + "CTF")
				.addLore(
						"",
						"Select your champion and battle in an 8v8",
						"custom non-vanilla PvP game!",
						"Steal the enemy’s flag and bring it to your base",
						"5 times to win.",
						"",
						CLICK_TO_PLAY
				)
				.build(), getButtonFor("CTF", "Capture The Flag"));
	}
}
