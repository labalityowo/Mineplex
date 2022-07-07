package mineplex.hub.server.ui.server;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.game.GameDisplay;
import mineplex.core.game.status.GameInfo;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.hub.server.GameServer;
import mineplex.hub.server.ServerManager;
import mineplex.hub.server.ServerManager.Perm;
import mineplex.serverdata.data.MinecraftServer;
import mineplex.serverdata.data.ServerGroup;

public class MPSServerSelectionPage extends ShopPageBase<ServerManager, ServerSelectionShop>
{

	private final ServerGroup _serverGroup;
	private final boolean _ownsUltra;

	public MPSServerSelectionPage(ServerManager plugin, ServerSelectionShop shop, CoreClientManager clientManager, DonationManager donationManager, ServerGroup serverGroup, Player player)
	{
		super(plugin, shop, clientManager, donationManager, serverGroup.getServerNpcName(), player);

		_serverGroup = serverGroup;
		_ownsUltra = clientManager.Get(player).hasPermission(Perm.JOIN_FULL);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		List<GameServer> servers = new ArrayList<>(getPlugin().getServers(_serverGroup.getPrefix()));

		servers.sort((o1, o2) -> o2.getServer().getPlayerCount() - o1.getServer().getPlayerCount());

		int slot = 18, featuredSlot = 0;

		for (GameServer server : servers)
		{
			MinecraftServer serverStatus = server.getServer();
			GameInfo info = server.getInfo();
			PermissionGroup hostRank = info.getHostRank();

			if (hostRank == null)
			{
				continue;
			}

			int thisSlot = hostRank.hasPermission(Perm.FEATURE_SERVER) ? featuredSlot++ : slot++;

			if (thisSlot >= getSize() - 1)
			{
				continue;
			}

			GameDisplay game = info.getGame();
			ItemBuilder builder;

			if (game == null)
			{
				builder = new ItemBuilder(Material.REDSTONE_BLOCK);
			}
			else
			{
				builder = new ItemBuilder(game.getMaterial(), game.getMaterialData())
						.addLore(
								"",
								C.cYellow + "Game: " + C.Reset + game.getName()
						);
			}

			if (info.getMap() != null)
			{
				builder.addLore(C.cYellow + "Map: " + C.Reset + info.getMap());
			}

			builder.setTitle(C.cGreenB + serverStatus.getName())
					.addLore(
							C.cYellow + "Host Rank: " + hostRank.getDisplay(true, true, true, true),
							C.cYellow + "Players: " + C.Reset + serverStatus.getPlayerCount() + "/" + serverStatus.getMaxPlayerCount(),
							""
					);

			String footer;
			boolean canJoin;

			switch (info.getJoinable())
			{
				case OPEN:
					footer = "Click to Join!";
					canJoin = true;
					break;
				case RANKS_ONLY:
					if (_ownsUltra)
					{
						footer = "You have ULTRA! Click to Join!";
						canJoin = true;
					}
					else
					{
						footer = "ULTRA+ can join. Visit mineplex.com/shop";
						canJoin = false;
					}
					break;
				default:
					footer = "Game is full!";
					canJoin = false;
					break;
			}

			builder.addLore(C.cWhite + C.Line + footer);

			addButton(thisSlot, builder.build(), (player, clickType) ->
			{
				if (canJoin)
				{
					getPlugin().selectServer(player, server);
				}
				else
				{
					playDenySound(player);
				}
			});
		}
	}
}
