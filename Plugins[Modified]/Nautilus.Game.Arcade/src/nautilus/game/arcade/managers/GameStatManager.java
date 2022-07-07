package nautilus.game.arcade.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.stats.StatTracker;

public class GameStatManager implements Listener
{
	public enum Perm implements Permission
	{
		STAT_BOOST_COMMAND,
	}

	final ArcadeManager Manager;
	
	private final Map<UUID, Long> _joinTimes = new HashMap<>();

	public GameStatManager(ArcadeManager manager)
	{
		Manager = manager;

		Manager.registerEvents(this);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.LT.setPermission(Perm.STAT_BOOST_COMMAND, true, true);
	}

	@EventHandler
	public void statEnableDisable(GameStateChangeEvent event)
	{
		if (!Manager.IsRewardStats() || event.GetState() != GameState.Live)
		{
			return;
		}

		int requirement = 2;

		event.GetGame().CanAddStats = event.GetGame().GetPlayers(true).size() >= requirement;

		if (!event.GetGame().CanAddStats)
		{
			event.GetGame().Announce(C.Bold + "Stats/Achievements Disabled. Requires " + requirement + " Players.", event.GetGame().PlaySoundGameStart);
		}
	}
			
	@EventHandler
	public void statRecord(GameStateChangeEvent event)
	{
		if (!Manager.IsRewardStats() || event.GetState() != GameState.Dead)
		{
			return;
		}

		event.GetGame().GetStats().forEach((player, statMap) ->
		{
			statMap.forEach((stat, value) ->
			{
				if (value > 0)
				{
					Manager.GetStatsManager().incrementStat(player, stat, value);
				}
			});
		});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void statRecord(PlayerQuitEvent event)
	{
		Game game = Manager.GetGame();

		if (game == null || !game.IsLive())
		{
			return;
		}

		Player player = event.getPlayer();
		Map<String, Integer> stats = game.GetStats().remove(player);

		if (stats == null)
		{
			return;
		}

		stats.forEach((stat, value) ->
		{
			if (value > 0)
			{
				Manager.GetStatsManager().incrementStat(player, stat, value);
			}
		});
	}
	
	@EventHandler
	public void statTrackCleanup(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.End)
		{
			for (StatTracker tracker : event.GetGame().getStatTrackers())
			{
				HandlerList.unregisterAll(tracker);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void playTimeStatJoin(PlayerJoinEvent event)
	{
		_joinTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
	}

	@EventHandler
	public void playTimeStatQuit(PlayerQuitEvent event)
	{
		Long joinTime = _joinTimes.remove(event.getPlayer().getUniqueId());

		if (joinTime != null)
		{
			int timeInGame = (int) ((System.currentTimeMillis() - joinTime) / 1000);
			
			Manager.GetStatsManager().incrementStat(event.getPlayer(), "Global.TimeInGame", timeInGame);
		}
	}
}