package mineplex.game.clans.clans.antiafk;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;

@ReflectivelyCreateMiniPlugin
public class AfkManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		BYPASS_AFK_KICK
	}
	
	private static final long AFK_TIME_ALLOWED = UtilTime.convert(10, TimeUnit.MINUTES, TimeUnit.MILLISECONDS);
	
	private AfkManager()
	{
		super("AFK Manager");
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.BYPASS_AFK_KICK, true, true);
		PermissionGroup.YOUTUBE.setPermission(Perm.BYPASS_AFK_KICK, false, true);
		PermissionGroup.TWITCH.setPermission(Perm.BYPASS_AFK_KICK, false, true);
	}
	
	private boolean matchYawPitch(Location loc, Location loc2)
	{
		if (loc.getYaw() != loc2.getYaw())
		{
			return false;
		}
		if (loc.getPitch() != loc2.getPitch())
		{
			return false;
		}
		return true;
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent event)
	{
		if (UtilMath.offsetSquared(event.getFrom(), event.getTo()) > 0)
		{
			if (!matchYawPitch(event.getFrom(), event.getTo()))
			{
				UtilEnt.SetMetadata(event.getPlayer(), "AFK_MOVE", System.currentTimeMillis());
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		UtilEnt.SetMetadata(event.getPlayer(), "AFK_MOVE", System.currentTimeMillis());
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC_05)
		{
			return;
		}
		Bukkit.getOnlinePlayers().forEach(player ->
		{
			Long lastMove = UtilEnt.GetMetadata(player, "AFK_MOVE");
			if (lastMove != null)
			{
				if (UtilTime.elapsed(lastMove, AFK_TIME_ALLOWED) && !ClansManager.getInstance().getClientManager().Get(player).hasPermission(Perm.BYPASS_AFK_KICK))
				{
					UtilPlayer.message(player, F.main(getName(), "You have been sent to the hub for idling too long!"));
					Portal.getInstance().sendPlayerToGenericServer(player, GenericServer.CLANS_HUB, Intent.KICK);
				}
			}
		});
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event)
	{
		runSyncLater(() -> event.getEntity().spigot().respawn(), 10);
	}
}