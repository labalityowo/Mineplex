package mineplex.bungee.lobbyBalancer;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import mineplex.serverdata.Region;
import mineplex.serverdata.data.MinecraftServer;
import mineplex.serverdata.servers.ServerManager;
import mineplex.serverdata.servers.ServerRepository;

public class LobbyBalancer implements Listener, Runnable
{
	private Plugin _plugin;
	private ServerRepository _repository;

	private final Map<LobbyType, List<MinecraftServer>> _sortedLobbyMap = new EnumMap<>(LobbyType.class);
	private final Map<LobbyType, Integer> _nextIndexMap = new EnumMap<>(LobbyType.class);
	private static final LobbySorter LOBBY_SORTER = new LobbySorter();
	private static final Object _serverLock = new Object();
	
	public LobbyBalancer(Plugin plugin)
	{
		_plugin = plugin;
		
		Region region = !new File("eu.dat").exists() ? Region.US : Region.EU;
		_repository = ServerManager.getServerRepository(region);
		
		run();
		
		_plugin.getProxy().getPluginManager().registerListener(_plugin, this);
		_plugin.getProxy().getScheduler().schedule(_plugin, this, 500L, 500L, TimeUnit.MILLISECONDS);
	}
	
	@EventHandler
	public void playerConnect(ServerConnectEvent event)
	{
		Arrays.stream(LobbyType.values())
				.filter(type -> type.getConnectName().equalsIgnoreCase(event.getTarget().getName()))
				.findFirst()
				.ifPresent(lobbyType ->
				{
					synchronized (_serverLock)
					{
						List<MinecraftServer> lobbies = _sortedLobbyMap.get(lobbyType);

						int nextIndex = _nextIndexMap.getOrDefault(lobbyType, 0);
						if (nextIndex >= lobbies.size())
						{
							nextIndex = 0;
						}

						MinecraftServer server = lobbies.get(nextIndex);

						event.setTarget(_plugin.getProxy().getServerInfo(server.getName()));
						server.incrementPlayerCount(1);
						System.out.println("Sending " + event.getPlayer().getName() + " to " + server.getName() + "(" + server.getPublicAddress() + ")");

						_nextIndexMap.put(lobbyType, ++nextIndex);
					}
				});
	}
	
	public void run()
	{
		loadServers();

		for (LobbyType type : LobbyType.values())
		{
			if (!_plugin.getProxy().getServers().containsKey(type.getConnectName()))
			{
				_plugin.getProxy().getServers().put(type.getConnectName(), _plugin.getProxy().constructServerInfo(type.getConnectName(), new InetSocketAddress("lobby.mineplex.com", 25565), "LobbyBalancer", false));
			}
		}
	}
    
	public void loadServers()
	{		
		Collection<MinecraftServer> servers = _repository.getServerStatuses();
			
		synchronized (_serverLock)
		{
			long startTime = System.currentTimeMillis();
			_sortedLobbyMap.clear();
			for (LobbyType type : LobbyType.values())
			{
				_sortedLobbyMap.put(type, new ArrayList<>());
			}

			for (MinecraftServer server : servers)
			{
				if (server.getName() == null)
					continue;
				
				InetSocketAddress socketAddress = new InetSocketAddress(server.getPublicAddress(), server.getPort());
				_plugin.getProxy().getServers().put(server.getName(), _plugin.getProxy().constructServerInfo(server.getName(), socketAddress, "LobbyBalancer", false));

				if (server.getMotd() != null && server.getMotd().contains("Restarting"))
				{
					continue;
				}

				Arrays.stream(LobbyType.values())
						.filter(type -> server.getName().toUpperCase().startsWith(type.getUppercasePrefix()))
						.findFirst()
						.ifPresent(type -> _sortedLobbyMap.get(type).add(server));
			}

			_sortedLobbyMap.values().forEach(lobbies -> Collections.sort(lobbies, LOBBY_SORTER));

            long timeSpentInLock = System.currentTimeMillis() - startTime;
            
            if (timeSpentInLock > 50)
            	System.out.println("[==] TIMING [==] Locked loading servers for " + timeSpentInLock + "ms");

            _nextIndexMap.clear();
		}
	}
}
