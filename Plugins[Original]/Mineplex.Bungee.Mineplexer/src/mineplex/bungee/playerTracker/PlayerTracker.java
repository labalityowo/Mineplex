package mineplex.bungee.playerTracker;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import mineplex.serverdata.Region;
import mineplex.serverdata.commands.PlayerJoinCommand;
import mineplex.serverdata.commands.ServerCommandManager;
import mineplex.serverdata.data.DataRepository;
import mineplex.serverdata.data.PlayerStatus;
import mineplex.serverdata.redis.RedisDataRepository;
import mineplex.serverdata.servers.ServerManager;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class PlayerTracker implements Listener
{
	// Default period before status expiry (8 hours)
	private static final int DEFAULT_STATUS_TIMEOUT = 60 * 60 * 8;

	// Repository storing player status' across network.
	private DataRepository<PlayerStatus> _repository;
	
	private Plugin _plugin;
	
	private final List<UUID> _ignoreKick = Lists.newArrayList();
	
	public PlayerTracker(Plugin plugin)
	{
		_plugin = plugin;

		_plugin.getProxy().getPluginManager().registerListener(_plugin, this);
		
		_repository = new RedisDataRepository<PlayerStatus>(ServerManager.getMasterConnection(), ServerManager.getSlaveConnection(),
				Region.currentRegion(), PlayerStatus.class, "playerStatus");
		
		ServerCommandManager.getInstance().initializeServer("BUNGEE ENABLE - " + System.currentTimeMillis(), new Gson());
		ServerCommandManager.getInstance().registerCommandType(mineplex.serverdata.commands.PlayerJoinCommand.class, new PlayerJoinHandler(this));
		
		System.out.println("Initialized PlayerTracker.");
	}
	
	public Plugin getPlugin()
	{
		return _plugin;
	}

	@EventHandler
	public void playerConnect(final ServerConnectedEvent event)
	{
		_plugin.getProxy().getScheduler().runAsync(_plugin, new Runnable()
		{
			public void run()
			{
				PlayerStatus snapshot = new PlayerStatus(event.getPlayer().getUniqueId(), event.getPlayer().getName(), event.getServer().getInfo().getName());
				_repository.addElement(snapshot, DEFAULT_STATUS_TIMEOUT);
			}
		});
	}

	@EventHandler
	public void playerDisconnect(final PlayerDisconnectEvent event)
	{
		_plugin.getProxy().getScheduler().runAsync(_plugin, new Runnable()
		{
			public void run()
			{
				_repository.removeElement(event.getPlayer().getUniqueId().toString());
			}
		});
	}
	
	@EventHandler
	public void playerConnect(final PostLoginEvent event)
	{
		_ignoreKick.add(event.getPlayer().getUniqueId());
		PlayerJoinCommand command = new PlayerJoinCommand(event.getPlayer().getUniqueId(), event.getPlayer().getName());
		command.publish();
	}

	public boolean isPlayerOnline(UUID uuid) 
	{
		return _plugin.getProxy().getPlayer(uuid) != null;
	}

	public void kickPlayerIfOnline(UUID uuid) 
	{
		if (_ignoreKick.remove(uuid))
		{
			return;
		}
		if (isPlayerOnline(uuid))
		{
			ProxiedPlayer player = _plugin.getProxy().getPlayer(uuid);
			
			player.disconnect(new TextComponent("You have logged in from another location."));
		}
	}
}
