package mineplex.game.clans.restart;

import java.util.Calendar;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.mineplex.clansqueue.common.ClansQueueMessenger;
import com.mineplex.clansqueue.common.QueueConstant;
import com.mineplex.clansqueue.common.messages.ServerOfflineMessage;

import mineplex.core.Managers;
import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.slack.SlackAPI;
import mineplex.core.slack.SlackMessage;
import mineplex.core.slack.SlackTeam;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.RestartServerEvent;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.supplydrop.SupplyDropManager;
import mineplex.game.clans.gameplay.safelog.npc.NPCManager;
import net.minecraft.server.v1_8_R3.MinecraftServer;

public class RestartManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		RESTART_COMMAND,
	}

	private static final int MAX_RESTART_TIME = 2; //Server won't auto restart after 2am
	private final LinkedList<Long> _warnings = new LinkedList<>();
	private Long _restartUnlock;
	private Long _restartTime = -1L;
	private boolean _restarting;
	
	private final String _serverName;
	private final boolean _testServer;
	
	public RestartManager(JavaPlugin plugin)
	{
		super("Restart Manager", plugin);
		
		_serverName = plugin.getConfig().getString("serverstatus.name");
		_testServer = UtilServer.isTestServer();
		
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if (inRestartZone(hour))
		{
			_restartUnlock = System.currentTimeMillis() + 1000 + UtilTime.convert(MAX_RESTART_TIME - hour, TimeUnit.HOURS, TimeUnit.MILLISECONDS);
		}
		else
		{
			_restartUnlock = System.currentTimeMillis();
		}
		
		_warnings.add(60000L);
		_warnings.add(30000L);
		_warnings.add(10000L);
		_warnings.add(5000L);
		addCommand(new RestartCommand(this));
		
		if (!_testServer)
		{
			SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#clans-server-status", new SlackMessage("Clans Uptime", "crossed_swords", _serverName + " has started up!"), true);
		}
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.CMOD.setPermission(Perm.RESTART_COMMAND, false, true);
		PermissionGroup.QAM.setPermission(Perm.RESTART_COMMAND, false, true);
		PermissionGroup.ADMIN.setPermission(Perm.RESTART_COMMAND, true, true);
	}
	
	private boolean inRestartZone(int hour)
	{
		return hour >= 0 && hour < MAX_RESTART_TIME; //12 am = 0
	}
	
	private boolean tryRestartTime()
	{
		if (!inRestartZone(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) || System.currentTimeMillis() < _restartUnlock)
		{
			return false;
		}
		if (ClansManager.getInstance().getAmplifierManager().hasActiveAmplifier())
		{
			return false;
		}
		if (ClansManager.getInstance().getNetherManager().InNether.size() > 0)
		{
			return false;
		}
		if (ClansManager.getInstance().getWorldEvent().getEvents().size() > 0)
		{
			return false;
		}
		if (ClansManager.getInstance().getWorldEvent().getRaidManager().getActiveRaids() > 0)
		{
			return false;
		}
		if (Managers.get(SupplyDropManager.class).hasActiveSupplyDrop())
		{
			return false;
		}
		
		return true;
	}
	
	private boolean tryRestartTps()
	{
		boolean restart = MinecraftServer.getServer().recentTps[0] <= 12;
		
		if (restart && !_testServer)
		{
			SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#clans-server-status", new SlackMessage("Clans Uptime", "crossed_swords", _serverName + " has scheduled an immediate restart due to low TPS!"), true);
		}
		
		return restart;
	}
	
	public boolean isRestarting()
	{
		return _restarting;
	}
	
	@Override
	public void disable()
	{
		if (!_testServer)
		{
			SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#clans-server-status", new SlackMessage("Clans Uptime", "crossed_swords", _serverName + " has shut down!"), true);
		}
	}
	
	public void restart()
	{
		ServerOfflineMessage message = new ServerOfflineMessage();
		message.ServerName = UtilServer.getServerName();
		ClansQueueMessenger.getMessenger(UtilServer.getServerName()).transmitMessage(message, QueueConstant.SERVICE_MESSENGER_IDENTIFIER);
		Bukkit.broadcastMessage(F.main("Clans", "This Clans server will be restarting in " + F.elem(UtilTime.MakeStr(120000)) + "!"));
		UtilTextMiddle.display(C.cRed + "Server Restart", C.cGray + "This server will restart in " + F.elem(UtilTime.MakeStr(120000)) + "!");
		_restartTime = System.currentTimeMillis() + 120000;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void reflectMotd(ServerListPingEvent event)
	{
		if (_restarting)
		{
			event.setMotd("Restarting soon");
		}
	}
	
	@EventHandler
	public void blockLogin(PlayerLoginEvent event)
	{
		if (_restarting)
		{
			event.disallow(Result.KICK_OTHER, C.cRed + "This server is restarting!");
		}
	}
	
	@EventHandler
	public void onRestart(RestartServerEvent event)
	{
		event.setCancelled(true);
		
		if (_restarting || _restartTime != -1)
		{
			return;
		}
		
		restart();
	}
	
	@EventHandler
	public void onShutdownCommand(ServerCommandEvent event)
	{
		String command = event.getCommand().toLowerCase().trim();
		if (command.equals("stop") || command.startsWith("stop "))
		{
			if (UtilServer.isTestServer() && command.endsWith(" -f"))
			{
				return;
			}
			
			event.setCancelled(true);
			
			if (_restarting || _restartTime != -1)
			{
				return;
			}
			
			restart();
		}
	}
	
	@EventHandler
	public void onShutdownCommand(PlayerCommandPreprocessEvent event)
	{
		String command = event.getMessage().toLowerCase().trim();
		if (command.startsWith("/") && command.length() > 1)
		{
			command = command.substring(1);
		}
		else
		{
			return;
		}
		
		if (!command.equals("stop") && !command.startsWith("stop "))
		{
			return;
		}
		
		if (!event.getPlayer().isOp() && !event.getPlayer().hasPermission("bukkit.command.stop"))
		{
			return;
		}
		
		if (UtilServer.isTestServer() && command.endsWith(" -f"))
		{
			return;
		}
		
		event.setCancelled(true);
		
		if (_restarting || _restartTime != -1)
		{
			return;
		}
		
		restart();
	}
	
	@EventHandler
	public void checkRestart(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		
		if (_restarting)
		{
			return;
		}
		
		if (_restartTime != -1)
		{
			if (!_warnings.isEmpty())
			{
				if (_restartTime - System.currentTimeMillis() <= _warnings.getFirst())
				{
					Long time = _warnings.removeFirst();
					Bukkit.broadcastMessage(F.main("Clans", "This Clans server will be restarting in " + F.elem(UtilTime.MakeStr(time)) + "!"));
				}
			}
			if (System.currentTimeMillis() >= _restartTime)
			{
				_restarting = true;
				NPCManager.getInstance().disable();
				Portal.getInstance().sendAllPlayersToGenericServer(GenericServer.CLANS_HUB, Intent.KICK);
				runSyncLater(() ->
				{
					if (!_testServer)
					{
						SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#clans-server-status", new SlackMessage("Clans Uptime", "crossed_swords", _serverName + " is now restarting!"), true);
					}
					Bukkit.shutdown();
				}, 120L);
			}
		}
		else
		{
			if (tryRestartTime() || tryRestartTps())
			{
				restart();
			}
		}
	}
}