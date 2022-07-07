package mineplex.core.leaderboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.hologram.HologramManager;
import mineplex.core.stats.StatsManager;

public class LeaderboardManager extends MiniPlugin
{

	public enum Perm implements Permission
	{
		CYCLE_LEADERBOARD_COMMAND,
	}

	private final StatsManager _statsManager;
	private final HologramManager _hologramManager;
	private final LeaderboardRepository _repo;

	private final Map<String, LeaderboardDisplay> _leaderboards;
	private final Map<Leaderboard, Runnable> _loading;

	public LeaderboardManager(StatsManager statsManager)
	{
		super("Leaderboard");

		_statsManager = statsManager;
		_hologramManager = require(HologramManager.class);

		_repo = new LeaderboardRepository();
		_leaderboards = new HashMap<>();
		_loading = new HashMap<>();

		addCommand(new CommandBase<LeaderboardManager>(this, Perm.CYCLE_LEADERBOARD_COMMAND, "cycleleaderboard")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				caller.sendMessage(F.main(getName(), "Cycling leaderboards!"));
				refreshBoards();
			}
		});

		int refreshRate = 60 * 20 * 10;
		int initialDelay = UtilMath.r(50) + 100;

		runSyncTimer(this::refreshBoards, initialDelay, refreshRate);

		runSyncTimer(() ->
		{
			Iterator<Entry<Leaderboard, Runnable>> iterator = _loading.entrySet().iterator();
			while (iterator.hasNext())
			{
				Entry<Leaderboard, Runnable> entry = iterator.next();
				boolean registered = true;
				for (int id : entry.getKey().getStatIds())
				{
					if (id == 0)
					{
						registered = false;
					}
				}
				if (registered)
				{
					entry.getValue().run();
					iterator.remove();
				}
			}
		}, 0, 20 * 2);

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.DEV.setPermission(Perm.CYCLE_LEADERBOARD_COMMAND, true, true);
	}

	private void refreshBoards()
	{
		List<Leaderboard> leaderboards = new ArrayList<>();
		_leaderboards.values().forEach(display -> leaderboards.addAll(display.getDisplayedLeaderboards()));

		_repo.loadLeaderboards(leaderboards, boards ->
		{
			for (int i = 0; i < boards.length && i < leaderboards.size(); i++)
			{
				leaderboards.get(i).update(boards[i]);
				_leaderboards.values().forEach(LeaderboardDisplay::update);
			}
		});
	}

	public void registerIfNotExists(String identifier, LeaderboardDisplay display)
	{
		if (_leaderboards.containsKey(identifier))
		{
			return;
		}

		registerLeaderboard(identifier, display);
	}

	public void registerLeaderboard(String identifier, LeaderboardDisplay display)
	{
		LeaderboardDisplay oldDisplay = _leaderboards.remove(identifier);

		if (oldDisplay != null)
		{
			oldDisplay.unregister();
		}

		List<Leaderboard> boards = display.getDisplayedLeaderboards();

		final Runnable postLoad = () ->
		{
			_leaderboards.put(identifier, display);

			for (Leaderboard board : boards)
			{
				_repo.loadLeaderboard(board, board::update);
			}

			if (display instanceof PlayerActionHook)
			{
				PlayerActionHook actionHook = (PlayerActionHook) display;
				UtilServer.getPlayersCollection().forEach(actionHook::onPlayerJoin);
			}

			display.register();
			display.update();
		};

		for (Leaderboard board : boards)
		{
			_loading.put(board, postLoad);

			for (int i = 0; i < board.getStatNames().length; i++)
			{
				final int index = i;
				_statsManager.loadStatId(board.getStatNames()[index], id -> board.setStatId(index, id));
			}
		}
	}

	public void unregisterLeaderboard(String boardIdentifier)
	{
		LeaderboardDisplay display = _leaderboards.remove(boardIdentifier);

		if (display != null)
		{
			display.unregister();
		}
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		_leaderboards.values().forEach(display ->
		{
			if (display instanceof PlayerActionHook)
			{
				((PlayerActionHook) display).onPlayerJoin(event.getPlayer());
			}
		});
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_leaderboards.values().forEach(display ->
		{
			if (display instanceof PlayerActionHook)
			{
				((PlayerActionHook) display).onPlayerQuit(event.getPlayer());
			}
		});
	}

	public HologramManager getHologramManager()
	{
		return _hologramManager;
	}
}