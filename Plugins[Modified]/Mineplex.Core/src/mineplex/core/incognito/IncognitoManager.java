package mineplex.core.incognito;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.Managers;
import mineplex.core.MiniDbClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.incognito.commands.IncognitoToggleCommand;
import mineplex.core.incognito.events.IncognitoHidePlayerEvent;
import mineplex.core.incognito.events.IncognitoStatusChangeEvent;
import mineplex.core.incognito.repository.IncognitoClient;
import mineplex.core.incognito.repository.IncognitoRepository;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.visibility.VisibilityManager;

public class IncognitoManager extends MiniDbClientPlugin<IncognitoClient>
{
	public enum Perm implements Permission
	{
		USE_INCOGNITO,
	}

	private CoreClientManager _clientManager;
	private IncognitoRepository _repository;
	private PreferencesManager _preferencesManager;
	
	public IncognitoManager(JavaPlugin plugin, CoreClientManager clientManager, PacketHandler packetHandler)
	{
		super("Incognito", plugin, clientManager);
		
		_repository = new IncognitoRepository(this);
		_clientManager = clientManager;
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.TRAINEE.setPermission(Perm.USE_INCOGNITO, true, true);
	}
	
	private boolean canSeeThroughIncognito(Player viewer, Player target)
	{
		PermissionGroup viewerGroup = _clientManager.Get(viewer).getPrimaryGroup();
		PermissionGroup targetGroup = _clientManager.Get(target).getPrimaryGroup();

		return viewerGroup.inheritsFrom(targetGroup);

	}

	public void addCommands()
	{
		addCommand(new IncognitoToggleCommand(this));
	}

	public boolean toggle(Player caller)
	{
		boolean enabled = !Get(caller).Status;
		
		VisibilityManager vm = Managers.require(VisibilityManager.class);

		IncognitoStatusChangeEvent event = UtilServer.CallEvent(new IncognitoStatusChangeEvent(caller, enabled));

		if (event.isCancelled())
		{
			return false;
		}

		Get(caller).Status = enabled;
		
		if (!enabled)
		{
			if (event.doShow())
			{
				for (Player other : UtilServer.getPlayers())
				{
					vm.showPlayer(other, caller, "Incognito Mode");
				}
			}
		}
		
		runAsync(() -> _repository.setStatus(_clientManager.getAccountId(caller), enabled));

		return enabled;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void Join(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		
		if (Get(event.getPlayer()).Status && !_clientManager.Get(event.getPlayer()).hasPermission(Perm.USE_INCOGNITO))
		{
			Get(event.getPlayer()).Status = false;
			runAsync(() -> _repository.setStatus(_clientManager.getAccountId(player), false));
			return;
		}
		
		if (Get(event.getPlayer()).Status)
		{
			event.setJoinMessage(null);
			informIncognito(player);
		}
		
		IncognitoHidePlayerEvent customEvent = null;
		
		if (Get(event.getPlayer()).Status)
		{
			customEvent = UtilServer.CallEvent(new IncognitoHidePlayerEvent(player));
		}
		
		for (Player other : UtilServer.getPlayers())
		{
			if (customEvent != null && !customEvent.isCancelled() && !canSeeThroughIncognito(other, player))
			{
				vm.hidePlayer(other, player, "Incognito Mode");
			}
			
			if (Get(other).Status)
			{
				IncognitoHidePlayerEvent customEvent2 = UtilServer.CallEvent(new IncognitoHidePlayerEvent(other));
				
				if (!customEvent2.isCancelled() && !canSeeThroughIncognito(player, other))
				{
					vm.hidePlayer(player, other, "Incognito Mode");
				}
			}
		}
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		
		for (Player player : UtilServer.getPlayers())
		{
			for (Player other : UtilServer.getPlayers())
			{
				if (Get(player).Status)
				{
					IncognitoHidePlayerEvent customEvent = UtilServer.CallEvent(new IncognitoHidePlayerEvent(player));
					
					if (!customEvent.isCancelled() && !canSeeThroughIncognito(other, player))
					{
						vm.hidePlayer(other, player, "Incognito Mode");
					}
					
					Get(player).Hidden = !customEvent.isCancelled();
				}
				else
				{
					Get(player).Hidden = false;
				}
				
				if (Get(other).Status)
				{
					IncognitoHidePlayerEvent customEvent = UtilServer.CallEvent(new IncognitoHidePlayerEvent(other));
					
					if (!customEvent.isCancelled() && !canSeeThroughIncognito(player, other))
					{
						vm.hidePlayer(player, other, "Incognito Mode");
					}
					
					Get(other).Hidden = !customEvent.isCancelled();
				}
				else
				{
					Get(other).Hidden = false;
				}
			}	
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void Quit(PlayerQuitEvent event)
	{
		if (Get(event.getPlayer()).Status)
		{
			event.setQuitMessage(null);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void Kick(PlayerKickEvent event)
	{
		if (Get(event.getPlayer()).Status)
		{
			event.setLeaveMessage(null);
		}
	}

	private void informIncognito(Player player)
	{
		UtilPlayer.message(player, " ");
		UtilPlayer.message(player, C.cGoldB + "You are currently incognito.");
		UtilPlayer.message(player, " ");
	}

	protected IncognitoClient addPlayer(UUID uuid)
	{
		return new IncognitoClient();
	}

	public IncognitoRepository getRepository()
	{
		return _repository;
	}
	
	@Override
	public String getQuery(int accountId, String uuid, String name) 
	{
		return "SELECT * FROM incognitoStaff WHERE accountId = " + accountId + ";";
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		while (resultSet.next())
		{
			Get(uuid).Status = resultSet.getInt("status") == 1;
		}
	}

	public PreferencesManager getPreferences()
	{
		return _preferencesManager;
	}

	public void setPreferencesManager(PreferencesManager preferencesManager)
	{
		_preferencesManager = preferencesManager;
	}
}