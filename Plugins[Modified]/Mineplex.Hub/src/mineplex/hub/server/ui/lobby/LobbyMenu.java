package mineplex.hub.server.ui.lobby;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.hub.server.GameServer;
import mineplex.hub.server.ServerManager;
import mineplex.hub.server.ServerManager.Perm;
import mineplex.serverdata.data.MinecraftServer;

public class LobbyMenu extends ShopPageBase<ServerManager, LobbyShop>
{

	LobbyMenu(ServerManager plugin, LobbyShop lobbyShop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, lobbyShop, clientManager, donationManager, "Lobby Selector", player);
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		List<GameServer> servers = new ArrayList<>(getPlugin().getServers("Lobby"));
		boolean ownsUltra = getClientManager().Get(getPlayer()).hasPermission(Perm.JOIN_FULL);
		String serverName = UtilServer.getServerName();

		servers.forEach(server ->
		{
			if (server.getNumber() >= getSize())
			{
				return;
			}

			MinecraftServer serverStatus = server.getServer();

			ItemBuilder builder = new ItemBuilder(Material.IRON_BLOCK, Math.max(1, Math.min(server.getNumber(), 64)));
			String colour = C.cYellow, bottomLine = "Click to Join!";
			boolean sameServer = serverName.equals(serverStatus.getName());
			boolean full = serverStatus.getPlayerCount() >= serverStatus.getMaxPlayerCount();
			boolean canJoin = ownsUltra || !full;

			if (sameServer)
			{
				builder.setType(Material.EMERALD_BLOCK);
				colour = C.cGreenB;
				bottomLine = "You are here!";
			}
			else if (full)
			{
				if (ownsUltra)
				{
					builder.setType(Material.DIAMOND_BLOCK);
					colour = C.cAquaB;
				}
				else
				{
					builder.setType(Material.REDSTONE_BLOCK);
					colour = C.cRedB;
					bottomLine = "Ultra+ can join. Visit mineplex.com/shop!";
				}
			}

			builder.setTitle(colour + serverStatus.getName());
			builder.addLore(
					"",
					C.cYellow + "Players: " + C.Reset + serverStatus.getPlayerCount() + "/" + serverStatus.getMaxPlayerCount(),
					"",
					bottomLine
			);

			addButton(server.getNumber() - 1, builder.build(), (player, clickType) ->
			{
				if (sameServer)
				{
					playDenySound(player);
				}
				else if (canJoin)
				{
					getPlugin().selectServer(player, server);
				}
			});
		});
	}
}