package mineplex.core.serverConfig;

import java.lang.reflect.Field;

import net.minecraft.server.v1_8_R3.PlayerList;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.UtilServer;
import mineplex.serverdata.Region;
import mineplex.serverdata.data.ServerGroup;
import mineplex.serverdata.servers.ServerManager;

public class ServerConfiguration extends MiniPlugin
{
	public enum Perm implements Permission
	{
		JOIN_STAFF_SERVER,
	}

	private CoreClientManager _clientManager;
	
	private Field _playerListMaxPlayers;
	private ServerGroup _serverGroup;

	public ServerConfiguration(JavaPlugin plugin, CoreClientManager clientManager)
	{
		super("Server Configuration", plugin);
		
		_clientManager = clientManager;
		Region region = plugin.getConfig().getBoolean("serverstatus.us") ? Region.US : Region.EU;
		String groupName = plugin.getConfig().getString("serverstatus.group");

		_serverGroup = ServerManager.getServerRepository(region).getServerGroup(groupName);
		
		if (_serverGroup == null)
			return;

		try
		{
			_playerListMaxPlayers = PlayerList.class.getDeclaredField("maxPlayers");
			_playerListMaxPlayers.setAccessible(true);
			_playerListMaxPlayers.setInt(((CraftServer)_plugin.getServer()).getHandle(), _serverGroup.getMaxPlayers());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		_plugin.getServer().setWhitelist(_serverGroup.getWhitelist());
		((CraftServer)_plugin.getServer()).getServer().setPVP(_serverGroup.getPvp());
//		((CraftServer)_plugin.getServer()).getServer().setResourcePack(_serverGroup.getResourcePack());
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.TRAINEE.setPermission(Perm.JOIN_STAFF_SERVER, true, true);
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		if (UtilServer.isTestServer() && !Bukkit.hasWhitelist())
		{
			return;
		}

		if (_serverGroup.getStaffOnly() && !_clientManager.Get(event.getPlayer().getUniqueId()).hasPermission(Perm.JOIN_STAFF_SERVER))
		{
			event.disallow(Result.KICK_OTHER, "This is a staff only server.");
		}
	}
	
	public ServerGroup getServerGroup()
	{
		return _serverGroup;
	}
}