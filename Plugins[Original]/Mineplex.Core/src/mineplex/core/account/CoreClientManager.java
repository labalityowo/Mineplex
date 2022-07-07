package mineplex.core.account;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

import mineplex.cache.player.PlayerCache;
import mineplex.cache.player.PlayerInfo;
import mineplex.core.MiniPlugin;
import mineplex.core.account.command.RanksCommand;
import mineplex.core.account.event.ClientUnloadEvent;
import mineplex.core.account.event.ClientWebResponseEvent;
import mineplex.core.account.event.OnlinePrimaryGroupUpdateEvent;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.account.permissions.PermissionGroupHelper;
import mineplex.core.account.redis.AddPermissionGroup;
import mineplex.core.account.redis.AddPermissionGroupHandler;
import mineplex.core.account.redis.ClearGroups;
import mineplex.core.account.redis.ClearGroupsHandler;
import mineplex.core.account.redis.PrimaryGroupUpdate;
import mineplex.core.account.redis.PrimaryGroupUpdateHandler;
import mineplex.core.account.redis.RemovePermissionGroup;
import mineplex.core.account.redis.RemovePermissionGroupHandler;
import mineplex.core.account.repository.AccountRepository;
import mineplex.core.account.repository.token.ClientToken;
import mineplex.core.common.Pair;
import mineplex.core.common.timing.TimingManager;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UUIDFetcher;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTasks;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilGameProfile;
import mineplex.core.utils.UtilScheduler;
import mineplex.serverdata.commands.ServerCommandManager;

public class CoreClientManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		JOIN_FULL,
		RANK_COMMAND,
		ADD_RANK_COMMAND,
		RANK_INFO_COMMAND,
		LIST_RANKS_COMMAND,
		REMOVE_RANK_COMMAND,
		RESET_PLAYER_COMMAND,
		SET_RANK_COMMAND,
	}

	private static final Map<String, Object> CLIENT_LOGIN_LOCKS = new ConcurrentHashMap<>();
	
	private static final Pattern VALID_USERNAME = Pattern.compile("[a-zA-Z0-9_]{1,16}");

	private JavaPlugin _plugin;
	private AccountRepository _repository;
	private Map<UUID, CoreClient> _clientList = new HashMap<>();
	private Set<UUID> _duplicateLoginGlitchPreventionList = new HashSet<>();

	private List<ILoginProcessor> _loginProcessors = new ArrayList<>();

	private final Object _clientLock = new Object();

	private static AtomicInteger _clientsConnecting = new AtomicInteger(0);
	private static AtomicInteger _clientsProcessing = new AtomicInteger(0);

	private final Set<UUID> _reservedSlots = Sets.newConcurrentHashSet();
	
	private final Cache<UUID, Integer> _loginCounter = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build();

	public CoreClientManager(JavaPlugin plugin)
	{
		super("Client Manager", plugin);

		_plugin = plugin;
		_repository = new AccountRepository();

		UtilScheduler.runEvery(UpdateType.TICK, this::checkForIllegalAccounts);
		
		ServerCommandManager.getInstance().registerCommandType(AddPermissionGroup.class, new AddPermissionGroupHandler(this));
		ServerCommandManager.getInstance().registerCommandType(ClearGroups.class, new ClearGroupsHandler(this));
		ServerCommandManager.getInstance().registerCommandType(PrimaryGroupUpdate.class, new PrimaryGroupUpdateHandler(this));
		ServerCommandManager.getInstance().registerCommandType(RemovePermissionGroup.class, new RemovePermissionGroupHandler(this));
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ULTRA.setPermission(Perm.JOIN_FULL, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.RANK_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.ADD_RANK_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.RANK_INFO_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.LIST_RANKS_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.REMOVE_RANK_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.RESET_PLAYER_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.SET_RANK_COMMAND, true, true);
	}

	private void checkForIllegalAccounts()
	{
		// Use getOnlinePlayers because in the future, I might change UtilServer.getPlayers to account for vanish
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (Get(player).getAccountId() == -1)
			{
				// ew ew getim outta here
				player.kickPlayer("There was a problem logging you in");
			}
		}
	}

	public AccountRepository getRepository()
	{
		return _repository;
	}
	
	@Override
	public void addCommands()
	{
		addCommand(new RanksCommand(this));
	}

	public CoreClient Add(String name, UUID uuid)
	{
		CoreClient newClient = new CoreClient(name, uuid);

		CoreClient oldClient;

		synchronized (_clientLock)
		{
			oldClient = _clientList.put(uuid, newClient);
		}

		return newClient;
	}

	public void Del(String name, UUID uuid, int accountId)
	{
		synchronized (_clientLock)
		{
			_clientList.remove(uuid);
		}

		// rawr added account id for custom data - william
		_plugin.getServer().getPluginManager().callEvent(new ClientUnloadEvent(name, uuid, accountId));
	}

	@Deprecated
	public CoreClient Get(String name)
	{
		Player p = Bukkit.getPlayerExact(name);
		return p != null ? Get(p.getUniqueId()) : null;
	}

	public CoreClient Get(UUID uuid)
	{
		synchronized (_clientLock)
		{
			CoreClient client = _clientList.get(uuid);

			if (client == null)
			{
				Player player = Bukkit.getPlayer(uuid);
				if (player != null)
				{
					client = new CoreClient(player.getName(), uuid);
				}
				else
				{
					client = new CoreClient(null, uuid);
				}
			}

			return client;
		}
	}

	public CoreClient Get(Player player)
	{
		return Get(player.getUniqueId());
	}

	public int getPlayerCountIncludingConnecting()
	{
		return Bukkit.getOnlinePlayers().size() + Math.max(0, _clientsConnecting.get());
	}

	/**
	 * Get the database account id for a player. Requires the player is online
	 *
	 * @param player
	 * @return
	 */
	public int getAccountId(Player player)
	{
		return Get(player).getAccountId();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void AsyncLogin(AsyncPlayerPreLoginEvent event)
	{
		Integer newValueCache = _loginCounter.getIfPresent(event.getUniqueId());
		if (newValueCache == null)
		{
			newValueCache = 0;
		}
		_loginCounter.put(event.getUniqueId(), newValueCache + 1);
		System.out.println("CLIENT LOGIN TOTALS IN PAST 5 SECONDS: " + _loginCounter.asMap().values().stream().reduce(0, Integer::sum));
		
		try
		{
			_clientsConnecting.incrementAndGet();
			while (_clientsProcessing.get() >= 5)
			{
				try
				{
					Thread.sleep(25);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}

			try
			{
				_clientsProcessing.incrementAndGet();

				if (!LoadClient(Add(event.getName(), event.getUniqueId()), event.getUniqueId(), event.getAddress().getHostAddress()))
				{
					event.disallow(Result.KICK_OTHER, "There was a problem logging you in.");
				}
			}
			catch (Exception exception)
			{
				event.disallow(Result.KICK_OTHER, "Error retrieving information from web, please retry in a minute.");
				exception.printStackTrace();
			}
			finally
			{
				_clientsProcessing.decrementAndGet();
			}
		}
		finally
		{
			_clientsConnecting.decrementAndGet();
		}
	}

	public void loadAccountIdFromUUID(UUID uuid, Callback<Integer> callback)
	{
		_repository.getAccountId(uuid, callback);
	}

	public void getOrLoadClient(String playerName, Consumer<CoreClient> loadedClient)
	{
		CoreClient client = Get(playerName);
		if (client != null)
		{
			loadedClient.accept(client);
			return;
		}
		loadClientByName(playerName, loadedClient);
	}

	public void loadClientByName(final String playerName, final Runnable runnable)
	{
		loadClientByName(playerName, client -> runnable.run());
	}

	public void loadClientByUUID(UUID uuid, Consumer<CoreClient> loadedClient)
	{
		runAsync(() ->
		{
			AtomicReference<CoreClient> loaded = new AtomicReference<>();
			try
			{
				Gson gson = new Gson();

				String response = _repository.getClientByUUID(uuid);

				ClientToken token = gson.fromJson(response, ClientToken.class);
				if (token.Name == null || token.Rank == null)
				{
					loaded.set(null);
					return;
				}

				CoreClient client = Add(token.Name, uuid);
				Pair<Integer, Pair<PermissionGroup, Set<PermissionGroup>>> result = _repository.login(_loginProcessors, uuid, client.getName());
				
				client.setAccountId(result.getLeft());
				if (result.getRight().getLeft() == null)
				{
					PermissionGroup newGroup = PermissionGroupHelper.getGroupFromLegacy(token.Rank);
					client.setPrimaryGroup(newGroup);
					_repository.setPrimaryGroup(client.getAccountId(), newGroup, null);
				}
				else
				{
					client.setPrimaryGroup(result.getRight().getLeft());
				}
				for (PermissionGroup group : result.getRight().getRight())
				{
					client.addAdditionalGroup(group);
				}

				Bukkit.getServer().getPluginManager().callEvent(new ClientWebResponseEvent(response, uuid));

				if (client.getAccountId() > 0)
				{
					PlayerInfo playerInfo = PlayerCache.getInstance().getPlayer(uuid);

					if (playerInfo != null)
					{
						PlayerCache.getInstance().updateAccountId(uuid, client.getAccountId());
					}
				}

				loaded.set(client);
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
			finally
			{
				UtilTasks.onMainThread(() -> loadedClient.accept(loaded.get())).run();
			}
		});
	}

	public void loadClientByName(String playerName, Consumer<CoreClient> loadedClient)
	{
		if (!VALID_USERNAME.matcher(playerName).find())
		{
			return;
		}
		
		runAsync(() ->
		{
			AtomicReference<CoreClient> loaded = new AtomicReference<>();
			try
			{
				ClientToken token = null;
				Gson gson = new Gson();

				// Fails if not in DB and if duplicate.
				UUID uuid = loadUUIDFromDB(playerName);

				if (uuid == null)
				{
					uuid = UtilGameProfile.getProfileByName(playerName, false, profile -> {}).get().getId();
				}

				String response = "";

				if (uuid == null)
				{
					response = _repository.getClientByName(playerName);
				}
				else
				{
					response = _repository.getClientByUUID(uuid);
				}

				token = gson.fromJson(response, ClientToken.class);

				CoreClient client = Add(playerName, uuid);
				Pair<Integer, Pair<PermissionGroup, Set<PermissionGroup>>> result = _repository.login(_loginProcessors, uuid, client.getName());
				
				client.setAccountId(result.getLeft());
				if (result.getRight().getLeft() == null)
				{
					PermissionGroup newGroup = PermissionGroupHelper.getGroupFromLegacy(token.Rank);
					client.setPrimaryGroup(newGroup);
					_repository.setPrimaryGroup(client.getAccountId(), newGroup, null);
				}
				else
				{
					client.setPrimaryGroup(result.getRight().getLeft());
				}
				for (PermissionGroup group : result.getRight().getRight())
				{
					client.addAdditionalGroup(group);
				}

				// JSON sql response
				Bukkit.getServer().getPluginManager().callEvent(new ClientWebResponseEvent(response, uuid));

				if (client.getAccountId() > 0)
				{
					PlayerCache.getInstance().updateAccountId(uuid, client.getAccountId());
				}

				loaded.set(client);
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
			finally
			{
				UtilTasks.onMainThread(() -> loadedClient.accept(loaded.get())).run();
			}
		});
	}

	public void loadClientByNameSync(final String playerName, final Runnable runnable)
	{
		if (!VALID_USERNAME.matcher(playerName).find())
		{
			return;
		}
		
		try
		{
			ClientToken token = null;
			Gson gson = new Gson();

			// Fails if not in DB and if duplicate.
			UUID uuid = loadUUIDFromDB(playerName);

			if (uuid == null)
			{
				try
				{
					uuid = UUIDFetcher.getUUIDOf(playerName);
				}
				catch (Exception exception)
				{
					System.out.println("Error fetching uuid from mojang : " + exception.getMessage());
				}
			}

			String response = "";

			if (uuid == null)
			{
				response = _repository.getClientByName(playerName);
			}
			else
			{
				response = _repository.getClientByUUID(uuid);
			}

			token = gson.fromJson(response, ClientToken.class);

			CoreClient client = Add(playerName, uuid);
			Pair<Integer, Pair<PermissionGroup, Set<PermissionGroup>>> result = _repository.login(_loginProcessors, uuid, client.getName());
			
			client.setAccountId(result.getLeft());
			if (result.getRight().getLeft() == null)
			{
				PermissionGroup newGroup = PermissionGroupHelper.getGroupFromLegacy(token.Rank);
				client.setPrimaryGroup(newGroup);
				_repository.setPrimaryGroup(client.getAccountId(), newGroup, null);
			}
			else
			{
				client.setPrimaryGroup(result.getRight().getLeft());
			}
			for (PermissionGroup group : result.getRight().getRight())
			{
				client.addAdditionalGroup(group);
			}

			// JSON sql response
			Bukkit.getServer().getPluginManager().callEvent(new ClientWebResponseEvent(response, uuid));

			if (client.getAccountId() > 0)
			{
				PlayerCache.getInstance().updateAccountId(uuid, client.getAccountId());
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		finally
		{
			Bukkit.getServer().getScheduler().runTask(getPlugin(), new Runnable()
			{
				public void run()
				{
					if (runnable != null)
						runnable.run();
				}
			});
		}
	}

	public boolean LoadClient(final CoreClient client, final UUID uuid, String ipAddress)
	{
		TimingManager.start(client.getName() + " LoadClient Total.");
		long timeStart = System.currentTimeMillis();

		CLIENT_LOGIN_LOCKS.put(client.getName(), new Object());
		Gson gson = new Gson();

		runAsync(() ->
		{
			try
			{
				Pair<Integer, Pair<PermissionGroup, Set<PermissionGroup>>> result = _repository.login(_loginProcessors, uuid, client.getName());
				client.setAccountId(result.getLeft());
				if (result.getRight().getLeft() == null)
				{
					client.setPrimaryGroup(null);
				}
				else
				{
					client.setPrimaryGroup(result.getRight().getLeft());
				}
				for (PermissionGroup group : result.getRight().getRight())
				{
					client.addAdditionalGroup(group);
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			CLIENT_LOGIN_LOCKS.remove(client.getName());
		});

		TimingManager.start(client.getName() + " GetClient.");
		String response = _repository.GetClient(client.getName(), uuid, ipAddress);
		TimingManager.stop(client.getName() + " GetClient.");

		TimingManager.start(client.getName() + " While Loop.");
		while (CLIENT_LOGIN_LOCKS.containsKey(client.getName()) && System.currentTimeMillis() - timeStart < 15000)
		{
			try
			{
				Thread.sleep(2);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		TimingManager.stop(client.getName() + " While Loop.");

		if (CLIENT_LOGIN_LOCKS.containsKey(client.getName()))
		{
			System.out.println("MYSQL TOO LONG TO LOGIN....");
		}
		
		ClientToken token = gson.fromJson(response, ClientToken.class);
		
		if (client.getRawPrimaryGroup() == null)
		{
			String mssqlRank = token.Rank;
			PermissionGroup newGroup = PermissionGroupHelper.getGroupFromLegacy(mssqlRank);
			client.setPrimaryGroup(newGroup);
			_repository.setPrimaryGroup(client.getAccountId(), newGroup, null);
		}
		
		TimingManager.start(client.getName() + " Event.");
		// JSON sql response
		Bukkit.getServer().getPluginManager().callEvent(new ClientWebResponseEvent(response, uuid));
		TimingManager.stop(client.getName() + " Event.");

		TimingManager.stop(client.getName() + " LoadClient Total.");

		if (client.getAccountId() > 0)
		{
			PlayerCache.getInstance().updateAccountId(uuid, client.getAccountId());
		}

		return !CLIENT_LOGIN_LOCKS.containsKey(client.getName());
	}
	
	public ClientToken loadOfflineClient(UUID uuid)
	{
		String client = _repository.getClientByUUID(uuid);
		return new Gson().fromJson(client, ClientToken.class);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void Login(PlayerLoginEvent event)
	{
		synchronized (_clientLock)
		{
			if (!_clientList.containsKey(event.getPlayer().getUniqueId()))
			{
				_clientList.put(event.getPlayer().getUniqueId(), new CoreClient(event.getPlayer().getName(), event.getPlayer().getUniqueId()));
			}
		}

		CoreClient client = Get(event.getPlayer().getUniqueId());

		if (client == null || client.getRawPrimaryGroup() == null)
		{
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "There was an error logging you in.  Please reconnect.");
			return;
		}

		client.SetPlayer(event.getPlayer());

		_reservedSlots.remove(event.getPlayer().getUniqueId());

		// Reserved Slot Check
		if (Bukkit.getOnlinePlayers().size() + _reservedSlots.size() >= Bukkit.getServer().getMaxPlayers())
		{
			if (client.hasPermission(Perm.JOIN_FULL))
			{
				event.allow();
				return;
			}

			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "This server is full and no longer accepts players.");
		}
	}

	public void reserveFor(UUID player)
	{
		_reservedSlots.add(player);
	}

	public void unreserve(UUID uuid)
	{
		_reservedSlots.remove(uuid);
	}

	@EventHandler
	public void Kick(PlayerKickEvent event)
	{
		if (event.getReason().contains("You logged in from another location"))
		{
			_duplicateLoginGlitchPreventionList.add(event.getPlayer().getUniqueId());
			Bukkit.getScheduler().runTask(_plugin, () ->
			{
				if (!_clientList.containsKey(event.getPlayer().getUniqueId()))
				{
					return;
				}
				Player p = _clientList.get(event.getPlayer().getUniqueId()).GetPlayer();
				p.kickPlayer("You're already logged in.");
			});
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void Quit(PlayerQuitEvent event)
	{
		// When an account is logged in to this server and the same account name logs in
		// Then it Fires events in this order  (original, new for acco unts)
		// AsyncPreLogin -> new
		// PlayerLogin -> new
		// PlayerKick -> old
		// PlayerQuit -> old
		// Then it glitches because it added new, but then removed old afterwards since its based on name as key.

		if (!_duplicateLoginGlitchPreventionList.contains(event.getPlayer().getUniqueId()))
		{
			Del(event.getPlayer().getName(), event.getPlayer().getUniqueId(), _clientList.get(event.getPlayer().getUniqueId()).getAccountId());
			_duplicateLoginGlitchPreventionList.remove(event.getPlayer().getUniqueId());
		}
	}
	
	public void setPrimaryGroup(Player player, final PermissionGroup group, Runnable after)
	{
		setPrimaryGroup(Get(player).getAccountId(), group, () ->
		{
			PermissionGroup old = Get(player).getPrimaryGroup();
			Get(player).setPrimaryGroup(group);
			UtilServer.CallEvent(new OnlinePrimaryGroupUpdateEvent(player, old, group));
			if (after != null)
			{
				after.run();
			}
		});
	}
	
	public void setPrimaryGroup(final int accountId, final PermissionGroup group, Runnable after)
	{
		_repository.setPrimaryGroup(accountId, group, after);
	}

	public void addAdditionalGroup(final int accountId, final PermissionGroup group, Consumer<Boolean> successCallback)
	{
		_repository.addAdditionalGroup(accountId, group, success ->
		{
			if (successCallback != null)
			{
				successCallback.accept(success);
			}
			if (!success)
			{
				System.out.println("Error adding additional group " + group + " to account " + accountId + "!");
			}
		});
	}
	
	public void removeAdditionalGroup(final int accountId, final PermissionGroup group, Consumer<Boolean> successCallback)
	{
		_repository.removeAdditionalGroup(accountId, group, success ->
		{
			if (successCallback != null)
			{
				successCallback.accept(success);
			}
			if (!success)
			{
				System.out.println("Error removing additional group " + group + " from account " + accountId + "!");
			}
		});
	}

	public void clearGroups(final int accountId, Consumer<Boolean> successCallback)
	{
		_repository.clearGroups(accountId, success ->
		{
			if (successCallback != null)
			{
				successCallback.accept(success);
			}
			if (!success)
			{
				System.out.println("Error clearing groups from account " + accountId + "!");
			}
		});
	}
	
	public void fetchGroups(final int accountId, BiConsumer<PermissionGroup, Set<PermissionGroup>> resultCallback, Runnable onError)
	{
		_repository.fetchGroups(accountId, (primaryGroup, additionalGroups) ->
		{
			if (primaryGroup != null)
			{
				UtilServer.runSync(() -> resultCallback.accept(primaryGroup, additionalGroups));
				return;
			}

			UUID uuid;
			if ((uuid = _repository.getClientUUID(accountId)) != null)
			{
				try
				{
					PermissionGroup legacyPrimary = CompletableFuture.supplyAsync(() ->
					{
						String legacy = loadOfflineClient(uuid).Rank;
						PermissionGroup defaultGroup = PermissionGroup.PLAYER;
						PermissionGroup loaded = PermissionGroupHelper.getGroupFromLegacy(legacy);

						return loaded == null ? defaultGroup : loaded;
					}).get(5, TimeUnit.SECONDS);

					UtilServer.runSync(() -> resultCallback.accept(legacyPrimary, additionalGroups));
					return;

				} catch (Exception e)
				{
					System.out.println("Error fetching groups of account " + accountId + "!");
					e.printStackTrace();
				}
			}

			UtilServer.runSync(() -> resultCallback.accept(PermissionGroup.PLAYER, additionalGroups));
		}, () ->
		{
			if (onError != null)
			{
				onError.run();
			}
			System.out.println("Error fetching groups of account " + accountId + "!");
		});
	}

	public void checkPlayerNameExact(final Callback<Boolean> callback, final String playerName)
	{
		_repository.matchPlayerName(matches ->
		{
			for (String match : matches)
			{
				if (match.equalsIgnoreCase(playerName))
				{
					callback.run(true);
				}
			}

			callback.run(false);
		}, playerName);
	}

	public void checkPlayerName(final Player caller, final String playerName, final Callback<String> callback)
	{
		_repository.matchPlayerName(matches ->
		{
			String tempName = null;

			for (String match : matches)
			{
				if (match.equalsIgnoreCase(playerName))
				{
					tempName = match;
					break;
				}
			}

			final String matchedName = tempName;

			if (matchedName != null)
			{
				matches.removeIf(s -> !s.equalsIgnoreCase(playerName));
			}

			UtilPlayer.searchOffline(matches, target ->
			{
				if (target == null)
				{
					callback.run(matchedName);
					return;
				}

				callback.run(matchedName);
			}, caller, playerName, true);
		}, playerName);
	}

	// DONT USE THIS IN PRODUCTION...its for enjin listener -someone you despise but definitely not me (defek7)
	public UUID loadUUIDFromDB(String name)
	{
		return _repository.getClientUUID(name);
	}

	@EventHandler
	public void cleanGlitchedClients(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;

		synchronized (_clientLock)
		{
			for (Iterator<Entry<UUID, CoreClient>> clientIterator = _clientList.entrySet().iterator(); clientIterator.hasNext(); )
			{
				CoreClient client = clientIterator.next().getValue(); // rawr, needed this for custom data - william
				Player clientPlayer = client.GetPlayer();

				if (clientPlayer != null && !clientPlayer.isOnline())
				{
					clientIterator.remove();

					if (clientPlayer != null)
					{
						_plugin.getServer().getPluginManager().callEvent(new ClientUnloadEvent(clientPlayer.getName(), clientPlayer.getUniqueId(), client.getAccountId()));
					}
				}
			}
		}
	}

	public void addStoredProcedureLoginProcessor(ILoginProcessor processor)
	{
		_loginProcessors.add(processor);
	}

	public void loadLastLogin(String name, Consumer<Long> lastLogin)
	{
		_repository.loadLastLogin(name, lastLogin);
	}
}