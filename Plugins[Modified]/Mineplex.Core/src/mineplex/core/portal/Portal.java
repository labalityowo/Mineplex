package mineplex.core.portal;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTabTitle;
import mineplex.core.portal.commands.SendCommand;
import mineplex.core.portal.commands.ServerCommand;
import mineplex.core.portal.events.GenericServerTransferEvent;
import mineplex.core.portal.events.ServerTransferEvent;
import mineplex.serverdata.Region;
import mineplex.serverdata.commands.ServerCommandManager;
import mineplex.serverdata.commands.TransferCommand;
import mineplex.serverdata.commands.TransferUUIDCommand;
import mineplex.serverdata.data.MinecraftServer;
import mineplex.serverdata.servers.ServerManager;
import mineplex.serverdata.servers.ServerRepository;

public class Portal extends MiniPlugin
{
	public enum Perm implements Permission
	{
		JOIN_FULL,
		JOIN_STAFF,
		SERVER_COMMAND_CLANS,
		SERVER_COMMAND,
		SEND_COMMAND,
	}
	
	// The singleton instance of Portal
	private static Portal instance;

	public static Portal getInstance()
	{
		return instance;
	}

	private final CoreClientManager _clientManager = require(CoreClientManager.class);

	private final ServerRepository _repository;
	private final Set<String> _connectingPlayers = Collections.synchronizedSet(new HashSet<>());

	public Portal()
	{
		super("Portal");

		instance = this;

		_repository = ServerManager.getServerRepository(getPlugin().getConfig().getBoolean("serverstatus.us") ? Region.US : Region.EU);

		Bukkit.getMessenger().registerOutgoingPluginChannel(getPlugin(), "BungeeCord");

		ServerCommandManager.getInstance().registerCommandType(TransferCommand.class, command ->
		{
			Player player = Bukkit.getPlayerExact(command.getPlayerName());

			if (player != null && player.isOnline())
			{
				sendPlayerToServer(player, command.getTargetServer(), Intent.FORCE_TRANSFER);
			}
		});
		ServerCommandManager.getInstance().registerCommandType(TransferUUIDCommand.class, command ->
		{
			Player player = Bukkit.getPlayer(command.getPlayerUUID());

			if (player != null && player.isOnline())
			{
				sendPlayerToServer(player, command.getTargetServer(), Intent.FORCE_TRANSFER);
			}
		});
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.ULTRA.setPermission(Perm.JOIN_FULL, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.SERVER_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.SEND_COMMAND, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.JOIN_STAFF, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.SERVER_COMMAND_CLANS, true, true);
	}

	public void sendAllPlayersToGenericServer(GenericServer hub, Intent kick)
	{
		for (Player player : UtilServer.GetPlayers())
		{
			sendPlayerToGenericServer(player, hub, kick);
		}
	}

	public void sendPlayerToGenericServer(Player player, GenericServer genericServer, Intent intent)
	{
		if (_connectingPlayers.contains(player.getName()))
			return;

		GenericServerTransferEvent event = new GenericServerTransferEvent(player, genericServer, intent);
		UtilServer.CallEvent(event);

		if (event.isCancelled())
		{
			return;
		}

		sendPlayer(player, genericServer.getName());
	}

	public void sendPlayerToServer(Player player, String serverName, Intent intent)
	{
		if (_connectingPlayers.contains(player.getName()))
		{
			return;
		}

		ServerTransferEvent event = new ServerTransferEvent(player, serverName, intent);
		UtilServer.CallEvent(event);

		if (event.isCancelled())
		{
			return;
		}

		runAsync(() ->
		{
			final MinecraftServer server = _repository.getServerStatus(serverName);

			if (server == null)
				return;
			
			runSync(() ->
			{
				if (server.getGroup().equalsIgnoreCase("Clans") && server.getMotd().equalsIgnoreCase("Restarting soon"))
				{
					UtilPlayer.message(player, F.main(getName(), C.cGold + serverName + C.cRed + " is restarting!"));
					return;
				}
				if (server.getPlayerCount() < server.getMaxPlayerCount() || server.getGroup().equalsIgnoreCase("Clans") || _clientManager.Get(player).hasPermission(Perm.JOIN_FULL))
				{
					sendPlayer(player, serverName);
				}
				else
				{
					UtilPlayer.message(player, F.main(getName(), C.cGold + serverName + C.cRed + " is full!"));
				}
			});
		});
	}

	public static void transferPlayer(String playerName, String serverName)
	{
		new TransferCommand(playerName, serverName).publish();
	}

	public static void transferPlayer(UUID playerUUID, String serverName)
	{
		new TransferUUIDCommand(playerUUID, serverName).publish();
	}

	public void doesServerExist(final String serverName, final Callback<Boolean> callback)
	{
		if (callback == null)
		{
			return;
		}

		runAsync(() ->
		{
			boolean result = _repository.serverExists(serverName);
			runSync(() -> callback.run(result));
		});
	}

	public void addCommands()
	{
		addCommand(new ServerCommand(this));
		addCommand(new SendCommand(this));
	}

	public void sendToHub(Player player, String message, Intent intent)
	{
		if (message != null)
		{
			UtilPlayer.message(player, "  ");
			UtilPlayer.message(player, C.cGold + C.Bold + message);
			UtilPlayer.message(player, "  ");
		}

		player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 10f, 1f);
		sendPlayerToGenericServer(player, GenericServer.HUB, intent);
	}

	/**
	 * Directly sends a player to the provided server name, bypassing all validation
	 *
	 * @param player     The player to send
	 * @param serverName The destination server
	 */
	public void sendPlayer(final Player player, String serverName)
	{
		if (player.getGameMode() == GameMode.SPECTATOR)
		{
			player.setSpectatorTarget(null);
		}

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);

		try
		{
			out.writeUTF("Connect");
			out.writeUTF(serverName);
		}
		catch (IOException ignored) {}

		player.sendPluginMessage(getPlugin(), "BungeeCord", b.toByteArray());
		_connectingPlayers.add(player.getName());

		getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> _connectingPlayers.remove(player.getName()), 20L);

		UtilPlayer.message(player, F.main(getName(), "You have been sent from " + C.cGold + UtilServer.getServerName() + C.cGray + " to " + C.cGold + serverName));
	}


	@EventHandler
	private void setTabHeaderAndFooterOnJoin(PlayerJoinEvent event)
	{
		UtilTabTitle.setHeaderAndFooter(
				event.getPlayer(),
				C.Bold + "Mineplex Network   " + C.cGreen + UtilServer.getServerName(),
				"Visit " + C.cGreen + "www.mineplex.com" + ChatColor.RESET + " for News, Forums and Shop"
		);
	}

	public ServerRepository getRepository()
	{
		return _repository;
	}
}