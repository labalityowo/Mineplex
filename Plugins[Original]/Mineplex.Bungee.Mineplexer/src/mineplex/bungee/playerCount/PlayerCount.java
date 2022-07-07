package mineplex.bungee.playerCount;

import java.io.File;
import java.util.concurrent.TimeUnit;

import mineplex.bungee.status.InternetStatus;
import mineplex.serverdata.Region;
import mineplex.serverdata.data.BungeeServer;
import mineplex.serverdata.data.DataRepository;
import mineplex.serverdata.redis.RedisDataRepository;
import mineplex.serverdata.servers.ConnectionData;
import mineplex.serverdata.servers.ConnectionData.ConnectionType;
import mineplex.serverdata.servers.ServerManager;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class PlayerCount implements Listener, Runnable
{
	private DataRepository<BungeeServer> _repository;
	private DataRepository<BungeeServer> _secondRepository;
	private Region _region;
	
	private ListenerInfo _listenerInfo;
	private Plugin _plugin;
	
	private int _totalPlayers = -1;

	public PlayerCount(Plugin plugin)
	{
		_region = !new File("eu.dat").exists() ? Region.US : Region.EU;
		_plugin = plugin;
		
		_plugin.getProxy().getScheduler().schedule(_plugin, this, 4L, 4L, TimeUnit.SECONDS);
		_plugin.getProxy().getPluginManager().registerListener(_plugin, this);
		
		for (ListenerInfo info : _plugin.getProxy().getConfigurationAdapter().getListeners())
		{
			if (info.getDefaultServer().equalsIgnoreCase("Lobby"))
			{
				_listenerInfo = info;
			}
		}

		_repository = new RedisDataRepository<BungeeServer>(ServerManager.getConnection(true, ServerManager.SERVER_STATUS_LABEL), ServerManager.getConnection(false, ServerManager.SERVER_STATUS_LABEL),
													Region.ALL, BungeeServer.class, "bungeeServers");
		
		if (_region == Region.US)
			_secondRepository = new RedisDataRepository<BungeeServer>(new ConnectionData("10.81.1.156", 6379, ConnectionType.MASTER, "ServerStatus"), new ConnectionData("10.81.1.156", 6377, ConnectionType.SLAVE, "ServerStatus"),
				Region.ALL, BungeeServer.class, "bungeeServers");
		else
			_secondRepository = new RedisDataRepository<BungeeServer>(new ConnectionData("10.33.53.16", 6379, ConnectionType.MASTER, "ServerStatus"), new ConnectionData("10.33.53.16", 6377, ConnectionType.SLAVE, "ServerStatus"),
					Region.ALL, BungeeServer.class, "bungeeServers");
	}
	
	public void run()
	{
		BungeeServer snapshot = generateSnapshot();
		if (snapshot != null)
		{
			_repository.addElement(snapshot, 15);	// Update with a 15 second expiry on session
		}
		
		_totalPlayers = fetchPlayerCount();
	}
	
	/**
	 * @return an up-to-date total player count across all active Bungee Servers.
	 */
	private int fetchPlayerCount()
	{
		int totalPlayers = 0;
		for (BungeeServer server : _repository.getElements())
		{
			totalPlayers += server.getPlayerCount();
		}
		
		for (BungeeServer server : _secondRepository.getElements())
		{
			totalPlayers += server.getPlayerCount();
		}
		
		return totalPlayers;
	}
	
	/**
	 * @return a newly instantiated {@link BungeeServer} snapshot of the current state of this server.
	 */
	private BungeeServer generateSnapshot()
	{
		if (_listenerInfo == null)
		{
			return null;
		}
		String name = _listenerInfo.getHost().getAddress().getHostAddress();
		String host = _listenerInfo.getHost().getAddress().getHostAddress();
		int port = _listenerInfo.getHost().getPort();
		boolean connected = InternetStatus.isConnected();
		int playerCount = _plugin.getProxy().getOnlineCount();
		return new BungeeServer(name, _region, host, port, playerCount, connected);
	}
	
	@EventHandler
	public void ServerPing(ProxyPingEvent event)
	{
		net.md_5.bungee.api.ServerPing serverPing = event.getResponse();

		event.setResponse(new net.md_5.bungee.api.ServerPing(serverPing.getVersion(), new Players(_totalPlayers + 1, _totalPlayers, null), serverPing.getDescription(), serverPing.getFaviconObject()));
	}
	
	public int getTotalPlayers()
	{
		return _totalPlayers;
	}
}
