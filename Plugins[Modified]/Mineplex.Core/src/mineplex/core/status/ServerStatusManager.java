package mineplex.core.status;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.ImmutableSet;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.Constants;
import mineplex.core.monitor.LagMeter;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.serverdata.Region;
import mineplex.serverdata.Utility;
import mineplex.serverdata.commands.ServerCommandManager;
import mineplex.serverdata.commands.SuicideCommand;
import mineplex.serverdata.data.MinecraftServer;
import mineplex.serverdata.data.ServerGroup;
import mineplex.serverdata.servers.ServerManager;
import mineplex.serverdata.servers.ServerRepository;

public class ServerStatusManager extends MiniPlugin
{
	private static final Set<PermissionGroup> DONOR_GROUPS = ImmutableSet.of(PermissionGroup.ULTRA, PermissionGroup.HERO, PermissionGroup.LEGEND, PermissionGroup.TITAN, PermissionGroup.ETERNAL);
	// The default timeout (in seconds) before the ServerStatus expires.
	public final int DEFAULT_SERVER_TIMEOUT = 30;

	private ServerRepository _repository;
	private CoreClientManager _clientManager;
	private LagMeter _lagMeter;

	private String _name;
	private Region _region;

	private boolean _enabled = true;

	private long _startUpDate;

	public ServerStatusManager(JavaPlugin plugin, CoreClientManager clientManager, LagMeter lagMeter)
	{
		super("Server Status Manager", plugin);

		_startUpDate = Utility.currentTimeSeconds();
		_clientManager = clientManager;
		_lagMeter = lagMeter;

		if (new File("IgnoreUpdates.dat").exists() && !(new File("EnableStatus.dat").exists()))
			_enabled = false;

		setupConfigValues();

		_name = plugin.getConfig().getString("serverstatus.name");

		_region = plugin.getConfig().getBoolean("serverstatus.us") ? Region.US : Region.EU;

		ServerCommandManager.getInstance().initializeServer(_name, Constants.GSON);
		ServerCommandManager.getInstance().registerCommandType("SuicideCommand", SuicideCommand.class, new SuicideHandler(this, _name, _region));

		_repository = ServerManager.getServerRepository(_region);
		saveServerStatus();
	}

	private void setupConfigValues()
	{
		try
		{
			getPlugin().getConfig().addDefault("serverstatus.connectionurl", "db.mineplex.com:3306");
			getPlugin().getConfig().set("serverstatus.connectionurl", getPlugin().getConfig().getString("serverstatus.connectionurl"));

			getPlugin().getConfig().addDefault("serverstatus.username", "MilitaryPolice");
			getPlugin().getConfig().set("serverstatus.username", getPlugin().getConfig().getString("serverstatus.username"));

			getPlugin().getConfig().addDefault("serverstatus.password", "CUPr6Wuw2Rus$qap");
			getPlugin().getConfig().set("serverstatus.password", getPlugin().getConfig().getString("serverstatus.password"));

			getPlugin().getConfig().addDefault("serverstatus.us", true);
			getPlugin().getConfig().set("serverstatus.us", getPlugin().getConfig().getBoolean("serverstatus.us"));

			getPlugin().getConfig().addDefault("serverstatus.name", "TEST-1");
			getPlugin().getConfig().set("serverstatus.name", getPlugin().getConfig().getString("serverstatus.name"));

			getPlugin().getConfig().addDefault("serverstatus.group", "Testing");
			getPlugin().getConfig().set("serverstatus.group", getPlugin().getConfig().getString("serverstatus.group"));

			getPlugin().saveConfig();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@EventHandler
	public void saveServerStatus(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		if (_enabled)
			saveServerStatus();
	}

	/**
	 * Save the current {@link MinecraftServer} snapshot of this server to
	 * the {@link ServerRepository}.
	 */
	private void saveServerStatus()
	{
		final MinecraftServer serverSnapshot = generateServerSnapshot();
		getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable()
		{
			public void run()
			{
				MinecraftServer server = _repository.getServerStatus(serverSnapshot.getName());
				int timeout = DEFAULT_SERVER_TIMEOUT;

				if (server != null && !server.getPublicAddress().equalsIgnoreCase(serverSnapshot.getPublicAddress()))
				{
					timeout = -DEFAULT_SERVER_TIMEOUT;
				}

				_repository.updataServerStatus(serverSnapshot, timeout);
			}
		});
	}

	/**
	 * @return a newly instanced {@link MinecraftServer} snapshot that represents the
	 * current internal state of this minecraft server.
	 */
	private MinecraftServer generateServerSnapshot()
	{
		ServerListPingEvent event = new ServerListPingEvent(null, getPlugin().getServer().getMotd(), getPlugin().getServer().getOnlinePlayers().size(), getPlugin().getServer().getMaxPlayers());
		getPluginManager().callEvent(event);

		String motd = _enabled ? event.getMotd() : "Restarting";
		int playerCount = _clientManager.getPlayerCountIncludingConnecting();
		int maxPlayerCount = event.getMaxPlayers();
		int tps = (int) Math.max(_lagMeter.getRecentTicksPercentageAverage(), _lagMeter.getTicksPerSecond());
		String address = Bukkit.getServer().getIp().isEmpty() ? "localhost" : Bukkit.getServer().getIp();
		int port = _plugin.getServer().getPort();
		String group = _plugin.getConfig().getString("serverstatus.group") + "";
		int ram = (int) ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / 1048576);
		int maxRam = (int) (Runtime.getRuntime().maxMemory() / 1048576);

		int donorsOnline = 0;

		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (DONOR_GROUPS.contains(_clientManager.Get(player).getPrimaryGroup()))
			{
				donorsOnline++;
			}
		}

		return new MinecraftServer(_name, group, motd, address, port, playerCount,
				maxPlayerCount, tps, ram, maxRam, _startUpDate, donorsOnline);
	}

	public String getCurrentServerName()
	{
		return _name;
	}

	public Collection<ServerGroup> retrieveServerGroups()
	{
		return _enabled ? _repository.getServerGroups(null) : null;
	}

	public Collection<MinecraftServer> retrieveServerStatuses()
	{
		return _enabled ? _repository.getServerStatuses() : null;
	}

	public Region getRegion()
	{
		return _region;
	}

	public void disableStatus()
	{
		_enabled = false;
		saveServerStatus();
	}
}