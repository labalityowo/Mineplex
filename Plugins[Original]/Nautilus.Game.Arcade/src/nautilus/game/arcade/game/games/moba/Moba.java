package nautilus.game.arcade.game.games.moba;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mineplex.anticheat.checks.combat.KillauraTypeD;
import com.mineplex.anticheat.checks.move.Glide;
import com.mineplex.anticheat.checks.move.HeadRoll;
import com.mineplex.anticheat.checks.move.Speed;

import mineplex.core.Managers;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.antihack.AntiHack;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.imagemap.ImageMapManager;
import mineplex.core.imagemap.objects.MapBoardSelector;
import mineplex.core.imagemap.objects.PlayerMapBoard;
import mineplex.core.leaderboard.Leaderboard;
import mineplex.core.leaderboard.LeaderboardManager;
import mineplex.core.leaderboard.LeaderboardRepository.LeaderboardSQLType;
import mineplex.core.leaderboard.StaticLeaderboard;
import mineplex.minecraft.game.core.combat.DeathMessageType;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerPrepareTeleportEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.moba.boss.BossManager;
import nautilus.game.arcade.game.games.moba.buff.BuffManager;
import nautilus.game.arcade.game.games.moba.fountain.MobaFountain;
import nautilus.game.arcade.game.games.moba.general.ArrowKBManager;
import nautilus.game.arcade.game.games.moba.general.BetaManager;
import nautilus.game.arcade.game.games.moba.general.MobaDamageManager;
import nautilus.game.arcade.game.games.moba.gold.GoldManager;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.game.games.moba.kit.KitPlayer;
import nautilus.game.arcade.game.games.moba.kit.anath.HeroAnath;
import nautilus.game.arcade.game.games.moba.kit.bardolf.HeroBardolf;
import nautilus.game.arcade.game.games.moba.kit.biff.HeroBiff;
import nautilus.game.arcade.game.games.moba.kit.bob.HeroBob;
import nautilus.game.arcade.game.games.moba.kit.dana.HeroDana;
import nautilus.game.arcade.game.games.moba.kit.devon.HeroDevon;
import nautilus.game.arcade.game.games.moba.kit.hattori.HeroHattori;
import nautilus.game.arcade.game.games.moba.kit.hp.HPManager;
import nautilus.game.arcade.game.games.moba.kit.ivy.HeroIvy;
import nautilus.game.arcade.game.games.moba.kit.larissa.HeroLarissa;
import nautilus.game.arcade.game.games.moba.kit.rowena.HeroRowena;
import nautilus.game.arcade.game.games.moba.minion.MinionManager;
import nautilus.game.arcade.game.games.moba.modes.MobaMapType;
import nautilus.game.arcade.game.games.moba.overtime.OvertimeManager;
import nautilus.game.arcade.game.games.moba.progression.MobaProgression;
import nautilus.game.arcade.game.games.moba.shop.MobaShop;
import nautilus.game.arcade.game.games.moba.structure.tower.TowerManager;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import nautilus.game.arcade.game.modules.CustomScoreboardModule;
import nautilus.game.arcade.game.modules.EnderPearlModule;
import nautilus.game.arcade.game.modules.capturepoint.CapturePointModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.lobby.current.NewGameLobbyManager;

public class Moba extends TeamGame
{
	public enum Perm implements Permission
	{
		DEBUG_KIT_COMMAND,
	}

	private static final String[] ITEM_IMAGES =
			{
					"Ivy.png",
					"Anath_the_Burnt.png",
					"Bardolf.png",
					"Biff.png",
					"Dana.png",
					"Devon.png",
					"Hattori.png",
					"Larissa.png",
					"Rowena.png"
			};

	private final HeroKit[] _kits;

	protected final Set<MobaPlayer> _playerData = new HashSet<>();
	private final Set<Listener> _listeners = new HashSet<>();

	protected final MobaShop _shop;
	protected final GoldManager _goldManager;
	protected final BossManager _boss;
	protected final OvertimeManager _overtimeManager;
	protected final BuffManager _buffs;
	protected final ArrowKBManager _arrowKb;
	protected final TowerManager _tower;
	protected final CapturePointModule _capturePoint;
	protected final MinionManager _minion;
	private final MobaProgression _progression;
	private final ImageMapManager _mapManager = Managers.require(ImageMapManager.class);
	private PlayerMapBoard _board;
	private MapBoardSelector _selector;

	private int _inPlayers;

	public Moba(ArcadeManager manager, GameType gameType, String[] description)
	{
		super(manager, gameType, new Kit[]{new KitPlayer(manager)}, description);

		_kits = new HeroKit[]{
				new HeroHattori(Manager),
				new HeroDevon(Manager),
				new HeroAnath(Manager),
				new HeroDana(Manager),
				new HeroBiff(Manager),
				new HeroLarissa(Manager),
				new HeroBardolf(Manager),
				new HeroRowena(Manager),
				new HeroIvy(Manager),
				new HeroBob(Manager)
		};

		AllowParticles = false;

		manager.GetCreature().SetDisableCustomDrops(true);

		// Instantiate managers

		// Global managers
		_shop = registerManager(new MobaShop(this));
		_goldManager = registerManager(new GoldManager(this));
		_boss = registerManager(new BossManager(this));
		_overtimeManager = registerManager(new OvertimeManager(this));
		_buffs = registerManager(new BuffManager());
		_arrowKb = registerManager(new ArrowKBManager(this));
		_minion = registerManager(new MinionManager(this));
		_progression = registerManager(new MobaProgression(this));
		registerManager(new HPManager(this));
		registerManager(new MobaDamageManager(this));
		registerManager(new MobaFountain(this));
		new EnderPearlModule()
				.setMaxTicks(40)
				.register(this);

		// Structures
		_tower = registerManager(new TowerManager(this));
		_capturePoint = new CapturePointModule();
		_capturePoint.register(this);

		// Beta Message
		registerManager(new BetaManager(this));

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);

		// Disable specific GWEN checks for this game
		AntiHack antiHack = Managers.get(AntiHack.class);
		antiHack.addIgnoredCheck(Speed.class);
		antiHack.addIgnoredCheck(Glide.class);
		antiHack.addIgnoredCheck(HeadRoll.class);
		antiHack.addIgnoredCheck(KillauraTypeD.class);
	}

	protected <T extends Listener> T registerManager(T listener)
	{
		_listeners.add(listener);
		return listener;
	}

	@Override
	public void ParseData()
	{
		// Make all spawns face the center of the map
		for (List<Location> locations : WorldData.getAllSpawnLocations().values())
		{
			locations.forEach(location -> location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, GetSpectatorLocation()))));
		}

		SpectatorSpawn = WorldData.GetCustomLocs("CENTER").get(0);

		MobaMapType mapType = null;

		for (String key : WorldData.GetAllCustomLocs().keySet())
		{
			try
			{
				mapType = MobaMapType.valueOf(key);
				break;
			}
			catch (IllegalArgumentException e)
			{
			}
		}

		if (mapType == null)
		{
			mapType = MobaMapType.HEROES_VALLEY;
		}

		registerManager(mapType.createInstance(this));

		if (Manager.IsRewardStats() && Manager.GetLobby() instanceof NewGameLobbyManager && !Manager.GetGameHostManager().isPrivateServer())
		{
			LeaderboardManager leaderboardManager = Managers.get(LeaderboardManager.class);
			Map<String, List<Location>> lobbyCustomLocs = ((NewGameLobbyManager) Manager.GetLobby()).getCustomLocs();
			LeaderboardManager leaderboard = Managers.get(LeaderboardManager.class);

			{
				Location location = lobbyCustomLocs.get("TOP_DAILY_WINS").get(0);
				leaderboard.registerIfNotExists("TOP_HOG_DAILY_WINS", new StaticLeaderboard(
						leaderboardManager,
						"Top Daily Wins",
						new Leaderboard(
								LeaderboardSQLType.DAILY,
								GetName() + ".Wins"
						),
						location));
			}
			{
				Location location = lobbyCustomLocs.get("TOP_DAILY_KILLS").get(0);
				leaderboard.registerIfNotExists("TOP_HOG_DAILY_KILLS", new StaticLeaderboard(
						leaderboardManager,
						"Top Daily Kills",
						new Leaderboard(
								LeaderboardSQLType.DAILY,
								GetName() + ".Kills"
						),
						location));
			}
			{
				Location location = lobbyCustomLocs.get("TOP_DAILY_GOLD").get(0);
				leaderboard.registerIfNotExists("TOP_HOG_DAILY_GOLD", new StaticLeaderboard(
						leaderboardManager,
						"Top Daily Gold Earned",
						new Leaderboard(
								LeaderboardSQLType.DAILY,
								GetName() + ".GoldEarned"
						),
						location));
			}
			{
				Location location = lobbyCustomLocs.get("TOP_WINS").get(0);
				leaderboard.registerIfNotExists("TOP_HOG_WINS", new StaticLeaderboard(
						leaderboardManager,
						"Top Wins",
						new Leaderboard(
								LeaderboardSQLType.ALL,
								GetName() + ".Wins"
						),
						location));
			}
			{
				Location location = lobbyCustomLocs.get("TOP_KILLS").get(0);
				leaderboard.registerIfNotExists("TOP_HOG_KILLS", new StaticLeaderboard(
						leaderboardManager,
						"Top Kills",
						new Leaderboard(
								LeaderboardSQLType.ALL,
								GetName() + ".Kills"
						),
						location));
			}
			{
				Location location = lobbyCustomLocs.get("TOP_GOLD").get(0);
				leaderboard.registerIfNotExists("TOP_HOG_GOLD", new StaticLeaderboard(
						leaderboardManager,
						"Top Gold",
						new Leaderboard(
								LeaderboardSQLType.ALL,
								GetName() + ".GoldEarned"
						),
						location));
			}

			_progression.spawnRoleViewers(lobbyCustomLocs);

//			_board = _mapManager.createPlayerBoard(lobbyCustomLocs.get("HERO_VIEWER").get(0), BlockFace.EAST, 7, 4, ITEM_IMAGES);
//			_selector = new MapBoardSelector(_board);
//			_selector.createHolograms(lobbyCustomLocs.get("HERO_VIEWER NEXT").get(0), lobbyCustomLocs.get("HERO_VIEWER BACK").get(0));
		}

		// Register all "Managers"
		_listeners.forEach(UtilServer::RegisterEvents);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		// Override those kits!
		setKits(_kits);

		// Store player data
		GetPlayers(true).forEach(this::setupPlayerData);

		// Make sure to cleanup
		cleanupLobby();
	}

	public void setupPlayerData(Player player)
	{
		_playerData.add(new MobaPlayer(player));
		MobaUtil.setTeamEntity(player, GetTeam(player));
	}

	@EventHandler
	public void preventOverfill(PlayerPrepareTeleportEvent event)
	{
		Player player = event.GetPlayer();

		if (++_inPlayers > 8)
		{
			SetPlayerState(player, GameTeam.PlayerState.OUT);
			Manager.addSpectator(player, true);
			player.sendMessage(F.main("Game", "Too many players are in this server. You are now spectating, sorry."));
		}
	}

	@Override
	public void RespawnPlayer(Player player)
	{
		super.RespawnPlayer(player);
		player.setGameMode(GameMode.ADVENTURE);
	}

	private void cleanupLobby()
	{
		if (_board != null)
		{
			_mapManager.cleanupBoard(_board);
			_selector.cleanup();
		}

		_progression.removeRoleViewers();
	}

	@Override
	public void disable()
	{
		super.disable();
		_listeners.forEach(UtilServer::Unregister);
		_listeners.clear();

		cleanupLobby();

		Manager.runSyncLater(() ->
		{
			// Undisguise all players
			for (Player player : Bukkit.getOnlinePlayers())
			{
				DisguiseBase disguise = Manager.GetDisguise().getActiveDisguise(player);

				if (disguise != null && disguise instanceof DisguisePlayer)
				{
					Manager.GetDisguise().undisguise(disguise);
				}
			}
		}, 50);
	}

	@Override
	public void SetKit(Player player, Kit kit, boolean announce)
	{
		super.SetKit(player, kit, announce);

		if (kit instanceof HeroKit)
		{
			getMobaData(player).setKit((HeroKit) kit);
		}
	}

	@Override
	public DeathMessageType GetDeathMessageType()
	{
		return DeathMessageType.Detailed;
	}

	@Override
	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		return assist ? 1 : 2;
	}

	// Clear up memory
	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		_playerData.removeIf(mobaPlayer -> mobaPlayer.getPlayer().equals(player));
	}

	// Clean up arrows
	@EventHandler
	public void projectileHit(ProjectileHitEvent event)
	{
		if (event.getEntity() instanceof Arrow)
		{
			event.getEntity().remove();
		}
	}

	public Map<String, Location> getLocationStartsWith(String s)
	{
		Map<String, Location> map = new HashMap<>();

		for (String key : WorldData.GetAllCustomLocs().keySet())
		{
			if (key.startsWith(s))
			{
				map.put(key, WorldData.GetCustomLocs(key).get(0));
			}
		}

		return map;
	}

	public GameTeam getTeam(String name)
	{
		for (GameTeam team : GetTeamList())
		{
			if (team.GetName().equalsIgnoreCase(name))
			{
				return team;
			}
		}

		return null;
	}

	public HeroKit[] getKits()
	{
		return _kits;
	}

	public List<HeroKit> getKits(MobaRole role)
	{
		List<HeroKit> kits = new ArrayList<>();

		for (HeroKit kit : _kits)
		{
			if (kit.getRole() == role && kit.isVisible())
			{
				kits.add(kit);
			}
		}

		return kits;
	}

	public Set<MobaPlayer> getMobaData()
	{
		return _playerData;
	}

	public MobaPlayer getMobaData(Player player)
	{
		for (MobaPlayer mobaPlayer : _playerData)
		{
			if (mobaPlayer.getPlayer().equals(player))
			{
				return mobaPlayer;
			}
		}

		return null;
	}

	public HeroKit getFirstKit(Player player)
	{
		MobaPlayer mobaPlayer = getMobaData(player);

		if (mobaPlayer.getRole() == null)
		{
			MobaRole role = getRandomRole(player);

			return getFirstKit(role);
		}
		else if (mobaPlayer.getKit() == null)
		{
			return getFirstKit(mobaPlayer.getRole());
		}

		return null;
	}

	private HeroKit getFirstKit(MobaRole role)
	{
		for (HeroKit kit : _kits)
		{
			if (kit.getRole() == role && kit.isVisible())
			{
				return kit;
			}
		}

		return null;
	}

	private MobaRole getRandomRole(Player player)
	{
		List<MobaRole> roles = new ArrayList<>();

		for (MobaPlayer mobaPlayer : getTeamData(GetTeam(player)))
		{
			MobaRole role = mobaPlayer.getRole();

			if (role != null)
			{
				roles.add(role);
			}
		}

		return UtilAlg.Random(Arrays.asList(MobaRole.values()), roles);
	}

	private List<MobaPlayer> getTeamData(GameTeam team)
	{
		List<MobaPlayer> players = new ArrayList<>();

		for (MobaPlayer mobaPlayer : _playerData)
		{
			GameTeam otherTeam = GetTeam(mobaPlayer.getPlayer());

			if (team.equals(otherTeam))
			{
				players.add(mobaPlayer);
			}
		}

		return players;
	}

	public boolean isRoleFree(Player player, MobaRole role)
	{
		GameTeam team = GetTeam(player);

		if (team == null)
		{
			return false;
		}

		for (MobaPlayer mobaPlayer : getTeamData(team))
		{
			if (mobaPlayer.getRole() == role)
			{
				return false;
			}
		}

		return true;
	}

	public CustomScoreboardModule getScoreboardModule()
	{
		return getModule(CustomScoreboardModule.class);
	}

	public MobaShop getShop()
	{
		return _shop;
	}

	public GoldManager getGoldManager()
	{
		return _goldManager;
	}

	public OvertimeManager getOvertimeManager()
	{
		return _overtimeManager;
	}

	public BuffManager getBuffManager()
	{
		return _buffs;
	}

	public TowerManager getTowerManager()
	{
		return _tower;
	}

	public CapturePointModule getCapturePointManager()
	{
		return _capturePoint;
	}

	public BossManager getBossManager()
	{
		return _boss;
	}

	public ArrowKBManager getArrowKbManager()
	{
		return _arrowKb;
	}

	public MinionManager getMinionManager()
	{
		return _minion;
	}

	public MobaProgression getProgression()
	{
		return _progression;
	}
}
