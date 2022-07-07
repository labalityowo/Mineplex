package mineplex.game.nano.status;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.server.ServerListPingEvent;

import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.game.status.GameInfo;
import mineplex.core.game.status.GameInfo.GameDisplayStatus;
import mineplex.core.game.status.GameInfo.GameJoinStatus;
import mineplex.game.nano.GameManager;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.Game;

@ReflectivelyCreateMiniPlugin
public class GameStatusManager extends GameManager
{

	public enum Perm implements Permission
	{
		JOIN_FULL,
		JOIN_FULL_STAFF,
	}

	private GameStatusManager()
	{
		super("Game Status");

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.ULTRA.setPermission(Perm.JOIN_FULL, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.JOIN_FULL_STAFF, true, true);
	}

	@EventHandler
	public void motdPing(ServerListPingEvent event)
	{
		if (UtilServer.isTestServer(false))
		{
			event.setMotd(C.cAqua + "Private Mineplex Test Server");
			return;
		}

		Game game = _manager.getGame();
		String gameName = null, map = null;
		GameDisplayStatus status = GameDisplayStatus.WAITING;

		if (game != null)
		{
			status = GameDisplayStatus.ALWAYS_OPEN;
			gameName = game.getGameType().getName();

			if (game.getMineplexWorld() != null)
			{
				map = game.getMineplexWorld().getMapName();
			}
		}

		GameJoinStatus joinable = getJoinable();

		if (joinable == GameJoinStatus.CLOSED)
		{
			status = GameDisplayStatus.IN_PROGRESS;
		}

		event.setMotd(new GameInfo(NanoManager.getGameDisplay(), gameName, map, -1, null, null, status, joinable).toString());
	}

	@EventHandler
	public void playerLogin(PlayerLoginEvent event)
	{
		Player player = event.getPlayer();
		CoreClient client = _manager.getClientManager().Get(player);
		GameJoinStatus joinable = getJoinable();

		if (joinable != GameJoinStatus.OPEN)
		{
			if (client.hasPermission(Perm.JOIN_FULL_STAFF))
			{
				event.allow();
				return;
			}

			boolean canOverflow = client.hasPermission(Perm.JOIN_FULL) || _manager.getDonationManager().Get(player).ownsUnknownSalesPackage(_manager.getServerGroup().getServerType() + " ULTRA");

			if (canOverflow)
			{
				if (joinable == GameJoinStatus.RANKS_ONLY)
				{
					event.allow();
				}
				else
				{
					event.disallow(Result.KICK_OTHER, C.Bold + "Server has reached max capacity for gameplay purposes.");
				}
			}
			else
			{
				if (joinable == GameJoinStatus.RANKS_ONLY)
				{
					event.disallow(Result.KICK_OTHER, C.Bold + "Server has reached max capacity for gameplay purposes.");
				}
				else
				{
					event.disallow(Result.KICK_OTHER, C.Bold + "Server Full > Purchase Ultra at www.mineplex.com/shop");
				}
			}
		}
	}

	private GameJoinStatus getJoinable()
	{
		if (Bukkit.getOnlinePlayers().size() >= Bukkit.getServer().getMaxPlayers())
		{
			if ((double) Bukkit.getServer().getOnlinePlayers().size() / Bukkit.getMaxPlayers() > 1.2)
			{
				return GameJoinStatus.CLOSED;
			}

			return GameJoinStatus.RANKS_ONLY;
		}

		return GameJoinStatus.OPEN;
	}
}
