package mineplex.hub.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonSyntaxException;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilTime;
import mineplex.core.donation.DonationManager;
import mineplex.core.game.status.GameInfo;
import mineplex.core.game.status.GameInfo.GameJoinStatus;
import mineplex.core.newnpc.NPC;
import mineplex.core.newnpc.NewNPCManager;
import mineplex.core.newnpc.StoredNPC;
import mineplex.core.newnpc.event.NPCInteractEvent;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.core.party.event.PartySelectServerEvent;
import mineplex.core.personalServer.PersonalServerManager;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.status.ServerStatusManager;
import mineplex.core.titles.Titles;
import mineplex.core.treasure.TreasureManager;
import mineplex.core.twofactor.TwoFactorAuth;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.HubManager;
import mineplex.hub.server.command.NetStatCommand;
import mineplex.hub.server.ui.game.QuickShop;
import mineplex.hub.server.ui.lobby.LobbyShop;
import mineplex.hub.server.ui.server.ServerSelectionShop;
import mineplex.serverdata.data.MinecraftServer;
import mineplex.serverdata.data.ServerGroup;

@ReflectivelyCreateMiniPlugin
public class ServerManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		JOIN_FULL,
		JOIN_ALWAYS,
		FEATURE_SERVER,
		NET_STAT_COMMAND
	}

	private static final long SELECT_SERVER_COOLDOWN = TimeUnit.SECONDS.toMillis(2);
	private static final long SERVER_TIMEOUT = TimeUnit.SECONDS.toMillis(5);
	private static final String MPS_PREFIX = "MPS";

	private final CoreClientManager _clientManager;
	private final Portal _portal;
	private final ServerStatusManager _statusManager;
	private final HubManager _hubManager;
	private final BoosterManager _boosterManager;
	private final TwoFactorAuth _twofactor;
	private final PartyManager _partyManager;
	private final PreferencesManager _preferencesManager;
	private final AchievementManager _achievementManager;
	private final TreasureManager _treasureManager;
	private final PersonalServerManager _personalServerManager;
	private final Titles _titles;

	// Maps ServerGroup name to ServerGroup
	private final Map<String, ServerGroup> _serverGroupsByName;
	// Maps ServerGroup NPC name to ServerGroup
	private final Map<String, ServerGroup> _serverGroupsByNPCName;
	// Maps ServerGroup prefix (CW4) to a Map that maps server name (CW4-5) to GameServer
	private final Map<String, Map<String, GameServer>> _gameServers;
	// Maps ServerType (Not ServerGroup) prefix (CW4) to the number of players on that server
	private final Map<String, Integer> _playersPlaying;
	// Maps (in both directions) ServerGroup NPC name to the game room location
	private final BiMap<String, Location> _serverNPCTeleport;
	// Maps NPC name to the ServerGroup tags associated with that NPC
	private final Map<String, String[]> _serverGroupTags = ImmutableMap.<String, String[]>builder()
			.put("Master Builders", new String[]{"BLD"})
			.put("Draw My Thing", new String[]{"DMT"})
			.put("Micro Battles", new String[]{"MB"})
			.put("Mixed Arcade", new String[]{"MIN"})
			.put("Turf Wars", new String[]{"TF"})
			.put("Speed Builders", new String[]{"SB"})
			.put("Block Hunt", new String[]{"BH"})
			.put("Cake Wars", new String[]{"CW2", "CW4"})
			.put("Survival Games", new String[]{"HG", "SG2"})
			.put("Skywars", new String[]{"SKY", "SKY2"})
			.put("The Bridges", new String[]{"BR"})
			.put("Mine-Strike", new String[]{"MS"})
			.put("Super Smash Mobs", new String[]{"SSM", "SSM2"})
			.put("Champions", new String[]{"DOM", "CTF"})
			.put("Clans", new String[]{"ClansHub", "Clans"})
			.put("Retro", new String[]{"RETRO"})
			.put("Nano Games", new String[]{"NANO"})
			.build();

	private final QuickShop _quickShop;
	private final LobbyShop _lobbyShop;
	private final ServerSelectionShop _serverShop;

	private boolean _alternateUpdateFire, _retrieving;
	private long _lastRetrieve;

	private ServerManager()
	{
		super("Matchmaker");

		_clientManager = require(CoreClientManager.class);
		_portal = require(Portal.class);
		_statusManager = require(ServerStatusManager.class);
		_hubManager = require(HubManager.class);
		_boosterManager = require(BoosterManager.class);
		_twofactor = require(TwoFactorAuth.class);
		_partyManager = require(PartyManager.class);
		_preferencesManager = require(PreferencesManager.class);
		_achievementManager = require(AchievementManager.class);
		_treasureManager = require(TreasureManager.class);
		_personalServerManager = require(PersonalServerManager.class);
		_titles = require(Titles.class);

		_serverGroupsByName = new HashMap<>();
		_serverGroupsByNPCName = new HashMap<>();
		_gameServers = new HashMap<>();
		_playersPlaying = new HashMap<>();
		_serverNPCTeleport = HashBiMap.create();

		_hubManager.getWorldData().getSpongeLocations().forEach((key, locations) ->
		{
			String[] split = key.split(" ");

			if (split.length < 2 || !split[0].equals("TELEPORT"))
			{
				return;
			}

			String npcName = Arrays.stream(split)
					.skip(1)
					.collect(Collectors.joining(" "));
			Location location = locations.get(0);
			UtilAlg.lookAtNearest(location, _hubManager.getLookAt());

			_serverNPCTeleport.put(npcName, location);
		});

		DonationManager donationManager = require(DonationManager.class);

		_quickShop = new QuickShop(this, _clientManager, donationManager, "Quick Menu");
		_lobbyShop = new LobbyShop(this, _clientManager, donationManager, "Lobby Menu");
		_serverShop = new ServerSelectionShop(this, _clientManager, donationManager, "Server Menu");

		NewNPCManager npcManager = require(NewNPCManager.class);
		npcManager.spawnNPCs("GAME_", this::addNPCInfo);
		npcManager.spawnNPCs("FEATURE_", this::addNPCInfo);

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.ULTRA.setPermission(Perm.JOIN_FULL, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.JOIN_ALWAYS, true, true);
		PermissionGroup.CONTENT.setPermission(Perm.FEATURE_SERVER, true, true);
		PermissionGroup.BUILDER.setPermission(Perm.FEATURE_SERVER, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.NET_STAT_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new NetStatCommand(this));
	}

	private void addNPCInfo(NPC npc)
	{
		String[] groups = _serverGroupTags.get(npc.getMetadata().split("_")[1]);

		if (groups == null)
		{
			return;
		}

		((StoredNPC) npc).addInfoVariable("{P}", () ->
		{
			int playerCount = 0;

			for (String group : groups)
			{
				playerCount += getGroupTagPlayerCount(group);
			}

			return String.valueOf(playerCount);
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void npcInteract(NPCInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		String metadata = event.getNpc().getMetadata();
		String[] split = metadata.split("_");

		if (split.length < 2)
		{
			return;
		}

		teleportOrOpen(event.getPlayer(), split[1], split[0].equals("FEATURE"));
	}

	public void teleportOrOpen(Player player, String npcName, boolean checkPreference)
	{
		if (checkPreference && !_preferencesManager.get(player).isActive(Preference.AUTO_QUEUE))
		{
			for (Entry<String, Location> entry : _serverNPCTeleport.entrySet())
			{
				if (entry.getKey().equals(npcName))
				{
					player.teleport(entry.getValue());
					return;
				}
			}
		}

		if (npcName.contains("Clans"))
		{
			ServerGroup serverGroup = _serverGroupsByName.get("ClansHub");

			if (serverGroup == null)
			{
				player.sendMessage(F.main(getName(), "Oh dear, this isn't good. Looks like Clans isn't a registered group."));
				return;
			}

			if (!selectBest(player, serverGroup, false))
			{
				player.sendMessage(F.main(getName(), "Sorry I was unable to find you a good server to send you to."));
			}
			return;
		}

		openServerShop(player, npcName);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerPortalEvent(PlayerPortalEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void entityPortalEvent(EntityPortalEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerCheckPortalEvent(EntityPortalEnterEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			if (event.getEntity() instanceof LivingEntity)
			{
				UtilAction.velocity(event.getEntity(), event.getEntity().getLocation().getDirection().multiply(-1), 1, true, 0.8, 0, 1, true);
			}

			return;
		}

		Player player = (Player) event.getEntity();
		Location location = UtilAlg.findClosest(player.getLocation(), _serverNPCTeleport.values());

		if (location == null || !Recharge.Instance.use(player, "Game Portal", 500, false, false))
		{
			return;
		}

		player.teleport(location);
		runSyncLater(() -> teleportOrOpen(player, _serverNPCTeleport.inverse().get(location), false), 3);
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL)
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (_twofactor.isAuthenticating(player) || _treasureManager.isOpeningTreasure(player) || itemStack == null)
		{
			return;
		}

		if (itemStack.getType() == Material.COMPASS)
		{
			_quickShop.attemptShopOpen(player);
		}
		else if (itemStack.getType() == Material.WATCH)
		{
			_lobbyShop.attemptShopOpen(player);
		}
	}

	@EventHandler
	public void onClickCompassPartyIcon(PartySelectServerEvent event)
	{
		_quickShop.attemptShopOpen(event.getPlayer());
	}

	private void addServerGroup(ServerGroup serverGroup)
	{
		_serverGroupsByName.put(serverGroup.getName(), serverGroup);

		if (serverGroup.getServerNpcName() != null)
		{
			_serverGroupsByNPCName.put(serverGroup.getServerNpcName(), serverGroup);
		}
	}

	private void openServerShop(Player player, String npcName)
	{
		ServerGroup serverGroup = _serverGroupsByNPCName.get(npcName);

		if (serverGroup != null)
		{
			_serverShop.openServerPage(player, serverGroup);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void updatePages(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC)
		{
			_quickShop.updatePages();
			_serverShop.updatePages();
		}
		else if (event.getType() == UpdateType.SLOW)
		{
			_lobbyShop.updatePages();
		}
	}

	@EventHandler
	public void updateServers(UpdateEvent event)
	{
		// Don't retrieve if we are already doing so and have not timed out.
		if (event.getType() != UpdateType.SEC || (_retrieving && !UtilTime.elapsed(_lastRetrieve, SERVER_TIMEOUT)))
		{
			return;
		}

		_alternateUpdateFire = !_alternateUpdateFire;

		if (!_alternateUpdateFire)
		{
			return;
		}

		_retrieving = true;

		runAsync(() ->
		{
			// Get ServerGroups and Servers
			Collection<ServerGroup> serverGroups = _statusManager.retrieveServerGroups();
			Collection<MinecraftServer> serverStatusList = _statusManager.retrieveServerStatuses();

			if (serverGroups == null || serverStatusList == null)
			{
				_retrieving = false;
				_lastRetrieve = System.currentTimeMillis();
				return;
			}

			// Clear cache
			_serverGroupsByName.clear();
			_serverGroupsByNPCName.clear();
			_playersPlaying.clear();

			for (ServerGroup serverGroup : serverGroups)
			{
				addServerGroup(serverGroup);
			}

			// Add MPS Group
			addServerGroup(new ServerGroup(MPS_PREFIX, "Mineplex Player Servers", MPS_PREFIX));

			for (MinecraftServer serverStatus : serverStatusList)
			{
				ServerGroup serverGroup = _serverGroupsByName.get(serverStatus.getGroup());

				// ServerGroup of server not valid
				if (serverGroup == null)
				{
					continue;
				}

				String prefix = serverGroup.getPrefix();

				// Special case for MPS. Normally they are all separate ServerGroups however here we want to group them together
				if (serverGroup.getHost() != null && !serverGroup.getHost().isEmpty())
				{
					prefix = MPS_PREFIX;
				}

				Map<String, GameServer> servers = _gameServers.computeIfAbsent(prefix, k -> new HashMap<>());
				GameServer gameServer = servers.computeIfAbsent(serverStatus.getName(), k -> new GameServer(serverStatus));
				GameInfo gameInfo;

				try
				{
					gameInfo = GameInfo.fromString(serverStatus.getMotd());
				}
				catch (JsonSyntaxException ex)
				{
					// Invalid MOTD. Most likely restarting or starting up
					gameInfo = new GameInfo();
				}

				gameServer.updateStatus(serverStatus, gameInfo);

				_playersPlaying.put(prefix, _playersPlaying.getOrDefault(prefix, 0) + gameServer.getServer().getPlayerCount());
			}

			_gameServers.values().forEach(map -> map.values().removeIf(gameServer ->
			{
				// Dead server, hasn't updated it's status in 5 seconds
				return UtilTime.elapsed(gameServer.getLastUpdate(), SERVER_TIMEOUT);
			}));

			// Reset
			_retrieving = false;
			_lastRetrieve = System.currentTimeMillis();
		});
	}

	public boolean selectServer(Player player, GameServer server)
	{
		if (!Recharge.Instance.use(player, "Select Server", SELECT_SERVER_COOLDOWN, false, false))
		{
			return false;
		}

		player.leaveVehicle();
		player.eject();

		_portal.sendPlayerToServer(player, server.getServer().getName(), Intent.PLAYER_REQUEST);
		return true;
	}

	public boolean selectBest(Player player, ServerGroup serverGroup)
	{
		return selectBest(player, serverGroup, true);
	}

	public boolean selectBest(Player player, ServerGroup serverGroup, boolean checkValid)
	{
		if (!Recharge.Instance.use(player, "Select Best Server", SELECT_SERVER_COOLDOWN, false, false))
		{
			return false;
		}

		Collection<GameServer> servers = getServers(serverGroup.getPrefix());
		int required = getRequiredSlots(player);

		if (checkValid)
		{
			servers.removeIf(server ->
			{
				MinecraftServer serverStatus = server.getServer();
				GameInfo info = server.getInfo();

				if (server.isDevServer() || info.getJoinable() != GameJoinStatus.OPEN || serverStatus.getMaxPlayerCount() - serverStatus.getPlayerCount() <= required)
				{
					return true;
				}

				switch (info.getStatus())
				{
					case ALWAYS_OPEN:
					case WAITING:
					case VOTING:
						return false;
					case STARTING:
						// If the game is about to start, ignore it. The player probably won't make it in time.
						return info.getTimer() < 5;
					default:
						return true;
				}
			});
		}

		if (servers.isEmpty())
		{
			return false;
		}

		List<GameServer> serversList = new ArrayList<>(servers);
		serversList.sort((o1, o2) -> o2.getServer().getPlayerCount() - o1.getServer().getPlayerCount());

		return selectServer(player, serversList.get(0));
	}

	private int getRequiredSlots(Player player)
	{
		int required = 1;
		Party party = _partyManager.getPartyByPlayer(player);

		if (party != null)
		{
			required = party.getSize();
		}

		return required;
	}

	public Collection<GameServer> getServers(String prefix)
	{
		Map<String, GameServer> servers = _gameServers.get(prefix);
		return servers == null ? Collections.emptyList() : servers.values();
	}

	public GameServer getServer(String name)
	{
		for (Map<String, GameServer> servers : _gameServers.values())
		{
			GameServer server = servers.get(name);

			if (server != null)
			{
				return server;
			}
		}

		return null;
	}

	public Map<String, Map<String, GameServer>> getServers()
	{
		return _gameServers;
	}

	public ServerGroup getServerGroupByPrefix(String prefix)
	{
		return _serverGroupsByName.values().stream()
				.filter(serverGroup -> serverGroup.getPrefix().equals(prefix))
				.findFirst()
				.orElse(null);
	}

	public int getGroupTagPlayerCount(String tag)
	{
		return _playersPlaying.getOrDefault(tag, 0);
	}

	public HubManager getHubManager()
	{
		return _hubManager;
	}

	public String[] getServerTags(String npcName)
	{
		return _serverGroupTags.get(npcName);
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}

	public BoosterManager getBoosterManager()
	{
		return _boosterManager;
	}

	public PreferencesManager getPreferencesManager()
	{
		return _preferencesManager;
	}

	public AchievementManager getAchievementManager()
	{
		return _achievementManager;
	}

	public TreasureManager getTreasureManager()
	{
		return _treasureManager;
	}

	public PersonalServerManager getPersonalServerManager()
	{
		return _personalServerManager;
	}

	public Titles getTitles()
	{
		return _titles;
	}

	public QuickShop getQuickShop()
	{
		return _quickShop;
	}
}