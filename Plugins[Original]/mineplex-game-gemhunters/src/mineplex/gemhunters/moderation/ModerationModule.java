package mineplex.gemhunters.moderation;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.incognito.events.IncognitoStatusChangeEvent;
import mineplex.core.teleport.event.MineplexTeleportEvent;
import mineplex.gemhunters.moderation.command.ModeratorModeCommand;
import mineplex.gemhunters.spawn.SpawnModule;
import mineplex.gemhunters.spawn.event.PlayerTeleportIntoMapEvent;

@ReflectivelyCreateMiniPlugin
public class ModerationModule extends MiniPlugin
{
	public enum Perm implements Permission
	{
		MODERATOR_MODE_COMMAND,
		MODERATOR_MODE_BYPASS,
		AUTO_OP,
	}

	private final CoreClientManager _client;
	private final IncognitoManager _incognito;
	private final SpawnModule _spawn;

	private final Set<UUID> _moderators;

	private ModerationModule()
	{
		super("Moderation");

		_client = require(CoreClientManager.class);
		_incognito = require(IncognitoManager.class);
		_spawn = require(SpawnModule.class);

		_moderators = new HashSet<>();
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.TRAINEE.setPermission(Perm.MODERATOR_MODE_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.MODERATOR_MODE_BYPASS, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.AUTO_OP, true, true);
		if (UtilServer.isTestServer())
		{
			PermissionGroup.QAM.setPermission(Perm.AUTO_OP, false, true);
		}
	}

	@Override
	public void addCommands()
	{
		addCommand(new ModeratorModeCommand(this));
	}

	@EventHandler
	public void teleport(MineplexTeleportEvent event)
	{
		Player player = event.getPlayer();

		if (isModerating(player) || isBypassing(player))
		{
			return;
		}

		enableModeratorMode(player);
	}

	@EventHandler
	public void vanish(IncognitoStatusChangeEvent event)
	{
		Player player = event.getPlayer();

		if (isBypassing(player))
		{
			return;
		}
		
		if (isModerating(player) && !event.getNewState())
		{
			disableModeratorMode(player);
		}
		else if (event.getNewState())
		{
			enableModeratorMode(player);
		}
	}

	@EventHandler
	public void mapTeleport(PlayerTeleportIntoMapEvent event)
	{
		Player player = event.getPlayer();

		if (isBypassing(player) || !_incognito.Get(player).Status)
		{
			return;
		}

		enableModeratorMode(player);
	}
	
	@EventHandler
	public void autoOp(PlayerJoinEvent event)
	{
		if (_client.Get(event.getPlayer()).hasPermission(Perm.AUTO_OP))
		{
			event.getPlayer().setOp(true);
		} else
		{
			event.getPlayer().setOp(false);
		}
	}

	public void enableModeratorMode(Player player)
	{
		player.sendMessage(F.main(_moduleName, "Enabled moderator mode."));
		player.setGameMode(GameMode.SPECTATOR);
		player.getInventory().clear();
		
		((CraftPlayer) player).getHandle().spectating = true;

		_moderators.add(player.getUniqueId());
	}

	public void disableModeratorMode(Player player)
	{
		player.sendMessage(F.main(_moduleName, "Disabled moderator mode."));
		player.setGameMode(GameMode.SURVIVAL);
		
		((CraftPlayer) player).getHandle().spectating = false;
		
		_spawn.teleportToSpawn(player);

		_moderators.remove(player.getUniqueId());
	}

	public boolean isModerating(Player player)
	{
		return _moderators.contains(player.getUniqueId());
	}

	public boolean isBypassing(Player player)
	{
		return _client.Get(player).hasPermission(Perm.MODERATOR_MODE_BYPASS);
	}
}