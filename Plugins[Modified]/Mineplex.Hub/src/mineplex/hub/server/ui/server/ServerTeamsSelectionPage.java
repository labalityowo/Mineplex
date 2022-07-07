package mineplex.hub.server.ui.server;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.hub.server.ServerManager;
import mineplex.serverdata.data.ServerGroup;

public class ServerTeamsSelectionPage extends ServerModeSelectionPage
{

	public ServerTeamsSelectionPage(ServerManager plugin, ServerSelectionShop shop, CoreClientManager clientManager, DonationManager donationManager, ServerGroup serverGroup, Player player)
	{
		super(plugin, shop, clientManager, donationManager, serverGroup, player);
	}

	@Override
	protected void buildPage()
	{
		super.buildPage();

		String friendlyName = _serverGroup.getServerNpcName();

		addButton(11, new ItemBuilder(Material.SKULL_ITEM, (byte) 3)
				.setTitle(C.cYellowB + "Solo " + C.cGoldB + friendlyName)
				.addLore(
						"",
						"Free-for-all!",
						C.cRedB + "WARNING: " + C.Reset + "Teaming in Solo Mode is bannable!",
						"",
						CLICK_TO_PLAY
				)
				.setPlayerHead(getPlayer().getName())
				.build(), getButtonFor(_serverGroup.getPrefix(), "Solo " + friendlyName));
		addButton(15, new ItemBuilder(Material.SKULL_ITEM, 2, (byte) 3)
				.setTitle(C.cYellowB + "Team " + C.cGoldB + friendlyName)
				.addLore(
						"",
						"2 Player Teams!",
						"",
						CLICK_TO_PLAY
				)
				.setPlayerHead(getPlayer().getName())
				.build(), getButtonFor(_serverGroup.getTeamServerKey(), "Team " + friendlyName));
	}
}
