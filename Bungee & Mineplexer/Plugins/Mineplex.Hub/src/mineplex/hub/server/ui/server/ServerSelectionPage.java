package mineplex.hub.server.ui.server;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTime;
import mineplex.core.donation.DonationManager;
import mineplex.core.game.status.GameInfo;
import mineplex.core.game.status.GameInfo.GameDisplayStatus;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.hub.server.GameServer;
import mineplex.hub.server.ServerManager;
import mineplex.hub.server.ServerManager.Perm;
import mineplex.serverdata.data.MinecraftServer;
import mineplex.serverdata.data.ServerGroup;

public class ServerSelectionPage extends ShopPageBase<ServerManager, ServerSelectionShop>
{

	private static final int STARTING_JOIN_SLOT = 19;
	private static final int MAX_SHOWN_SERVERS = 7;
	private static final int STARTING_PROGRESS_SLOT = 9;
	private static final long MAX_VIEWING_TIME = TimeUnit.SECONDS.toMillis(45);
	private static final Comparator<GameServer> GAME_SERVER_SORTER = (o1, o2) ->
	{
		// Dev Server
		if (o1.isDevServer())
		{
			return -1;
		}
		else if (o2.isDevServer())
		{
			return 1;
		}

		GameInfo info1 = o1.getInfo(), info2 = o2.getInfo();

		// Join-ability
		if (info1.getJoinable() != info2.getJoinable())
		{
			return info1.getJoinable().compareTo(info2.getJoinable());
		}

		// Status
		if (info1.getStatus() != info2.getStatus())
		{
			return info1.getStatus().compareTo(info2.getStatus());
		}

		MinecraftServer server1 = o1.getServer(), server2 = o2.getServer();

		// Players
		if (server1.getPlayerCount() != server2.getPlayerCount())
		{
			return Integer.compare(server2.getPlayerCount(), server1.getPlayerCount());
		}

		// Server Number
		return Integer.compare(o1.getNumber(), o2.getNumber());
	};

	private final ServerGroup _serverGroup;
	private final long _openedAt;
	private final boolean _ownsUltra;

	private boolean _showInProgress;

	ServerSelectionPage(ServerManager plugin, ServerSelectionShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, ServerGroup serverGroup, Player player)
	{
		super(plugin, shop, clientManager, donationManager, name, player);

		_serverGroup = serverGroup;
		_openedAt = System.currentTimeMillis();
		_ownsUltra = clientManager.Get(player).hasPermission(Perm.JOIN_FULL) || donationManager.Get(player).ownsUnknownSalesPackage(serverGroup.getServerType() + " ULTRA");

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		List<GameServer> servers = new ArrayList<>(getPlugin().getServers(_serverGroup.getPrefix()));
		servers.sort(GAME_SERVER_SORTER);

		if (_showInProgress)
		{
			buildInProgressPage(servers);
		}
		else
		{
			buildJoinablePage(servers);
		}

		if (UtilTime.elapsed(_openedAt, MAX_VIEWING_TIME))
		{
			getPlugin().runSyncLater(() -> getPlayer().closeInventory(), 0);
		}
	}

	private void buildJoinablePage(List<GameServer> servers)
	{
		int inProgress = servers.size();

		// Filter servers
		servers.removeIf(server ->
		{
			GameDisplayStatus status = server.getInfo().getStatus();
			return status == null || status == GameDisplayStatus.IN_PROGRESS || status == GameDisplayStatus.CLOSING;
		});

		// Subtracting the new size of the servers list gets us the amount that was removed.
		inProgress -= servers.size();

		// Instant join
		addRow(0, (byte) 3, new ItemBuilder(Material.DIAMOND_BLOCK)
						.setTitle(C.cGreenB + "Click To Join Instantly!")
						.setLore(UtilText.splitLineToArray(C.cGray + "Join the best server in an instant. No fiddling to find what server to join, let us pick one for you and join a game as fast as you can.", LineFormat.LORE)),
				(player, clickType) ->
				{
					if (!getPlugin().selectBest(player, _serverGroup))
					{
						playDenySound(player);
					}
				});

		// Public Servers
		int slot = STARTING_JOIN_SLOT, devSlot = STARTING_JOIN_SLOT + 9;

		for (GameServer server : servers)
		{
			if (server.isDevServer())
			{
				addServer(devSlot++, server);
				continue;
			}

			addServer(slot++, server);

			if (slot >= STARTING_JOIN_SLOT + MAX_SHOWN_SERVERS)
			{
				break;
			}
		}

		// In Progress Servers
		addRow(5, (byte) 4, new ItemBuilder(Material.GOLD_BLOCK)
						.setTitle(C.cYellowB + inProgress + " Games In Progress")
						.setLore(UtilText.splitLinesToArray(new String[]
								{
										"",
										C.cWhite + C.Line + "Click to Spectate",
										"",
										C.cGray + "List all servers in the middle of playing. Find a random one to learn how to play better, or find one that your friend might be in."
								}, LineFormat.LORE)), new SwapViewButton());
	}

	private void buildInProgressPage(List<GameServer> servers)
	{
		// Filter servers
		servers.removeIf(server ->
		{
			GameDisplayStatus status = server.getInfo().getStatus();
			return status != GameDisplayStatus.IN_PROGRESS && status != GameDisplayStatus.CLOSING;
		});

		// Go back
		addRow(0, (byte) 7, new ItemBuilder(Material.BED)
						.setTitle(C.cGreenB + "Go Back")
						.setLore(UtilText.splitLinesToArray(new String[]
								{
										"",
										C.cWhite + C.Line + "Click to Go Back",
								}, LineFormat.LORE)), new SwapViewButton());

		int slot = STARTING_PROGRESS_SLOT;

		for (GameServer server : servers)
		{
			addServer(slot++, server);

			if (slot >= getSize() - 1)
			{
				break;
			}
		}
	}

	private void addRow(int row, byte glassData, ItemBuilder builder, IButton button)
	{
		ItemStack glass = builder.clone()
				.setType(Material.STAINED_GLASS_PANE)
				.setData(glassData)
				.build();

		for (int column = 0; column < 9; column++)
		{
			addButton(getSlot(row, column), column == 4 ? builder.build() : glass, button);
		}
	}

	private void addServer(int slot, GameServer server)
	{
		GameInfo info = server.getInfo();
		// Subtract 2 from the timer because of the delay of the MOTD updating and retrieval
		int timerAdjust = info.getTimer() - 2;
		Material material;
		byte data = 0;
		String votingColour = null, votingStatus = null, votingOn = null;
		String motd = null;

		switch (info.getStatus())
		{
			case ALWAYS_OPEN:
				material = Material.EMERALD_BLOCK;
				break;
			case WAITING:
				material = Material.STAINED_GLASS;
				data = (byte) 8;
				votingColour = C.cGray;
				votingStatus = "Not Enough Players To Start";
				motd = "Recruiting";
				break;
			case VOTING:
				material = Material.STAINED_GLASS;
				data = (byte) 5;
				votingColour = C.cYellow;
				votingStatus = "Voting Is In Progress";
				motd = "Voting Ends in " + timeString(timerAdjust);
				break;
			case STARTING:
				material = Material.EMERALD_BLOCK;
				votingColour = C.cGreen;
				votingStatus = "Voting Already Finished!";
				motd = "Starting in " + timeString(timerAdjust);
				break;
			case IN_PROGRESS:
				material = Material.GOLD_BLOCK;
				motd = "In Progress";
				break;
			default:
				material = Material.IRON_BLOCK;
				motd = "Not Open";
				break;
		}

		if (_serverGroup.getGameVoting())
		{
			votingOn = "Game";
		}
		else if (_serverGroup.getMapVoting())
		{
			votingOn = "Map";
		}

		ItemBuilder builder = new ItemBuilder(material, Math.max(1, Math.min(64, server.getServer().getPlayerCount())), data)
				.setTitle(C.cGreenB + getName() + " Server " + server.getNumber())
				.addLore("");

		// If there's a game and the ServerGroup could run more than 1 game
		if (info.getGame() != null && _serverGroup.getGames().contains(","))
		{
			builder.addLore(C.cYellow + "Game: " + C.cWhite + info.getGame().getName());
		}

		if (info.getMode() != null)
		{
			builder.addLore(C.cYellow + "Mode: " + C.cWhite + info.getMode());
		}

		builder.addLore(
				C.cYellow + "Players: " + C.cWhite + server.getServer().getPlayerCount() + "/" + server.getServer().getMaxPlayerCount(),
				""
		);

		// Voting enabled
		if (votingOn != null)
		{
			boolean newLine = false;

			// Has information
			if (votingStatus != null)
			{
				if (info.getStatus() != GameDisplayStatus.WAITING)
				{
					builder.addLore(votingColour + votingOn + " " + votingStatus);
				}
				else
				{
					builder.addLore(votingColour + votingStatus);
				}

				newLine = true;
			}

			// Is voting
			if (info.getStatus() == GameDisplayStatus.VOTING)
			{
				for (String value : info.getVotingOn())
				{
					builder.addLore(C.cYellow + votingOn + ": " + C.cWhite + value);
				}

				newLine = true;
			}

			if (newLine)
			{
				builder.addLore("");
			}
		}

		// Map
		if (info.getMap() != null)
		{
			builder.addLore(
					C.cYellow + "Map: " + C.cWhite + info.getMap(),
					""
			);
		}

		if (motd != null)
		{
			builder.addLore(
					C.cGreen + motd,
					""
			);
		}

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
				builder.setType(Material.REDSTONE_BLOCK);
				footer = "Game is full!";
				// Always let staff join
				canJoin = _clientManager.Get(getPlayer()).hasPermission(Perm.JOIN_ALWAYS);
				break;
		}

		builder.addLore(C.cWhite + C.Line + footer);

		if (server.isDevServer())
		{
			builder.setType(Material.COMMAND);
			builder.addLore(
					C.cRed + "This is a server run by a",
					C.cRed + "developer. It may contain unreleased content."
			);
		}

		addButton(slot, builder.build(), (player, clickType) ->
		{
			if (canJoin && getPlugin().selectServer(player, server))
			{
				playAcceptSound(player);
			}
			else
			{
				playDenySound(player);
			}
		});
	}

	private class SwapViewButton implements IButton
	{
		@Override
		public void onClick(Player player, ClickType clickType)
		{
			_showInProgress = !_showInProgress;
			refresh();
			playAcceptSound(player);
		}
	}

	private String timeString(int time)
	{
		time = Math.max(0, time);

		return time + " Second" + (time == 1 ? "" : "s") + "...";
	}
}
