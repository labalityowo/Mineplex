package mineplex.game.nano.lobby;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import mineplex.core.MiniClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.event.PlayerStateChangeEvent;
import mineplex.game.nano.lobby.AFKManager.AFKData;

@ReflectivelyCreateMiniPlugin
public class AFKManager extends MiniClientPlugin<AFKData>
{

	public enum Perm implements Permission
	{
		BYPASS_AFK_KICK
	}

	private static final long KICK_WARNING_TIME = TimeUnit.SECONDS.toMillis(60);
	private static final long KICK_TIME = TimeUnit.SECONDS.toMillis(70);

	private final NanoManager _manager;

	private AFKManager()
	{
		super("AFK");

		_manager = require(NanoManager.class);

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.MOD.setPermission(Perm.BYPASS_AFK_KICK, true, true);

		if (UtilServer.isTestServer())
		{
			PermissionGroup.QAT.setPermission(Perm.BYPASS_AFK_KICK, true, true);
		}
	}

	@Override
	protected AFKData addPlayer(UUID uuid)
	{
		return new AFKData();
	}

	@EventHandler
	public void updateAFK(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || _manager.getGame() == null)
		{
			return;
		}

		for (Player player : UtilServer.getPlayersCollection())
		{
			if (_manager.getClientManager().Get(player).hasPermission(Perm.BYPASS_AFK_KICK) || !_manager.getGame().isAlive(player))
			{
				continue;
			}

			AFKData data = Get(player);

			if (data.Last == null || data.LastMovement == 0)
			{
				data.Last = player.getLocation();
				data.LastMovement = System.currentTimeMillis();
				continue;
			}

			Location location = player.getLocation();

			if (location.getWorld().equals(data.Last.getWorld()))
			{
				if (UtilMath.offsetSquared(location, data.Last) > 0.1)
				{
					data.LastMovement = System.currentTimeMillis();
					data.Informed = false;
				}
				else if (UtilTime.elapsed(data.LastMovement, KICK_TIME))
				{
					player.sendMessage("");
					player.sendMessage(C.cGoldB + "  YOU HAVE BEEN KICKED FOR AFK!");
					player.sendMessage("");
					player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
					Portal.getInstance().sendPlayerToGenericServer(player, GenericServer.HUB, Intent.KICK);
				}
				else if (UtilTime.elapsed(data.LastMovement, KICK_WARNING_TIME) && !data.Informed)
				{
					player.sendMessage("");
					player.sendMessage(C.cGold + "  If you do not move soon you will be kicked for AFK!");
					player.sendMessage("");
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
					data.Informed = true;
				}
			}

			data.Last = location;
		}
	}

	@EventHandler
	public void playerIn(PlayerStateChangeEvent event)
	{
		if (event.isAlive())
		{
			Get(event.getPlayer()).LastMovement = System.currentTimeMillis();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void entityShootBow(EntityShootBowEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			Get((Player) event.getEntity()).LastMovement = System.currentTimeMillis();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerTeleport(PlayerTeleportEvent event)
	{
		AFKData data = Get(event.getPlayer());
		data.Last = event.getTo();
	}

	class AFKData
	{

		Location Last;
		long LastMovement;
		boolean Informed;

	}

}
