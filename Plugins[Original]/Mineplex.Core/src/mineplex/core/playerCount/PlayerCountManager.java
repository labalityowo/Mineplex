package mineplex.core.playerCount;

import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.serverdata.Region;
import mineplex.serverdata.data.BungeeServer;
import mineplex.serverdata.data.DataRepository;
import mineplex.serverdata.redis.RedisDataRepository;
import mineplex.serverdata.servers.ConnectionData;
import mineplex.serverdata.servers.ServerManager;

public class PlayerCountManager extends MiniPlugin
{
	private Region _region;
	private DataRepository<BungeeServer> _repository;
	private DataRepository<BungeeServer> _secondRepository;

	private volatile int _playerCount = 0;

	public PlayerCountManager(JavaPlugin plugin)
	{
		super("PlayerCount", plugin);

		_region = plugin.getConfig().getBoolean("serverstatus.us") ? Region.US : Region.EU;

		_repository = new RedisDataRepository<BungeeServer>(ServerManager.getConnection(true, ServerManager.SERVER_STATUS_LABEL), ServerManager.getConnection(false, ServerManager.SERVER_STATUS_LABEL),
				Region.ALL, BungeeServer.class, "bungeeServers");

		if (_region == Region.US)
			_secondRepository = new RedisDataRepository<BungeeServer>(new ConnectionData("10.81.1.156", 6379, ConnectionData.ConnectionType.MASTER, "ServerStatus"), new ConnectionData("10.81.1.156", 6377, ConnectionData.ConnectionType.SLAVE, "ServerStatus"),
					Region.ALL, BungeeServer.class, "bungeeServers");
		else
			_secondRepository = new RedisDataRepository<BungeeServer>(new ConnectionData("10.33.53.16", 6379, ConnectionData.ConnectionType.MASTER, "ServerStatus"), new ConnectionData("10.33.53.16", 6377, ConnectionData.ConnectionType.SLAVE, "ServerStatus"),
					Region.ALL, BungeeServer.class, "bungeeServers");

		//updatePlayerCount();
	}

	private void updatePlayerCount()
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

		_playerCount = totalPlayers;
	}

	public int getPlayerCount()
	{
		return _playerCount;
	}

	@EventHandler
	public void refresh(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC_08)
		{
			return;
		}

		runAsync(this::updatePlayerCount);
	}
}