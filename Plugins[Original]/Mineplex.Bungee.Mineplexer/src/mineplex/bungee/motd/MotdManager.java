package mineplex.bungee.motd;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import mineplex.bungee.lobbyBalancer.LobbyType;
import mineplex.serverdata.Region;
import mineplex.serverdata.data.DataRepository;
import mineplex.serverdata.redis.RedisDataRepository;
import mineplex.serverdata.servers.ServerManager;

public class MotdManager implements Listener, Runnable
{
	private static final String DEFAULT_HEADLINE = "                §b§l§m   §8§l§m[ §r §9§lMineplex§r §f§lGames§r §8§l§m ]§b§l§m   §r";

	private final DataRepository<GlobalMotd> _repository;
	private final Random _random = new Random();
	private final Map<LobbyType, GlobalMotd> motds = new EnumMap<>(LobbyType.class);
	
	public MotdManager(Plugin plugin)
	{
		plugin.getProxy().getScheduler().schedule(plugin, this, 5L, 30L, TimeUnit.SECONDS);
		plugin.getProxy().getPluginManager().registerListener(plugin, this);
		
		_repository = new RedisDataRepository<GlobalMotd>(ServerManager.getConnection(true, ServerManager.SERVER_STATUS_LABEL), ServerManager.getConnection(false, ServerManager.SERVER_STATUS_LABEL),
				Region.ALL, GlobalMotd.class, "globalMotd");
		run();
	}
 
	@EventHandler
	public void serverPing(ProxyPingEvent event)
	{

		net.md_5.bungee.api.ServerPing serverPing = event.getResponse();
		Optional<LobbyType> maybeType = Optional.empty();

		if (event.getConnection().getListener() != null)
		{
			maybeType = Arrays.stream(LobbyType.values())
					.filter(type -> event.getConnection().getListener().getDefaultServer().equalsIgnoreCase(type.getConnectName()))
					.findFirst();
		}

		LobbyType lobbyType = maybeType.orElse(LobbyType.NORMAL);
		GlobalMotd globalMotd = motds.get(lobbyType);

		String motd = DEFAULT_HEADLINE;
		if (globalMotd != null && globalMotd.getHeadline() != null)
		{
			motd = globalMotd.getHeadline() == null ? DEFAULT_HEADLINE : globalMotd.getHeadline();
			if (globalMotd.getMotd() != null)
			{
				motd += "\n" + globalMotd.getMotd().get(_random.nextInt(globalMotd.getMotd().size()));
			}
		}

		event.setResponse(new net.md_5.bungee.api.ServerPing(serverPing.getVersion(), serverPing.getPlayers(), motd, serverPing.getFaviconObject()));
	}
	
	@Override
	public void run()
	{
		for (LobbyType type : LobbyType.values())
		{
			GlobalMotd motd = _repository.getElement(type.getRedisMotdKey());

			if (motd != null)
			{
				motds.put(type, motd);
			}
		}
	}
}
