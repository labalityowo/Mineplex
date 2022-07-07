package mineplex.clanshub;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;
import com.mineplex.ProtocolVersion;

import mineplex.clanshub.queue.HubQueueManager;
import mineplex.core.MiniDbClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.donation.DonationManager;
import mineplex.core.npc.event.NpcDamageByEntityEvent;
import mineplex.core.npc.event.NpcInteractEntityEvent;
import mineplex.core.party.PartyManager;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.core.repository.tokens.SimpleClanToken;
import mineplex.serverdata.Region;
import mineplex.serverdata.data.MinecraftServer;
import mineplex.serverdata.servers.ServerManager;

/**
 * Server selection controller for clans
 */
public class ClansTransferManager extends MiniDbClientPlugin<SimpleClanToken>
{
	public enum Perm implements Permission
	{
		STAFF_PAGE,
		ALLOW_HARDCORE,
	}
	
	private static final int[] PERMITTED_VERSIONS = {ProtocolVersion.v1_8, ProtocolVersion.v1_12, ProtocolVersion.v1_12_1, ProtocolVersion.v1_12_2};
	private static final Map<Integer, String> VERSION_NAMES = new HashMap<>();
	
	static
	{
		for (Field field : ProtocolVersion.class.getFields())
		{
			try
			{
				int protocol = field.getInt(null);
				String version = field.getName().replace("v", "").replace("_", ".");

				VERSION_NAMES.put(protocol, version);
			}
			catch (ReflectiveOperationException ex) {}
		}
	}

	private static final long SERVER_RELOAD_INTERVAL = 5000;
	private PartyManager _party;
	private Portal _portal;
	private Region _region;
	private final Map<MinecraftServer, ServerInfo> _servers = new HashMap<>();
	private boolean _loading = false;
	private long _lastLoaded;
	private ClansServerShop _serverShop;
	private final HubQueueManager _queue = require(HubQueueManager.class);
	
	public ClansTransferManager(JavaPlugin plugin, CoreClientManager client, DonationManager donation, PartyManager party, Portal portal)
	{
		super("Server Transfer", plugin, client);
		
		_party = party;
		_portal = portal;
		_region = plugin.getConfig().getBoolean("serverstatus.us") ? Region.US : Region.EU;
		_serverShop = new ClansServerShop(this, client, donation);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.TRAINEE.setPermission(Perm.STAFF_PAGE, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.ALLOW_HARDCORE, true, true);
		PermissionGroup.CONTENT.setPermission(Perm.ALLOW_HARDCORE, true, true);
	}
	
	private boolean checkCanJoinClans(Player player)
	{
		int protocol = ((CraftPlayer)player).getHandle().getProtocol();
		if (ArrayUtils.contains(PERMITTED_VERSIONS, protocol))
		{
			return true;
		}
		
		UtilPlayer.message(player, F.main(getName(), "Minecraft Version " + VERSION_NAMES.get(protocol) + " is not supported on Mineplex Clans.\n"
		+ C.cGray + "Please change your Minecraft version to 1.8.8 or 1.12+ and reconnect!"));
		
		return false;
	}
	
	/**
	 * Gets the stored party manager
	 * @return The stored party manager
	 */
	public PartyManager getPartyManager()
	{
		return _party;
	}
	
	/**
	 * Gets a list of all loaded servers
	 * @return A list of all loaded servers
	 */
	public List<ServerInfo> getServers(boolean onlineOnly)
	{
		List<ServerInfo> servers = Lists.newArrayList();
		for (ServerInfo info : _servers.values())
		{
			if (!(info.MOTD.equalsIgnoreCase("Restarting soon") || _queue.getData(info) == null) || !onlineOnly)
			{
				servers.add(info);
			}
		}
		return servers;
	}
	
	/**
	 * Gets the loaded ServerInfo with the given name
	 * @param name The name to check
	 * @return The loaded ServerInfo, or null if it is not stored
	 */
	public ServerInfo getServer(String name)
	{
		for (ServerInfo server : _servers.values())
		{
			if (server.Name.equalsIgnoreCase(name) && !server.MOTD.equalsIgnoreCase("Restarting soon") && _queue.getData(server) != null)
			{
				return server;
			}
		}
		
		return null;
	}
	
	/**
	 * Pulls all the clans servers from redis and loads them. SHOULD BE RUN ASYNC
	 */
	public void reload()
	{
		_servers.clear();
		for (MinecraftServer server : ServerManager.getServerRepository(_region).getServerStatusesByPrefix("Clans-"))
		{
			ServerInfo info = new ServerInfo();
			info.Name = server.getName();
			info.MOTD = server.getMotd();
			info.CurrentPlayers = server.getPlayerCount();
			info.MaxPlayers = server.getMaxPlayerCount();
			info.Hardcore = server.getMotd().contains("Hardcore");
			_servers.put(server, info);
		}
	}
	
	@EventHandler
	public void reloadServers(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || _loading || !UtilTime.elapsed(_lastLoaded, SERVER_RELOAD_INTERVAL))
		{
			return;
		}
		_loading = true;
		final Runnable after = () ->
		{
			_lastLoaded = System.currentTimeMillis();
			_loading = false;
		};
		runAsync(() ->
		{
			reload();
			runSync(after);
		});
	}
	
	@EventHandler
	public void refreshPages(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		_serverShop.getPageMap().values().stream().filter(page -> page instanceof ClansServerPage).forEach(page ->
		{
			((ClansServerPage) page).update();
		});
	}
	
	@EventHandler
	public void onUseNPC(NpcInteractEntityEvent event)
	{
		if (event.getNpc().getName().contains("Clans"))
		{
			if (checkCanJoinClans(event.getPlayer()))
			{
				_serverShop.attemptShopOpen(event.getPlayer());
			}
		}
		if (event.getNpc().getName().contains("Return"))
		{
			_portal.sendToHub(event.getPlayer(), "Returning to Mineplex!", Intent.PLAYER_REQUEST);
		}
	}
	
	@EventHandler
	public void onUseNPC(NpcDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player))
		{
			return;
		}
		Player player = (Player) event.getDamager();
		
		if (event.getNpc().getName().contains("Clans") && Recharge.Instance.use(player, "Go to Clans", 1000, false, false))
		{
			if (checkCanJoinClans(player))
			{
				_serverShop.attemptShopOpen(player);
			}
		}
		if (event.getNpc().getName().contains("Return") && Recharge.Instance.use(player, "Return to Mineplex", 1000, false, false))
		{
			_portal.sendToHub(player, "Returning to Mineplex!", Intent.PLAYER_REQUEST);
		}
	}
	
	@Override
	public String getQuery(int accountId, String uuid, String name) 
	{
		return "SELECT clans.name, accountClan.clanRole, clanServer.serverName, clans.id FROM accountClan INNER JOIN clans ON clans.id = accountClan.clanId INNER JOIN clanServer ON clans.serverId = clanServer.id WHERE accountClan.accountId = " + accountId + ";";
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		SimpleClanToken clanToken = new SimpleClanToken();
		
		while (resultSet.next())
		{
			String clanName = resultSet.getString(1);
			String clanRole = resultSet.getString(2);
			String homeServer = resultSet.getString(3);
			int clanId = resultSet.getInt(4);
			clanToken = new SimpleClanToken(clanName, clanRole, homeServer, clanId);
		}
		
		Set(uuid, clanToken);
	}

	@Override
	protected SimpleClanToken addPlayer(UUID uuid)
	{
		return new SimpleClanToken();
	}
}