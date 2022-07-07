package mineplex.core.stats;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniClientPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTasks;
import mineplex.core.leaderboard.LeaderboardManager;
import mineplex.core.stats.command.GiveStatCommand;
import mineplex.core.stats.command.SetLevelCommand;
import mineplex.core.stats.command.TimeCommand;
import mineplex.core.stats.event.PlayerStatsLoadedEvent;
import mineplex.core.stats.event.StatChangeEvent;
import mineplex.core.thread.ThreadPool;
import mineplex.core.updater.UpdateType;
import mineplex.core.utils.UtilScheduler;

/**
 * This manager handles player statistics
 */
public class StatsManager extends MiniClientPlugin<PlayerStats>
{
	public enum Perm implements Permission
	{
		GIVE_STAT_COMMAND,
		TIME_COMMAND,
		SET_LEVEL_COMMAND,
	}
	
	private static final Object STATS_LOCK = new Object();

	private final CoreClientManager _coreClientManager;
	private final StatsRepository _repository;

	private final Map<String, Integer> _stats = new HashMap<>();
	private final Map<CoreClient, Map<String, Long>> _statUploadQueue = new HashMap<>();
	
	private final Set<UUID> _loading = Collections.synchronizedSet(new HashSet<>());

	public StatsManager(JavaPlugin plugin, CoreClientManager clientManager)
	{
		super("Stats Manager", plugin);
		
		_repository = new StatsRepository();
		_coreClientManager = clientManager;
		
		new LeaderboardManager(this);

		UtilScheduler.runAsyncEvery(UpdateType.SEC, () ->
		{
			save(_statUploadQueue, map ->
			{
				_repository.insertStats(map);
			}, "increment");
		});

		for (Stat stat : _repository.retrieveStats())
		{
			_stats.put(stat.getName(), stat.getId());
		}
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.GIVE_STAT_COMMAND, true, true);
		PermissionGroup.MOD.setPermission(Perm.TIME_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.SET_LEVEL_COMMAND, true, true);
	}

	/**
	 * Gets offline stats for the specified player name
	 *
	 * @return A Future to listen to, if you are already off the main thread
	 */
	public Future<PlayerStats> getOfflinePlayerStats(String playerName)
	{
		return getOfflinePlayerStats(playerName, null);
	}

	/**
	 * Gets offline stats for the specified player name
	 *
	 * @param action The action to perform with the fetched PlayerStats. This action will be performed on the main thread. Can be null
	 * @return A Future to listen to, should you already be off the main thread
	 */
	public Future<PlayerStats> getOfflinePlayerStats(String playerName, Consumer<PlayerStats> action)
	{
		return ThreadPool.ASYNC.submit(() ->
		{
			PlayerStats stats = _repository.loadOfflinePlayerStats(playerName);
			UtilTasks.onMainThread(action).accept(stats);
			return stats;
		});
	}
	
	public void loadStatId(String statName, Consumer<Integer> idConsumer)
	{
		registerNewStat(statName, () ->
		{
			final int statId = _stats.get(statName);
			
			runSync(() -> idConsumer.accept(statId));
		});
	}

	/**
	 * Increments a stat for the given player by the specified amount
	 *
	 * @param value The value, must be greater or equal to zero
	 */
	public void incrementStat(Player player, String statName, long value)
	{
		if (value < 0)
			return;
		
		CoreClient client = _coreClientManager.Get(player);
		PlayerStats snapshot = Get(player);
		
		if (snapshot.isTemporary())
		{
			return;
		}
		
		long oldValue = snapshot.getStat(statName);
		long newValue = snapshot.addStat(statName, value);

		UtilServer.getServer().getPluginManager().callEvent(new StatChangeEvent(player, statName, oldValue, newValue));
		registerNewStat(statName, () -> addToQueue(statName, client, value));
	}

	/**
	 * Increments a stat for the given account ID of an <b>offline player</b> by the specified amount
	 */
	public void incrementStat(final int accountId, final String statName, final long value)
	{
		registerNewStat(statName, () ->
		{
			Map<Integer, Long> stats = new HashMap<>();
			stats.put(_stats.get(statName), value);
			
			_repository.insertStats(accountId, stats);
		});
	}

	private void addToQueue(String statName, CoreClient client, long value)
	{
		if (client.getAccountId() == -1)
		{
			System.out.println(String.format("Error: Tried to add %s/%s to increment queue with -1 account id", client.getName(), client.getUniqueId()));
			return;
		}

		synchronized (STATS_LOCK)
		{
			_statUploadQueue
					.computeIfAbsent(client, key -> new HashMap<>())
					.merge(statName, value, Long::sum);
		}
	}

	protected void save(Map<CoreClient, Map<String, Long>> statsMap, Consumer<Map<Integer, Map<Integer, Long>>> action, String type)
	{
		if (statsMap.isEmpty())
			return;

		Map<Integer, Map<Integer, Long>> uploadQueue = new HashMap<>();

		try
		{
			synchronized (STATS_LOCK)
			{
				statsMap.entrySet().removeIf(entry ->
				{
					CoreClient client = entry.getKey();
					if (Bukkit.getPlayer(client.getUniqueId()) != null)
						return false;

					Map<Integer, Long> uploadStats = uploadQueue.computeIfAbsent(client.getAccountId(), key -> new HashMap<>());

					entry.getValue().entrySet()
							.stream()
							.sorted(Map.Entry.comparingByKey())
							.forEach(ent ->
					{
						// Sanity check
						if (_stats.containsKey(ent.getKey()))
						{
							uploadStats.merge(_stats.get(ent.getKey()), ent.getValue(), Long::sum);
							System.out.println(String.format("Saving stat '%s' for '%s', value '%s', type '%s'", ent.getKey(), client.getName() == null ? client.getUniqueId().toString() : client.getName(), uploadStats.get(_stats.get(ent.getKey())), type));
						}
					});

					return true;
				});
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		finally
		{
			action.accept(uploadQueue);
		}
	}

	private void registerNewStat(final String statName, final Runnable callback)
	{
		runAsync(() ->
		{
			synchronized (STATS_LOCK)
			{
				if (_stats.containsKey(statName))
				{
					if (callback != null) callback.run();
					return;
				}
			}
			
			_repository.registerNewStat(statName, () ->
			{
				synchronized(STATS_LOCK)
				{
					_stats.clear();
					
					for (Stat stat : _repository.retrieveStats())
					{
						_stats.put(stat.getName(), stat.getId());
					}

					if (callback != null) callback.run();
				}
			});
		});
	}

	@EventHandler
	private void onPlayerJoin(PlayerJoinEvent event)
	{
		final UUID uuid = event.getPlayer().getUniqueId();
		final int accountId = _coreClientManager.Get(event.getPlayer()).getAccountId();
		UtilPlayer.message(event.getPlayer(), F.main(getName(), "Loading your stats..."));
		runSyncLater(() ->
		{
			_repository.loadStats(accountId, data ->
			{
				PlayerStats stats = new PlayerStats(false);
				
				data.forEach(stats::setStat);
				
				if (_loading.remove(uuid))
				{
					Set(uuid, stats);
					UtilPlayer.message(event.getPlayer(), F.main(getName(), "Your stats have been loaded!"));
					UtilServer.CallEvent(new PlayerStatsLoadedEvent(event.getPlayer()));
				}
			});
		}, 5 * 20); //Load 5 seconds later
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onLogin(AsyncPlayerPreLoginEvent event)
	{
		_loading.add(event.getUniqueId());
	}

	@Override
	public void addCommands()
	{
		addCommand(new TimeCommand(this));
		addCommand(new GiveStatCommand(this));
		addCommand(new SetLevelCommand(this));
	}

	@Override
	protected PlayerStats addPlayer(UUID uuid)
	{
		return new PlayerStats(false);
	}
	
	@Override
	public PlayerStats Get(UUID uuid)
	{
		if (_loading.contains(uuid))
		{
			return new PlayerStats(true);
		}
		return super.Get(uuid);
	}
	
	@Override
	public void saveData(String name, UUID uuid, int accountId)
	{
		_loading.remove(uuid);
	}
	
	public CoreClientManager getClientManager()
	{
		return _coreClientManager;
	}
}