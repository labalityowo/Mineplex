package mineplex.core.personalServer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.communities.data.Community;
import mineplex.core.recharge.Recharge;
import mineplex.serverdata.Region;
import mineplex.serverdata.data.ServerGroup;
import mineplex.serverdata.servers.ServerManager;
import mineplex.serverdata.servers.ServerRepository;

@ReflectivelyCreateMiniPlugin
public class PersonalServerManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		MPS,
		ADVANCED_MPS,
		EVENT_COMMAND,
		PERSONAL_EVENT_COMMAND,
	}

	private ServerRepository _repository;
	private CoreClientManager _clientManager;
	
	private boolean _us;

	private PersonalServerManager()
	{
		super("MPS");

		_clientManager = require(CoreClientManager.class);
		
		setupConfigValues();
		
		_us = _plugin.getConfig().getBoolean("serverstatus.us");
		
		Region region = _us ? Region.US : Region.EU;
		_repository = ServerManager.getServerRepository(region);

		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.LEGEND.setPermission(Perm.MPS, true, true);
		PermissionGroup.CONTENT.setPermission(Perm.ADVANCED_MPS, true, true);
		PermissionGroup.YT.setPermission(Perm.ADVANCED_MPS, true, false);
		PermissionGroup.SRMOD.setPermission(Perm.ADVANCED_MPS, true, true);
		PermissionGroup.EVENTMOD.setPermission(Perm.EVENT_COMMAND, false, true);
		PermissionGroup.EVENTMOD.setPermission(Perm.PERSONAL_EVENT_COMMAND, false, true);
		PermissionGroup.ADMIN.setPermission(Perm.EVENT_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.PERSONAL_EVENT_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new HostServerCommand(this));
		addCommand(new HostEventServerCommand(this));
		addCommand(new HostPersonalEventServer(this));
	}
	
	private void setupConfigValues()
	{
	    try 
	    {			
			getPlugin().getConfig().addDefault("serverstatus.us", true);
			getPlugin().getConfig().set("serverstatus.us", getPlugin().getConfig().getBoolean("serverstatus.us"));
			
			getPlugin().saveConfig();
	    } 
	    catch (Exception e) 
	    {
	    	e.printStackTrace();
	    }
	}
	
	public void hostServer(Player player, String serverName, boolean eventServer)
	{
		hostServer(player, serverName, eventServer, false);
	}
	
	public void hostServer(Player player, String serverName, boolean eventServer, boolean eventgame)
	{
		if (!Recharge.Instance.use(player, "Host Server", 30000, false, false))
		{
			return;
		}

		if (_clientManager.Get(player).isDisguised())
		{
			UtilPlayer.message(player, F.main("Disguise", "You can't create a MPS while you are disguised!"));
			return;
		}

		int ram = 1024;
		int cpu = 1;

		if (_clientManager.Get(player).hasPermission(Perm.ADVANCED_MPS))
		{
			ram = 2048;
			cpu = 4;
		}

		if (eventServer)
		{
			ram = 4096;
			cpu = 8;
			createGroup(player, "EVENT", ram, cpu, 40, 80, "Event", eventServer);
		}
		else
		{
			if (eventgame)
			{
				createGroup(player, serverName, ram, cpu, 40, 80, "Event", eventServer);	
			}
			else
			{
				createGroup(player, serverName, ram, cpu, 40, 80, "Smash", eventServer);	
			}
		}
	}
	
	public void hostCommunityServer(Player host, Community community)
	{
		int ram = 2048;
		int cpu = 4;
		
		runAsync(() ->
		{
			for (ServerGroup existingServerGroup : _repository.getServerGroups(null))
			{
				if (existingServerGroup.getPrefix().equalsIgnoreCase("COM-" + community.getName()) || existingServerGroup.getName().equalsIgnoreCase("COM-" + community.getId()))
				{
					host.sendMessage(F.main(getName(), "Your server is still being created or already exists.  If you have just started it up, wait 20 seconds and type /server COM-" + community.getName() + "-1."));
					return;
				}
			}
			
			final ServerGroup serverGroup = new ServerGroup("COM-" + community.getId(), "COM-" + community.getName(), "COM-" + community.getId(), ram, cpu, 1, 0, UtilMath.random.nextInt(250) + 19999, "", true, "Lobby_MCS.zip", "Arcade.jar", "plugins/Arcade/", 15, 20,
					true, false, false, community.getFavoriteGame().name(), "", "", "Community", true, false, false, true, false, false, true, false, false, false, false, false, false, true, true, true, false, false, false, "", _us ? Region.US : Region.EU, "", "", "", "");
			
			_repository.updateServerGroup(serverGroup);
			runSync(() ->
			{
				host.sendMessage(F.main(getName(), "COM-" + community.getName() + "-1 successfully created. You will be able to join it shortly."));
				host.sendMessage(F.main(getName(), "In around 10 seconds, type /server COM-" + community.getName() + "-1."));
			});
		});
	}
	
	private void createGroup(final Player host, final String serverName, final int ram, final int cpu, final int minPlayers, final int maxPlayers, final String games, final boolean event)
	{
		getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () ->
		{
			for (ServerGroup existingServerGroup : _repository.getServerGroups(null))
			{
				if (existingServerGroup.getPrefix().equalsIgnoreCase(serverName) || existingServerGroup.getName().equalsIgnoreCase(serverName))
				{
					if (host.getName().equalsIgnoreCase(existingServerGroup.getHost()))
					{
						host.sendMessage(F.main(getName(), "Your server is still being created or already exists.  If you haven't been connected in 20 seconds, type /server " + serverName + "-1."));
					}
					else
					{
						host.sendMessage(C.cRed + "Sorry, but you're not allowed to create a MPS server because you have chosen a name to glitch the system :)");
					}

					return;
				}
			}

			final ServerGroup serverGroup = new ServerGroup(serverName, serverName, host.getName(), ram, cpu, 1, 0, UtilMath.random.nextInt(250) + 19999, "", true, "Lobby_MPS.zip", "Arcade.jar", "plugins/Arcade/", minPlayers, maxPlayers,
					true, false, false, games, "", "", "Player", true, event, false, true, false, true, true, false, false, false, false, false, false, true, true, true, false, false, false, "", _us ? Region.US : Region.EU, "", "", "", "");

			getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () ->
			{
				_repository.updateServerGroup(serverGroup);
				Bukkit.getScheduler().runTask(getPlugin(), () ->
				{
					host.sendMessage(F.main(getName(), serverName + "-1 successfully created.  You will be sent to it shortly."));
					host.sendMessage(F.main(getName(), "If you haven't been connected in 20 seconds, type /server " + serverName + "-1."));
				});
			});
		});
	}
	
	public CoreClientManager getClients()
	{
		return _clientManager;
	}
}