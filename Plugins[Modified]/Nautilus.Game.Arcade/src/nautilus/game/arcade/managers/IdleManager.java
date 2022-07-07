package nautilus.game.arcade.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import mineplex.core.command.CommandCenter;
import nautilus.game.arcade.command.ToggleAfkKickCommand;
import nautilus.game.arcade.game.GameServerConfig;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;

public class IdleManager implements Listener
{
	public enum Perm implements Permission
	{
		BYPASS_KICK,
		TOGGLE_AFK_KICK_COMMAND
	}

	private final ArcadeManager _arcadeManager;
	private final Map<UUID, Float> _yaw = new HashMap<>();
	private final Map<UUID, Long> _idle = new HashMap<>();
	private final Map<UUID, Integer> _beep = new HashMap<>();
	private final Set<UUID> _teleported = new HashSet<>();

	public IdleManager(ArcadeManager manager)
	{
		_arcadeManager = manager;
		UtilServer.RegisterEvents(this);

		CommandCenter.Instance.addCommand(new ToggleAfkKickCommand(manager));

		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.MOD.setPermission(Perm.BYPASS_KICK, true, true);

		PermissionGroup.ADMIN.setPermission(Perm.TOGGLE_AFK_KICK_COMMAND, true, true);

		if (UtilServer.isTestServer())
		{
			PermissionGroup.QA.setPermission(Perm.TOGGLE_AFK_KICK_COMMAND, true, true);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		_yaw.remove(event.getPlayer().getUniqueId());
		_idle.remove(event.getPlayer().getUniqueId());
		_beep.remove(event.getPlayer().getUniqueId());
		_teleported.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void playerTeleport(PlayerTeleportEvent event)
	{
		_teleported.add(event.getPlayer().getUniqueId());
		_arcadeManager.runSyncLater(() ->
		{
			if (event.getPlayer().isOnline())
			{
				_yaw.put(event.getPlayer().getUniqueId(), event.getPlayer().getLocation().getYaw());
				_teleported.remove(event.getPlayer().getUniqueId());
			}
		}, 5);
	}


	@EventHandler
	public void idleChat(final AsyncPlayerChatEvent event)
	{
		if (!_arcadeManager.IsPlayerKickIdle())
			return;

		_arcadeManager.runSyncLater(() ->
		{
			if (event.getPlayer().isOnline())
			{
				_idle.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
			}
		}, 1L);
	}

	@EventHandler
	public void kickIdlePlayers(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (!_arcadeManager.IsPlayerKickIdle())
			return;

		if (_arcadeManager.GetGame() == null)
			return;

		for (Player player : UtilServer.getPlayersCollection())
		{
			if (_teleported.contains(player.getUniqueId()))
				continue;

			if (!_yaw.containsKey(player.getUniqueId()) || !_idle.containsKey(player.getUniqueId()))
			{
				_yaw.put(player.getUniqueId(), player.getLocation().getYaw());
				_idle.put(player.getUniqueId(), System.currentTimeMillis());
			}

			if (_yaw.get(player.getUniqueId()) == player.getLocation().getYaw())
			{
				if (UtilTime.elapsed(_idle.get(player.getUniqueId()), _arcadeManager.GetGame().IsLive() ? 240000 : 120000))
				{
					if (!_arcadeManager.GetGame().inLobby() && !_arcadeManager.GetGame().IsAlive(player))
						continue;

					if (_arcadeManager.GetClients().Get(player).hasPermission(Perm.BYPASS_KICK))
						continue;

					//Start Beeps
					if (!_beep.containsKey(player.getUniqueId()))
					{
						_beep.put(player.getUniqueId(), 20);
					}
					//Countdown
					else
					{
						int count = _beep.get(player.getUniqueId());

						if (count == 0)
						{
							player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 10f, 1f);
							_arcadeManager.GetPortal().sendPlayerToGenericServer(player, GenericServer.HUB, Intent.KICK);
						}
						else
						{
							float scale = (float) (0.8 + (((double) count / 20d) * 1.2));
							player.playSound(player.getLocation(), Sound.NOTE_PLING, scale, scale);

							if (count % 2 == 0)
							{
								UtilPlayer.message(player, C.cGold + C.Bold + "You will be AFK removed in " + (count / 2) + " seconds...");
							}

							count--;
							_beep.put(player.getUniqueId(), count);
						}
					}
				}
			}
			else
			{
				_yaw.put(player.getUniqueId(), player.getLocation().getYaw());
				_idle.put(player.getUniqueId(), System.currentTimeMillis());
				_beep.remove(player.getUniqueId());
			}
		}
	}
}