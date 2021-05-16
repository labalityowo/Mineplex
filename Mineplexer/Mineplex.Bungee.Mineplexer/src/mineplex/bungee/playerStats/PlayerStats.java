package mineplex.bungee.playerStats;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import mineplex.bungee.playerStats.data.IpInfo;
import mineplex.cache.player.PlayerCache;
import mineplex.cache.player.PlayerInfo;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class PlayerStats implements Listener, Runnable
{
	private Plugin _plugin;
	private PlayerStatsRepository _repository;
	
	private HashSet<UUID> _retrievingPlayerInfo = new HashSet<UUID>();
	
	public PlayerStats(Plugin plugin)
	{
		_plugin = plugin;

		_plugin.getProxy().getScheduler().schedule(_plugin, this, 5L, 5L, TimeUnit.MINUTES);
		_plugin.getProxy().getPluginManager().registerListener(_plugin, this);
		
		_repository = new PlayerStatsRepository();
	}
		
	@EventHandler
	public void playerConnect(final PostLoginEvent event)
	{
		_plugin.getProxy().getScheduler().runAsync(_plugin, new Runnable()
		{
			public void run()
			{
				String address = event.getPlayer().getPendingConnection().getAddress().getAddress().getHostAddress();
				UUID uuid = event.getPlayer().getUniqueId();
				String name = event.getPlayer().getName();
				int version = event.getPlayer().getPendingConnection().getVersion();
				
				try
				{
					PlayerInfo playerInfo = null;
					IpInfo ipInfo = _repository.getIp(address);
					
					boolean addOrUpdatePlayer = false;
					
					playerInfo = PlayerCache.getInstance().getPlayer(uuid);
					
					if (playerInfo == null)
					{
						addOrUpdatePlayer = true;
						_retrievingPlayerInfo.add(uuid);
					}

					if (!addOrUpdatePlayer)
					{
						if (playerInfo.getVersion() != version)
							addOrUpdatePlayer = true;
						else if (!playerInfo.getName().equalsIgnoreCase(name))
							addOrUpdatePlayer = true;
					}
					
					if (addOrUpdatePlayer)
					{
						// Just update? what about other properties?
						PlayerInfo updatedPlayerInfo = _repository.getPlayer(uuid, name, version);
						
						if (playerInfo != null)
						{
							playerInfo.setName(updatedPlayerInfo.getName());
							playerInfo.setVersion(updatedPlayerInfo.getVersion());
						}
						else
							playerInfo = updatedPlayerInfo;
					}
					
					playerInfo.setSessionId(_repository.updatePlayerStats(playerInfo.getId(), ipInfo.id));
					playerInfo.updateLoginTime();
					PlayerCache.getInstance().addPlayer(playerInfo);
				}
				finally
				{
					_retrievingPlayerInfo.remove(uuid);
				}
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
				UUID uuid = event.getPlayer().getUniqueId();
				
				PlayerInfo playerInfo = null;
				
				playerInfo = PlayerCache.getInstance().getPlayer(uuid);
				
				int timeout = 5;
				
				while (playerInfo == null && _retrievingPlayerInfo.contains(uuid) && timeout <= 5)
				{
					playerInfo = PlayerCache.getInstance().getPlayer(uuid);
					
					if (playerInfo != null)
						break;
					
					System.out.println("ERROR - Player disconnecting and isn't in cache... sleeping");
					
					try
					{
						Thread.sleep(500);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					
					timeout++;
				}
				
				System.out.println(playerInfo.getName() + ":" + playerInfo.getSessionId());
				_repository.updatePlayerSession(playerInfo.getSessionId());
			}
		});
	}

	@Override
	public void run()
	{
		PlayerCache.getInstance().clean();
	}
}
