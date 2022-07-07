package nautilus.game.arcade.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.server.v1_8_R3.EntityItem;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;

import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;

import mineplex.core.Managers;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.antihack.AntiHack;
import mineplex.core.arcadeevents.CoreGameStartEvent;
import mineplex.core.arcadeevents.CoreGameStopEvent;
import mineplex.core.command.CommandCenter;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTabTitle;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.elo.EloPlayer;
import mineplex.core.elo.EloTeam;
import mineplex.core.game.MineplexGameManager;
import mineplex.core.game.kit.KitAvailability;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.lifetimes.Lifetimed;
import mineplex.core.lifetimes.ListenerComponent;
import mineplex.core.lifetimes.PhasedLifetime;
import mineplex.core.mission.MissionTracker;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.preferences.Preference;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilGameProfile;
import mineplex.minecraft.game.classcombat.event.ClassCombatCreatureAllowSpawnEvent;
import mineplex.minecraft.game.core.combat.DeathMessageType;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

import nautilus.game.arcade.ArcadeFormat;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerGameRespawnEvent;
import nautilus.game.arcade.events.PlayerPrepareTeleportEvent;
import nautilus.game.arcade.events.PlayerStateChangeEvent;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.game.modules.AntiExpOrbModule;
import nautilus.game.arcade.game.modules.Module;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryModule;
import nautilus.game.arcade.game.team.GameTeamModule;
import nautilus.game.arcade.game.team.selectors.EvenTeamSelector;
import nautilus.game.arcade.game.team.selectors.TeamSelector;
import nautilus.game.arcade.kit.ChampionsKit;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.LinearUpgradeKit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.managers.GamePlayerManager;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.managers.lobby.LobbyManager;
import nautilus.game.arcade.missions.BlocksMissionTracker;
import nautilus.game.arcade.missions.CraftItemMissionTracker;
import nautilus.game.arcade.missions.DamageMissionTracker;
import nautilus.game.arcade.missions.EnchantItemMissionTracker;
import nautilus.game.arcade.missions.FurnaceMissionTracker;
import nautilus.game.arcade.missions.GameMissionTracker;
import nautilus.game.arcade.missions.KillMissionTracker;
import nautilus.game.arcade.missions.PlayGameMissionTracker;
import nautilus.game.arcade.missions.WalkMissionTracker;
import nautilus.game.arcade.missions.WinMissionTracker;
import nautilus.game.arcade.scoreboard.GameScoreboard;
import nautilus.game.arcade.stats.AssistsStatTracker;
import nautilus.game.arcade.stats.DamageDealtStatTracker;
import nautilus.game.arcade.stats.DamageTakenStatTracker;
import nautilus.game.arcade.stats.DeathsStatTracker;
import nautilus.game.arcade.stats.ExperienceStatTracker;
import nautilus.game.arcade.stats.GamesPlayedStatTracker;
import nautilus.game.arcade.stats.KillsStatTracker;
import nautilus.game.arcade.stats.LoseStatTracker;
import nautilus.game.arcade.stats.StatTracker;
import nautilus.game.arcade.stats.WinStatTracker;
import nautilus.game.arcade.wineffect.WinEffectManager;
import nautilus.game.arcade.world.WorldData;

public abstract class Game extends ListenerComponent implements Lifetimed
{
	private final static int MAX_TICK_SPEED_MEASUREMENT = 40;
	
	public long getGameLiveTime()
	{
		return _gameLiveTime;
	}

	public void setGameLiveTime(long gameLiveTime)
	{
		_gameLiveTime = gameLiveTime;
	}

	public enum GameState
	{
		PreLoad, Loading, Recruit, Prepare, Live, End, Dead
	}

	public ArcadeManager Manager;

	// Game
	private GameType _gameType;
	protected String[] _gameDesc;

	private PhasedLifetime<GameState> _lifetime = new PhasedLifetime<>();

	// State
	private GameState _gameState = GameState.PreLoad;
	private long _gameLiveTime;
	private long _gameStateTime = System.currentTimeMillis();

	private boolean _prepareCountdown = false;

	private int _countdown = -1;
	private boolean _countdownForce = false;

	private String _customWinLine = "";
	private NautHashMap<Player, String> _customWinMessages = new NautHashMap<Player, String>();

	// Kits
	private Kit[] _kits;

	// Teams
	private final GameTeamModule _teamModule;
	protected ArrayList<GameTeam> _teamList = new ArrayList<GameTeam>();
	protected TeamSelector _teamSelector = new EvenTeamSelector();
	public boolean TeamMode = false;

	// Player Preferences
	protected NautHashMap<Player, Kit> _playerKit = new NautHashMap<Player, Kit>();

	// Player Data
	private NautHashMap<Player, HashMap<String, GemData>> _gemCount = new NautHashMap<Player, HashMap<String, GemData>>();
	private final Map<Player, Map<String, Integer>> _stats = new HashMap<>();
	private NautHashMap<Player, Long> _playerInTime = new NautHashMap<>();

	// Player Location Store
	private final Map<String, Location> _playerLocationStore = new HashMap<>();

	// Scoreboard
	protected GameScoreboard Scoreboard;
	public boolean UseCustomScoreboard = false;

	// Loaded from Map Config
	public WorldData WorldData;

	// Game Help
	private int _helpIndex = 0;
	private ChatColor _helpColor;
	protected String[] _help;

	// Gameplay Flags
	public long GameTimeout = 1200000;

	public boolean SpectatorAllowed = true;

	public boolean Damage = true;
	public boolean DamagePvP = true;
	public boolean DamagePvE = true;
	public boolean DamageEvP = true;
	public boolean DamageSelf = true;
	public boolean DamageFall = true;
	public boolean DamageTeamSelf = false;
	public boolean DamageTeamOther = true;

	public boolean BlockBreak = false;
	public boolean BlockBreakCreative = false;
	public HashSet<Integer> BlockBreakAllow = new HashSet<Integer>();
	public HashSet<Integer> BlockBreakDeny = new HashSet<Integer>();

	public boolean BlockPlace = false;
	public boolean BlockPlaceCreative = false;
	public HashSet<Integer> BlockPlaceAllow = new HashSet<Integer>();
	public HashSet<Integer> BlockPlaceDeny = new HashSet<Integer>();

	public boolean ItemPickup = false;
	public HashSet<Integer> ItemPickupAllow = new HashSet<Integer>();
	public HashSet<Integer> ItemPickupDeny = new HashSet<Integer>();

	public boolean ItemDrop = false;
	public HashSet<Integer> ItemDropAllow = new HashSet<Integer>();
	public HashSet<Integer> ItemDropDeny = new HashSet<Integer>();

	public boolean InventoryOpenBlock = false;
	public boolean InventoryOpenChest = false;
	public boolean InventoryClick = false;

	public boolean PrivateBlocks = false;

	public boolean DeathOut = true;
	public boolean DeathDropItems = false;
	public boolean DeathMessages = true;

	public double DeathSpectateSecs = 0;
	public boolean DeathTeleport = true;

	public boolean QuitOut = true;
	public boolean QuitDropItems = false;

	public boolean CreatureAllow = false;
	public boolean CreatureAllowOverride = false;

	public int WorldTimeSet = 12000;
	public boolean WorldWeatherEnabled = false;
	public int WorldWaterDamage = 0;
	public boolean WorldBoundary = true;
	public boolean WorldBoundaryKill = true;
	public boolean WorldBlockBurn = false;
	public boolean WorldBlockGrow = false;
	public boolean WorldFireSpread = false;
	public boolean WorldLeavesDecay = false;
	public boolean WorldSoilTrample = false;
	public boolean WorldBoneMeal = false;
	public boolean WorldChunkUnload = false;
	
	public boolean AllowFlintAndSteel = false;

	public int HungerSet = -1;
	public int HealthSet = -1;

	public boolean SpawnTeleport = true;

	private double _itemMergeRadius = 0;

	public boolean AnnounceStay = true;
	public boolean AnnounceJoinQuit = true;
	public boolean AnnounceSilence = true;

	public GameState KitRegisterState = GameState.Live;

	public boolean JoinInProgress = false;

	public int TickPerTeleport = 1;

	public boolean SpawnNearAllies = false;
	public boolean SpawnNearEnemies = false;

	public boolean StrictAntiHack = false;
	public boolean AnticheatDisabled = false;

	public boolean DisableKillCommand = true;

	public boolean GadgetsDisabled = true;

	public boolean TeleportsDisqualify = true;

	public GameMode PlayerGameMode = GameMode.SURVIVAL;

	// Addons
	public boolean SoupEnabled = true;

	public boolean GiveClock = true;
	
	public boolean AllowParticles = true;
	public boolean ShowWeaponNames = true;

	public boolean Prepare = true;
	public long PrepareTime = 9000;
	public boolean PrepareFreeze = true;
	public boolean PrepareAutoAnnounce = true;
	public boolean PlaySoundGameStart = true;

	public double XpMult = 1;
	
	public boolean SpeedMeasurement = false;
	
	// Chat Stats
	public final ChatStatData Kills = new ChatStatData("Kills", "Kills", true);
	public final ChatStatData Assists = new ChatStatData("Assists", "Assists", true);
	public final ChatStatData DamageDealt = new ChatStatData("Damage Dealt", "Damage Dealt", true);
	public final ChatStatData DamageTaken = new ChatStatData("Damage Taken", "Damage Taken", true);
	public final ChatStatData DamageTakenPVP = new ChatStatData("Damage Taken PvP", "Damage Taken", true);
	public final ChatStatData Deaths = new ChatStatData("Deaths", "Deaths", true);
	public final ChatStatData ExpEarned = new ChatStatData("ExpEarned", "Exp Earned", true);
	public final ChatStatData GamesPlayed = new ChatStatData("GamesPlayed", "Games Played", true);
	public final ChatStatData GemsEarned = new ChatStatData("GemsEarned", "Gems Earned", true);
	public final ChatStatData Losses = new ChatStatData("Losses", "Losses", true);
	public final ChatStatData Wins = new ChatStatData("Wins", "Wins", true);
	public final ChatStatData KDRatio = new ChatStatData("KDRatio", "KD-Ratio", true);
	public final ChatStatData BlankLine = new ChatStatData().blankLine();

	// Gems
	public boolean CrownsEnabled = false;
	
	public double GemMultiplier = 1;
	public boolean GemHunterEnabled = true;
	public boolean GemBoosterEnabled = true;
	public boolean GemDoubleEnabled = true;

	public double GemKillDeathRespawn = .5;
	public double GemAssistDeathRespawn = .5;

	public double GemKillDeathOut = 4;
	public double GemAssistDeathOut = 1;

	// Gameplay Data
	public HashMap<Location, Player> PrivateBlockMap = new HashMap<Location, Player>();
	public HashMap<String, Integer> PrivateBlockCount = new HashMap<String, Integer>();

	public Location SpectatorSpawn = null;

	public boolean FirstKill = true;
	public int FirstKillReward = 10;

	public String Winner = "Nobody";
	public GameTeam WinnerTeam = null;

	//ELO
	public boolean EloRanking = false;
	public int EloStart = 1000;

	public boolean CanAddStats = true;
	public boolean CanGiveLoot = true;

	public boolean HideTeamSheep = false;
	public boolean ReplaceTeamsWithKits = false;

	public boolean DeadBodies = false;
	public boolean DeadBodiesQuit = true;
	public boolean DeadBodiesDeath = true;
	public int DeadBodiesExpire = -1;

	public boolean EnableTutorials = false;

	public boolean FixSpawnFacing = true;

	public boolean AllowEntitySpectate = true;
	
	// Used for "%player% is your teammate"
	public boolean ShowTeammateMessage = false;
	
	public boolean ShowEveryoneSpecChat = true;

	// Split Kit XP
	public boolean SplitKitXP = false;

	public boolean NightVision = false;

	private IPacketHandler _useEntityPacketHandler;
	private int _deadBodyCount;
	private NautHashMap<String, Entity> _deadBodies = new NautHashMap<String, Entity>();
	private NautHashMap<String, Long> _deadBodiesExpire = new NautHashMap<String, Long>();

	private final Set<StatTracker<? extends Game>> _statTrackers = new HashSet<>();

	public final WinEffectManager WinEffectManager = new WinEffectManager();
	public boolean WinEffectEnabled = true;

	private Map<Class<? extends Module>, Module> _modules = new HashMap<>();
	
	private HashMap<UUID, LinkedList<Triple<Double, Double, Double>>> _playerPastLocs = new HashMap<>();
	private Set<DebugCommand> _debugCommands = new HashSet<>();

	public enum Perm implements Permission
	{
		DEBUG_COMMANDS
	}
	
	public Game(ArcadeManager manager, GameType gameType, Kit[] kits, String[] gameDesc)
	{
		Manager = manager;

		_lifetime.register(this);

		// Game
		_gameType = gameType;
		_gameDesc = gameDesc;

		// Kits
		_kits = kits;

		// Scoreboard
		Scoreboard = new GameScoreboard(this);
		WorldData = new WorldData(this);

		// Stat Trackers
		registerStatTrackers(
				new KillsStatTracker(this),
				new DeathsStatTracker(this),
				new AssistsStatTracker(this),
				new ExperienceStatTracker(this),
				new WinStatTracker(this),
				new LoseStatTracker(this),
				new DamageDealtStatTracker(this),
				new DamageTakenStatTracker(this),
				new GamesPlayedStatTracker(this)
		);

		// Mission Tracks
		registerMissions(
				new PlayGameMissionTracker(this),
				new WinMissionTracker(this),
				new KillMissionTracker(this),
				new DamageMissionTracker(this),
				new CraftItemMissionTracker(this),
				new EnchantItemMissionTracker(this),
				new WalkMissionTracker(this),
				new BlocksMissionTracker(this),
				new FurnaceMissionTracker(this)
		);
		manager.getMissionsManager().setCanIncrement(() -> CanAddStats && (InProgress() || GetState() == GameState.End) && manager.IsRewardStats());

		Manager.getResourcePackManager().setResourcePack(gameType.getResourcePackUrls(this), gameType.isEnforceResourcePack(this));

		_useEntityPacketHandler = packetInfo ->
		{
			if (packetInfo.getPacket() instanceof PacketPlayInUseEntity)
			{
				net.minecraft.server.v1_8_R3.Entity entity = ((PacketPlayInUseEntity) packetInfo.getPacket())
						.a(((CraftWorld) packetInfo.getPlayer().getWorld()).getHandle());

				if (entity instanceof EntityItem)
				{
					packetInfo.setCancelled(true);
				}
			}
		};

		System.out.println("Loading " + GetName() + "...");

		new AntiExpOrbModule().register(this);
		new GameSummaryModule()
				.register(this);
		_teamModule = new GameTeamModule();
		_teamModule.register(this);

		registerDebugCommand("kit", Perm.DEBUG_COMMANDS, PermissionGroup.ADMIN, (caller, args) ->
		{
			String kit = Arrays.stream(args).collect(Collectors.joining(" "));

			for (Kit gkit : GetKits())
			{
				if (kit.equalsIgnoreCase(gkit.GetName()))
				{
					SetKit(caller, gkit, true);
					return;
				}
			}

			caller.sendMessage(F.main("Kit", "Sorry, but that is not a kit!"));
		});
		registerDebugCommand("cooldown", Perm.DEBUG_COMMANDS, PermissionGroup.ADMIN, (caller, args) ->
		{
			for (Player other : UtilServer.getPlayers())
			{
				Recharge.Instance.Reset(other);
			}

			Announce(C.cWhiteB + caller.getName() + C.cAquaB + " reset cooldowns!");
		});
	}

	// You should never use this so please don't. Use Module.register instead
	public final void registerModule(Module module)
	{
		if (!_modules.containsKey(module.getClass()))
		{
			_modules.put(module.getClass(), module);
			UtilServer.RegisterEvents(module);
			module.initialize(this);
		}
		else
		{
			throw new IllegalStateException("Module " + module.getClass() + " is already registered");
		}
	}

	public void unregisterModule(Module module)
	{
		if (_modules.containsKey(module.getClass()) && _modules.get(module.getClass()) == module)
		{
			_modules.remove(module.getClass());
			module.cleanup();
			HandlerList.unregisterAll(module);
		}
	}

	public <T extends Enum<T> & Permission> void registerDebugCommand(String commandName, T permission, PermissionGroup defaultRank, DebugCommandExecutor executor)
	{
		DebugCommand command = new DebugCommand(commandName, permission, executor);
		if (defaultRank != null)
		{
			defaultRank.setPermission(permission, true, true);
		}
		if (UtilServer.isTestServer())
		{
			PermissionGroup.QA.setPermission(permission, true, true);
		}
		_debugCommands.add(command);
		for (String string : command.Aliases())
		{
			if (CommandCenter.getCommands().containsKey(string.toLowerCase()))
			{
				throw new IllegalArgumentException("Existing command: " + string.toLowerCase());
			}
		}
		CommandCenter.Instance.addCommand(command);
	}

	public void setKits(Kit[] kits)
	{
		_kits = kits;
	}

	public String GetName()
	{
		return _gameType.getName();
	}

	public static GameType[] getWorldHostNames(GameType targetType)
	{
		GameType[] mapSource = new GameType[]
				{
						targetType
				};

		if (targetType.getMapSource() != null)
		{
			if (targetType.ownMaps())
			{
				int i = 1;
				mapSource = new GameType[targetType.getMapSource().length + 1];
				for (GameType type : targetType.getMapSource())
				{
					mapSource[i] = type;
					i++;
				}
				mapSource[0] = targetType;
			}
			else
			{
				mapSource = targetType.getMapSource();
			}
		}

		return mapSource;
	}

	public String GetMode()
	{
		return null;
	}

	public boolean isAllowingGameStats()
	{
		return true;
	}

	public GameType GetType()
	{
		return _gameType;
	}

	public String[] GetDesc()
	{
		return _gameDesc;
	}

	public void SetCustomWinLine(String line)
	{
		_customWinLine = line;
	}

	public void SetCustomWinMessage(Player player, String message)
	{
		_customWinMessages.put(player, message);
	}

	public GameScoreboard GetScoreboard()
	{
		return Scoreboard;
	}

	public ArrayList<GameTeam> GetTeamList()
	{
		return _teamList;
	}

	public GameTeamModule getTeamModule()
	{
		return _teamModule;
	}

	public int GetCountdown()
	{
		return _countdown;
	}

	public void SetCountdown(int time)
	{
		_countdown = time;
	}

	public boolean GetCountdownForce()
	{
		return _countdownForce;
	}

	public void SetCountdownForce(boolean value)
	{
		_countdownForce = value;
	}

	public NautHashMap<Player, Kit> GetPlayerKits()
	{
		return _playerKit;
	}

	public NautHashMap<Player, HashMap<String, GemData>> GetPlayerGems()
	{
		return _gemCount;
	}

	public Map<String, Location> GetLocationStore()
	{
		return _playerLocationStore;
	}

	public GameState GetState()
	{
		return _gameState;
	}

	public void prepareToRecruit()
	{
		UtilTabTitle.broadcastHeaderAndFooter(GamePlayerManager.getHeader(this), GamePlayerManager.FOOTER);

		generateTeams();
		recruit();
	}

	public void recruit()
	{
		SetState(GameState.Recruit);
	}

	public void generateTeams()
	{
		int count = 1;

		for (Entry<String, List<Location>> entry : WorldData.getAllSpawnLocations().entrySet())
		{
			String team = entry.getKey();
			ChatColor color;

			if (team.equalsIgnoreCase("RED"))			color = ChatColor.RED;
			else if (team.equalsIgnoreCase("YELLOW"))	color = ChatColor.YELLOW;
			else if (team.equalsIgnoreCase("GREEN"))	color = ChatColor.GREEN;
			else if (team.equalsIgnoreCase("BLUE"))		color = ChatColor.AQUA;
			else if (team.equalsIgnoreCase("PINK"))		color = ChatColor.LIGHT_PURPLE;
			else if (team.equalsIgnoreCase("CYAN"))		color = ChatColor.DARK_AQUA;
			else if (team.equalsIgnoreCase("PURPLE"))	color = ChatColor.DARK_PURPLE;
			else if (team.equalsIgnoreCase("ORANGE"))	color = ChatColor.GOLD;
			else
			{
				color = ChatColor.DARK_GREEN;
				int modulo = GetTeamList().size() % 14;
				
				if (modulo == 0) 		if (WorldData.getAllSpawnLocations().size() > 1)		color = ChatColor.RED;
				if (modulo == 1) 		color = ChatColor.YELLOW;
				if (modulo == 2) 		color = ChatColor.GREEN;
				if (modulo == 3) 		color = ChatColor.AQUA;
				if (modulo == 4) 		color = ChatColor.GOLD;
				if (modulo == 5) 		color = ChatColor.LIGHT_PURPLE;
				if (modulo == 6) 		color = ChatColor.DARK_BLUE;
				if (modulo == 7) 		color = ChatColor.WHITE;
				if (modulo == 8) 		color = ChatColor.BLUE;
				if (modulo == 9) 		color = ChatColor.DARK_GREEN;
				if (modulo == 10)		color = ChatColor.DARK_PURPLE;
				if (modulo == 11) 		color = ChatColor.DARK_RED;
				if (modulo == 12) 		color = ChatColor.DARK_AQUA;
			}

			//Random Names
			String teamName = team;
			if (WorldData.getAllSpawnLocations().size() > 12)
			{
				teamName = String.valueOf(count);
				count++;
			}

			GameTeam newTeam = new GameTeam(this, teamName, color, entry.getValue());
			AddTeam(newTeam);
		}

		//Restrict Kits
		RestrictKits();

		//Parse Data
		ParseData();
	}

	public void SetState(GameState state)
	{
		_gameState = state;
		_gameStateTime = System.currentTimeMillis();

		if (_gameState == Game.GameState.Prepare)
		{
			// Speed Builders, Master Builders, Draw My Thing, Castle Siege
			if (!AnticheatDisabled)
			{
				Managers.get(AntiHack.class).enableAnticheat();
			}
		}
		else if (_gameState == Game.GameState.End && !this.AnticheatDisabled)
		{
			Managers.get(AntiHack.class).disableAnticheat();
		}


		if (_gameState == GameState.Live)
			setGameLiveTime(_gameStateTime);

		for (Player player : UtilServer.getPlayers())
			player.leaveVehicle();


		_lifetime.setPhase(state);

		// Event
		GameStateChangeEvent stateEvent = new GameStateChangeEvent(this, state);
		UtilServer.getServer().getPluginManager().callEvent(stateEvent);

		System.out.println(GetName() + " state set to " + state.toString());

		if (state.equals(GameState.Prepare))
		{
			CoreGameStartEvent coreGameStartEvent = new CoreGameStartEvent(GetType().getDisplay());
			UtilServer.getServer().getPluginManager().callEvent(coreGameStartEvent);
		}
		else if (state.equals(GameState.End))
		{
			CoreGameStopEvent coreGameStopEvent = new CoreGameStopEvent(GetType().getDisplay());
			UtilServer.getServer().getPluginManager().callEvent(coreGameStopEvent);
		}
	}

	public void SetStateTime(long time)
	{
		_gameStateTime = time;
	}

	public long GetStateTime()
	{
		return _gameStateTime;
	}

	public boolean inLobby()
	{
		return GetState() == GameState.PreLoad || GetState() == GameState.Loading || GetState() == GameState.Recruit;
	}

	public boolean InProgress()
	{
		return GetState() == GameState.Prepare || GetState() == GameState.Live;
	}

	public boolean IsLive()
	{
		return _gameState == GameState.Live;
	}

	public void AddTeam(GameTeam team)
	{
		// Add
		GetTeamList().add(team);

		System.out.println("Created Team: " + team.GetName());
	}

	public void RemoveTeam(GameTeam team)
	{
		if (GetTeamList().remove(team))
			System.out.println("Deleted Team: " + team.GetName());
	}

	public boolean HasTeam(GameTeam team)
	{
		for (GameTeam cur : GetTeamList())
			if (cur.equals(team))
				return true;

		return false;
	}

	public void RestrictKits()
	{
		// Null Default
	}

	public void RegisterKits()
	{
		for (Kit kit : _kits)
		{
			if (kit == null)
			{
				continue;
			}

			kit.registerEvents();
			UtilServer.RegisterEvents(kit);

			if (kit instanceof LinearUpgradeKit)
			{
				for (Perk[] arrayOfPerks : ((LinearUpgradeKit) kit).getPerks())
				{
					for (Perk perk : arrayOfPerks)
					{
						UtilServer.RegisterEvents(perk);
						perk.registeredEvents();
					}
				}
			}
			else
			{
				for (Perk perk : kit.GetPerks())
				{
					UtilServer.getServer().getPluginManager().registerEvents(perk, Manager.getPlugin());
					perk.registeredEvents();
				}
			}
		}
	}

	public void DeregisterKits()
	{
		for (Kit kit : _kits)
		{
			if (kit == null)
			{
				continue;
			}

			kit.unregisterEvents();
			HandlerList.unregisterAll(kit);

			if (kit instanceof LinearUpgradeKit)
			{
				for (Perk[] arrayOfPerks : ((LinearUpgradeKit) kit).getPerks())
				{
					for (Perk perk : arrayOfPerks)
					{
						perk.unregisteredEvents();
						HandlerList.unregisterAll(perk);
					}
				}
			}
			else
			{
				for (Perk perk : kit.GetPerks())
				{
					HandlerList.unregisterAll(perk);
					perk.unregisteredEvents();
				}
			}
			
			
		}
	}

	public void ParseData()
	{
		// Nothing by default,
		// Use this to parse in extra location data from maps
	}

	public boolean loadNecessaryChunks(long maxMilliseconds)
	{
		long endTime = System.currentTimeMillis() + maxMilliseconds;

		int minX = WorldData.MinX >> 4;
		int minZ = WorldData.MinZ >> 4;
		int maxX = WorldData.MaxX >> 4;
		int maxZ = WorldData.MaxZ >> 4;

		for (int x = minX; x <= maxX; x++)
		{
			for (int z = minZ; z <= maxZ; z++)
			{
				if (System.currentTimeMillis() >= endTime)
					return false;

				WorldData.World.getChunkAt(x, z);
			}
		}

		return true;
	}

	public void SetPlayerTeam(Player player, GameTeam team, boolean in)
	{
		// Clean Old Team
		GameTeam pastTeam = this.GetTeam(player);
		if (pastTeam != null)
		{
			pastTeam.RemovePlayer(player);
		}

		team.AddPlayer(player, in);

		// Game Scoreboard
		Scoreboard.setPlayerTeam(player, team);

		// Lobby Scoreboard
		Manager.GetLobby().AddPlayerToScoreboards(player, team);

		// Ensure Valid Kit
		ValidateKit(player, team);
	}

	public TeamSelector getTeamSelector()
	{
		return _teamSelector;
	}

	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		if (DeathOut)
		{
			if (!assist)
			{
				return GemKillDeathOut;
			}
			else
			{
				return GemAssistDeathOut;
			}
		}
		else
		{
			if (!assist)
			{
				return GemKillDeathRespawn;
			}
			else
			{
				return GemAssistDeathRespawn;
			}
		}
	}

	public HashMap<String, GemData> GetGems(Player player)
	{
		if (!_gemCount.containsKey(player))
			_gemCount.put(player, new HashMap<>());

		return _gemCount.get(player);
	}

	public void AddGems(Player player, double gems, String reason, boolean countAmount, boolean multipleAllowed)
	{
		if (!countAmount && gems < 1)
			gems = 1;

		if (GetGems(player).containsKey(reason) && multipleAllowed)
		{
			GetGems(player).get(reason).AddGems(gems);
		}
		else
		{
			GetGems(player).put(reason, new GemData(gems, countAmount));
		}
	}

	public void ValidateKit(Player player, GameTeam team)
	{
		Kit kit = GetKit(player);

		if (kit != null)
		{
			//Make sure their current kit can be used, if not, tell them.
			if (team.KitAllowed(kit))
			{
				return;
			}

			setFirstKit(player, team, true);
		}
		else
		{
			MineplexGameManager gameManager = Manager.getMineplexGameManager();

			for (Kit otherKit : _kits)
			{
				KitAvailability availability = otherKit.GetAvailability();

				if (availability == KitAvailability.Hide || availability == KitAvailability.Null)
				{
					continue;
				}

				if (gameManager.isActive(player, otherKit.getGameKit()) && team.KitAllowed(otherKit))
				{
					SetKit(player, otherKit, true);
					return;
				}
			}

			setFirstKit(player, team, false);
		}
	}

	private void setFirstKit(Player player, GameTeam team, boolean inform)
	{
		if (inform)
		{
			player.sendMessage(F.main("Kit", "Your current kit is not applicable with your team. Please select a different kit."));
		}

		Arrays.stream(_kits).filter(team::KitAllowed).findFirst().ifPresent(kit1 -> SetKit(player, kit1, false));
	}

	public void SetKit(Player player, Kit kit, boolean announce)
	{
		SetKit(player, kit, announce, true);
	}

	public void SetKit(Player player, Kit kit, boolean announce, boolean apply)
	{
		GameTeam team = GetTeam(player);

		if (team != null && !team.KitAllowed(kit))
		{
			if (announce)
			{
				player.playSound(player.getLocation(), Sound.NOTE_BASS, 2f, 0.5f);
				player.sendMessage(F.main("Kit", F.elem(team.GetFormattedName()) + " cannot use " + F.elem(kit.GetFormattedName() + " Kit") + "."));
			}

			return;
		}

		Kit oldKit = GetKit(player);

		if (oldKit != null)
		{
			oldKit.Deselected(player);
		}

		_playerKit.put(player, kit);

		kit.Selected(player);

		if (announce)
		{
			player.playSound(player.getLocation(), Sound.ORB_PICKUP, 2f, 1f);
			UtilPlayer.message(player, F.main("Kit", "You equipped " + F.elem(kit.GetFormattedName() + " Kit") + "."));
		}

		if (InProgress() && apply)
		{
			kit.ApplyKit(player);
		}
		else if (!(kit instanceof ChampionsKit))
		{
			UtilPlayer.closeInventoryIfOpen(player);
		}
	}

	public Kit GetKit(Player player)
	{
		return _playerKit.get(player);
	}

	public Kit[] GetKits()
	{
		return _kits;
	}

	public boolean HasKit(Kit kit)
	{
		for (Kit cur : GetKits())
			if (cur.equals(kit))
				return true;

		return false;
	}

	public boolean HasKit(Player player, Kit kit)
	{
		Kit playerKit = GetKit(player);

		return IsAlive(player) && playerKit != null && playerKit.equals(kit);
	}

	public void disqualify(Player player)
	{
		_teamModule.getPreferences().remove(player);
		GetPlayerKits().remove(player);
		GetPlayerGems().remove(player);

		//Remove Team
		GameTeam team = GetTeam(player);
		if (team != null)
		{
			if (InProgress())
			{
				SetPlayerState(player, PlayerState.OUT);
			}
			else
			{
				team.RemovePlayer(player);
			}
		}

		Manager.addSpectator(player, false);
	}

	public boolean SetPlayerState(Player player, PlayerState state)
	{
		GameTeam team = GetTeam(player);

		if (team == null)
		{
			return false;
		}

		team.SetPlayerState(player, state);

		// Event
		UtilServer.CallEvent(new PlayerStateChangeEvent(this, player, state));

		return true;
	}

	public abstract void EndCheck();

	public void RespawnPlayer(final Player player)
	{
		player.eject();
		RespawnPlayerTeleport(player);

		Manager.Clear(player);

		// Event
		PlayerGameRespawnEvent event = new PlayerGameRespawnEvent(this, player);
		UtilServer.getServer().getPluginManager().callEvent(event);

		// Re-set player gamemode
		player.setGameMode(PlayerGameMode);

		// Re-Give Kit
		Manager.runSyncLater(() -> GetKit(player).ApplyKit(player), 0);
	}

	public void RespawnPlayerTeleport(Player player)
	{
		player.teleport(GetTeam(player).GetSpawn());
	}

	public boolean IsPlaying(Player player)
	{
		return GetTeam(player) != null;
	}

	public boolean IsAlive(Entity entity)
	{
		if (entity instanceof Player)
		{
			Player player = (Player) entity;

			GameTeam team = GetTeam(player);

			return team != null && team.IsAlive(player);
		}

		return false;
	}

	public boolean shouldHeal(Player player)
	{
		return true;
	}

	public ArrayList<Player> GetPlayers(boolean aliveOnly)
	{
		ArrayList<Player> players = new ArrayList<Player>();

		for (GameTeam team : _teamList)
			players.addAll(team.GetPlayers(aliveOnly));

		return players;
	}

	public GameTeam GetTeam(String player, boolean aliveOnly)
	{
		for (GameTeam team : _teamList)
			if (team.HasPlayer(player, aliveOnly))
				return team;

		return null;
	}

	public GameTeam GetTeam(Player player)
	{
		if (player == null)
			return null;

		for (GameTeam team : _teamList)
			if (team.HasPlayer(player))
				return team;

		return null;
	}

	public GameTeam GetTeam(ChatColor color)
	{
		for (GameTeam team : _teamList)
			if (team.GetColor() == color)
				return team;

		return null;
	}

	public Location GetSpectatorLocation()
	{
		if (SpectatorSpawn != null)
			return SpectatorSpawn;

		Vector vec = new Vector(0, 0, 0);
		double count = 0;

		for (GameTeam team : this.GetTeamList())
		{
			for (Location spawn : team.GetSpawns())
			{
				count++;
				vec.add(spawn.toVector());
			}
		}

		SpectatorSpawn = new Location(this.WorldData.World, 0, 0, 0);

		vec.multiply(1d / count);

		SpectatorSpawn.setX(vec.getX());
		SpectatorSpawn.setY(vec.getY());
		SpectatorSpawn.setZ(vec.getZ());

		// Move Up - Out Of Blocks
		while (!UtilBlock.airFoliage(SpectatorSpawn.getBlock())
				|| !UtilBlock.airFoliage(SpectatorSpawn.getBlock().getRelative(BlockFace.UP)))
		{
			SpectatorSpawn.add(0, 1, 0);
		}

		int Up = 0;

		// Move Up - Through Air
		for (int i = 0; i < 15; i++)
		{
			if (UtilBlock.airFoliage(SpectatorSpawn.getBlock().getRelative(BlockFace.UP)))
			{
				SpectatorSpawn.add(0, 1, 0);
				Up++;
			}
			else
			{
				break;
			}
		}

		// Move Down - Out Of Blocks
		while (Up > 0 && !UtilBlock.airFoliage(SpectatorSpawn.getBlock())
				|| !UtilBlock.airFoliage(SpectatorSpawn.getBlock().getRelative(BlockFace.UP)))
		{
			SpectatorSpawn.subtract(0, 1, 0);
			Up--;
		}

		SpectatorSpawn = SpectatorSpawn.getBlock().getLocation().add(0.5, 0.1, 0.5);

		while (SpectatorSpawn.getBlock().getTypeId() != 0 || SpectatorSpawn.getBlock().getRelative(BlockFace.UP).getTypeId() != 0)
			SpectatorSpawn.add(0, 1, 0);

		return SpectatorSpawn;
	}

	@EventHandler
	public abstract void ScoreboardUpdate(UpdateEvent event);

	public DeathMessageType GetDeathMessageType()
	{
		if (!DeathMessages)
			return DeathMessageType.None;

		if (this.DeathOut)
			return DeathMessageType.Detailed;

		return DeathMessageType.Simple;
	}

	@EventHandler
	public final void onFoodLevelChangeEvent(FoodLevelChangeEvent event)
	{
		((Player) event.getEntity()).setSaturation(5); // While not entirely accurate, this is a pretty good guess at original
		// food level changes
	}

	public void AnnounceGame()
	{
		for (Player player : UtilServer.getPlayers())
			AnnounceGame(player);

		if (AnnounceSilence)
			Manager.GetChat().setChatSilence(PrepareTime, false);
	}

	public void AnnounceGame(Player player)
	{
		player.playSound(player.getLocation(), Sound.LEVEL_UP, 2f, 1f);

		for (int i = 0; i < 6 - GetDesc().length; i++)
			UtilPlayer.message(player, "");

		UtilPlayer.message(player, ArcadeFormat.Line);

		UtilPlayer.message(player, C.cGreen + "Game - " + C.cYellowB + GetName());
		UtilPlayer.message(player, "");

		for (String line : this.GetDesc())
		{
			UtilPlayer.message(player, C.cWhite + "  " + line);
		}

		UtilPlayer.message(player, "");
		UtilPlayer.message(player, WorldData.getFormattedName());

		UtilPlayer.message(player, ArcadeFormat.Line);
	}

	public void AnnounceEnd(GameTeam team)
	{
		if (!IsLive())
			return;

		if (WinEffectEnabled && team != null && !team.GetPlacements(true).isEmpty())
		{
			Map<Player, Integer> teamGemCount = new HashMap<>();

			List<Player> teamList = team.GetPlacements(true).stream()
					.filter((p) -> !getArcadeManager().isVanished(p))
					// Get the gem count for each member of the team,
					// and save it to the HashMap, so it can be sorted
					.peek((p) ->
					{
						Map<String, GemData> gemDataMap = GetGems(p);

						int gemCount = 0;

						for (GemData data : gemDataMap.values())
						{
							gemCount += data.Gems;
						}

						teamGemCount.put(p, gemCount);
					})
					// Sort based on gem count in descending order
					.sorted((p1, p2) -> teamGemCount.get(p2) - teamGemCount.get(p1))
					.collect(Collectors.toList());

			List<Player> otherList = UtilServer.getPlayersCollection()
					.stream()
					.filter((p) -> !getArcadeManager().isVanished(p) && !teamList.contains(p))
					.collect(Collectors.toList());

			// This sets the "winner" player as the player with the
            // most gems, since it's been previously sorted as such
			Player player = teamList.remove(0);

			WinEffectManager.prePlay(this, player, teamList, otherList);

			Location loc = GetSpectatorLocation().clone().add(1000, 0, 1000);
			loc.setY(200);
			WinEffectManager.playWinEffect(loc);
		}

		String winnerText = ChatColor.WHITE + "Nobody";
		ChatColor subColor = ChatColor.WHITE;

		for (Player player : UtilServer.getPlayers())
		{
			player.playSound(player.getLocation(), Sound.LEVEL_UP, 2f, 1f);

			UtilPlayer.message(player, "");
			UtilPlayer.message(player, ArcadeFormat.Line);

			UtilPlayer.message(player, C.cGreen + "Game - " + C.cYellowB + GetName());
			UtilPlayer.message(player, "");
			UtilPlayer.message(player, "");

			if (team != null)
			{
				WinnerTeam = team;
				Winner = team.GetName() + " Team";

				winnerText = team.GetColor() + team.GetName();
				subColor = team.GetColor();

				UtilPlayer.message(player, subColor + C.Bold + team.GetName() + " won the game!");
			}
			else
			{
				UtilPlayer.message(player, "Nobody won the game!");
			}

			if (_customWinMessages.containsKey(player))
			{
				if (!_customWinLine.trim().equalsIgnoreCase(""))
				{
					UtilPlayer.message(player, _customWinLine);
				}

				UtilPlayer.message(player, _customWinMessages.get(player));
			}
			else
			{
				UtilPlayer.message(player, _customWinLine);
			}

			UtilPlayer.message(player, "");
			UtilPlayer.message(player, WorldData.getFormattedName());

			UtilPlayer.message(player, ArcadeFormat.Line);
		}

		UtilTextMiddle.display(winnerText, subColor + "won the game", 20, 120, 20);

		if (AnnounceSilence)
			Manager.GetChat().setChatSilence(5000, false);

		endElo();
	}

	public void AnnounceEnd(List<Player> places)
	{
		String winnerText = ChatColor.WHITE + "§lNobody won the game...";
		ChatColor subColor = ChatColor.WHITE;

		if (WinEffectEnabled && places != null && !places.isEmpty())
		{
			List<Player> teamList = new ArrayList<>();
			List<Player> nonTeamList = new ArrayList<>(places);
			Player player = places.get(0);
			nonTeamList.remove(player);

			WinEffectManager.prePlay(this, player, teamList, nonTeamList);

			Location loc = GetSpectatorLocation().clone().add(1000, 0, 1000);
			loc.setY(200);
			WinEffectManager.playWinEffect(loc);
		}

		for (Player player : UtilServer.getPlayers())
		{
			player.playSound(player.getLocation(), Sound.LEVEL_UP, 2f, 1f);

			UtilPlayer.message(player, "");
			UtilPlayer.message(player, ArcadeFormat.Line);

			UtilPlayer.message(player, C.cGreen + "Game - " + C.cYellowB + GetName());
			UtilPlayer.message(player, "");

			if (places == null || places.isEmpty())
			{
				UtilPlayer.message(player, "");
				UtilPlayer.message(player, ChatColor.WHITE + "§lNobody won the game...");
				UtilPlayer.message(player, "");
			}
			else
			{
				if (places.size() >= 1)
				{
					Winner = places.get(0).getName();

					winnerText = C.cYellow + places.get(0).getName();
					subColor = ChatColor.YELLOW;

					UtilPlayer.message(player, C.cRed + C.Bold + "1st Place" + C.cWhite + " - " + places.get(0).getName());
				}

				if (places.size() >= 2)
				{
					UtilPlayer.message(player, C.cGold + C.Bold + "2nd Place" + C.cWhite + " - " + places.get(1).getName());
				}

				if (places.size() >= 3)
				{
					UtilPlayer.message(player, C.cYellow + C.Bold + "3rd Place" + C.cWhite + " - " + places.get(2).getName());
				}
			}

			UtilPlayer.message(player, "");
			UtilPlayer.message(player, WorldData.getFormattedName());

			UtilPlayer.message(player, ArcadeFormat.Line);
		}

		UtilTextMiddle.display(winnerText, subColor + "won the game", 20, 120, 20);

		if (AnnounceSilence)
			Manager.GetChat().setChatSilence(5000, false);

		endElo();
	}

	public void Announce(String message)
	{
		if (message == null)
			return;

		Announce(message, true);
	}

	public void Announce(String message, boolean playSound)
	{
		for (Player player : UtilServer.getPlayers())
		{
			if (playSound)
				player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 1f);

			UtilPlayer.message(player, message);
		}

		System.out.println("[Announcement] " + message);
	}

	public boolean AdvertiseText(LobbyManager gameLobbyManager, int _advertiseStage)
	{
		return false;
	}

	public boolean CanThrowTNT(Location location)
	{
		return true;
	}

	@EventHandler
	public void HelpUpdate(UpdateEvent event)
	{
		if (_help == null || _help.length == 0 || event.getType() != UpdateType.SLOWER || !inLobby())
		{
			return;
		}

		if (Manager.GetGameHostManager().isCommunityServer())
		{
			return;
		}

		_helpColor = _helpColor == ChatColor.YELLOW ? ChatColor.GOLD : ChatColor.YELLOW;

		String msg = C.cWhiteB+ "TIP> " + ChatColor.RESET + _helpColor + _help[_helpIndex];

		for (Player player : UtilServer.getPlayersCollection())
		{
			if (!Manager.getPreferences().get(player).isActive(Preference.GAME_TIPS))
			{
				continue;
			}

			player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1f, 1f);
			UtilPlayer.message(player, msg);
		}

		_helpIndex = (_helpIndex + 1) % _help.length;
	}

	public void StartPrepareCountdown()
	{
		_prepareCountdown = true;
	}

	public boolean CanStartPrepareCountdown()
	{
		return _prepareCountdown;
	}

	@EventHandler
	public void TeamPlayerPlacement(PlayerStateChangeEvent event)
	{
		GameTeam team = GetTeam(event.GetPlayer());

		if (team != null)
			team.SetPlacement(event.GetPlayer(), event.GetState());
	}

	public void HandleTimeout()
	{
		SetState(GameState.End);
	}

	public void AddStat(Player player, String stat, int amount, boolean limitTo1, boolean global)
	{
		if (!Manager.IsRewardStats())
			return;

		_stats.computeIfAbsent(player, k -> new HashMap<>());

		if (global)
		{
			stat = "Global." + stat;
		}
		else
		{
			// In certain game modes (for example OP Bridges) we don't want to award game stats but global ones like EXP are fine.
			if (!isAllowingGameStats())
			{
				return;
			}

			stat = GetName() + "." + stat;
		}

		if (Manager.IsTournamentServer())
			stat += ".Tournament";

		int past = 0;
		if (_stats.get(player).containsKey(stat))
			past = _stats.get(player).get(stat);

		_stats.get(player).put(stat, limitTo1 ? Math.min(1, past + amount) : past + amount);
	}

	public abstract List<Player> getWinners();

	public abstract List<Player> getLosers();

	public Map<Player, Map<String, Integer>> GetStats()
	{
		return _stats;
	}

	public void registerStatTrackers(StatTracker<? extends Game>... statTrackers)
	{
		for (StatTracker<? extends Game> tracker : statTrackers)
		{
			if (_statTrackers.add(tracker))
				Bukkit.getPluginManager().registerEvents(tracker, Manager.getPlugin());
		}
	}

	public void registerChatStats(ChatStatData... stats)
	{
		Manager.getGameChatManager().setGameChatStats(stats);
	}

	public Collection<StatTracker<? extends Game>> getStatTrackers()
	{
		return _statTrackers;
	}

	public void registerMissions(MissionTracker... trackers)
	{
		getArcadeManager().getMissionsManager().registerTrackers(trackers);
	}

	@EventHandler
	public void onHangingBreak(HangingBreakEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void onHangingPlace(HangingPlaceEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void onDamageHanging(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Hanging)
		{
			event.setCancelled(true);
		}
	}

	public void deRegisterStats()
	{
		for (StatTracker<? extends Game> tracker : _statTrackers)
			HandlerList.unregisterAll(tracker);

		_statTrackers.clear();
	}

	public ArcadeManager getArcadeManager()
	{
		return Manager;
	}

	@EventHandler
	public void classCombatCreatureAllow(ClassCombatCreatureAllowSpawnEvent event)
	{
		CreatureAllowOverride = event.getAllowed();
	}

	public boolean isInsideMap(Player player)
	{
		return isInsideMap(player.getLocation());
	}

	public boolean isInsideMap(Location loc)
	{
		return !(loc.getX() >= WorldData.MaxX + 1 || loc.getX() <= WorldData.MinX || loc.getZ() >= WorldData.MaxZ + 1
				|| loc.getZ() <= WorldData.MinZ || loc.getY() >= WorldData.MaxY + 1 || loc.getY() <= WorldData.MinY);
	}

	public void setItemMerge(boolean itemMerge)
	{
		setItemMergeRadius(itemMerge ? 3.5 : 0);
	}

	public void setItemMergeRadius(double mergeRadius)
	{
		_itemMergeRadius = mergeRadius;

		if (WorldData.World != null)
		{
			((CraftWorld) WorldData.World).getHandle().spigotConfig.itemMerge = _itemMergeRadius;
		}
	}

	public double getItemMergeRadius()
	{
		return _itemMergeRadius;
	}

	@EventHandler
	public void applyItemMerge(WorldLoadEvent event)
	{
		if (event.getWorld().getName().equals(WorldData.GetFolder()))
		{
			System.out.println("Setting item merge radius for game to " + _itemMergeRadius);
			((CraftWorld) event.getWorld()).getHandle().spigotConfig.itemMerge = _itemMergeRadius;
		}
	}

	public void setGame(GameType gameType, Player caller, boolean inform)
	{
		Manager.GetGameCreationManager().setNextGameType(gameType);

		// End Current
		if (inLobby())
		{
			SetState(GameState.Dead);

			if (inform)
				Announce(C.cAquaB + caller.getName() + " has changed game to " + gameType.getName() + ".");
		}
		else
		{
			if (inform)
				Announce(C.cAquaB + caller.getName() + " set next game to " + gameType.getName() + ".");
		}
	}

	public void endGame(GameTeam winningTeam)
	{
		AnnounceEnd(winningTeam);

		for (GameTeam team : GetTeamList())
		{
			if (WinnerTeam != null && team.equals(WinnerTeam))
			{
				for (Player player : team.GetPlayers(false))
					AddGems(player, 10, "Winning Team", false, false);
			}

			for (Player player : team.GetPlayers(false))
				if (player.isOnline())
					AddGems(player, 10, "Participation", false, false);
		}

		endElo();

		// End
		SetState(GameState.End);
	}

	@EventHandler
	public void onTeleportPrepare(PlayerPrepareTeleportEvent event)
	{
		event.GetPlayer().setGameMode(PlayerGameMode);
	}

	@EventHandler
	public void onGameStart(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Live)
		{
			if (EloRanking)
			{
				// Populate teams
				for (GameTeam team : GetTeamList())
				{
					EloTeam eloTeam = new EloTeam();

					for (Player player : team.GetPlayers(false))
					{
						eloTeam.addPlayer(new EloPlayer(player, Manager.GetClients().getAccountId(player), Manager.getEloManager().getElo(player, GetType().getGameId())));
					}

					Manager.getEloManager().addTeam(team.getDisplayName(), eloTeam);
				}
			}
		}
	}

	// Handle Elo at end of game -- method can be overridden in different game modes to meet their individual needs
	protected void endElo()
	{
		if (EloRanking)
		{
			if (WinnerTeam != null)
				Manager.getEloManager().setWinningTeam(WinnerTeam.getDisplayName());

			Manager.getEloManager().endMatch(GetType().getGameId());
		}
	}


	@EventHandler
	public void handleInteractEntityPacket(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Live)
		{
			getArcadeManager().getPacketHandler().addPacketHandler(_useEntityPacketHandler, PacketPlayInUseEntity.class);
		}
		else if (event.GetState() == GameState.Dead)
		{
			getArcadeManager().getPacketHandler().removePacketHandler(_useEntityPacketHandler);
		}
	}

	@EventHandler
	public void onDeadBodyDeath(CombatDeathEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (!(event.GetEvent().getEntity() instanceof Player))
			return;

		if (!DeadBodiesDeath)
		{
			return;
		}

		spawnDeadBody((Player) event.GetEvent().getEntity());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onDeadBodyQuit(PlayerQuitEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (!IsAlive(event.getPlayer()))
		{
			return;
		}

		if (!DeadBodiesQuit)
		{
			return;
		}

		spawnDeadBody(event.getPlayer());
	}

	private void spawnDeadBody(Player player)
	{
		if (!DeadBodies)
		{
			return;
		}

		Location loc = player.getLocation();
		String name = "Body #" + ++_deadBodyCount;

		if (UseCustomScoreboard)
		{
			for (Player other : UtilServer.getPlayersCollection())
			{
				setupDeadBodyScoreboard(other.getScoreboard(), name);
			}
		}
		else
		{
			setupDeadBodyScoreboard(Scoreboard.getScoreboard(), name);
		}

		EntityItem entityItem = new EntityItem(
				((CraftWorld) loc.getWorld()).getHandle(),
				loc.getX(),
				loc.getY() + 0.5,
				loc.getZ(),
				CraftItemStack.asNMSCopy(new ItemBuilder(Material.STONE).setTitle(System.currentTimeMillis() + "").build())
		);
		entityItem.pickupDelay = Integer.MAX_VALUE;
		entityItem.yaw = player.getLocation().getYaw();
		entityItem.pitch = player.getLocation().getPitch();

		UtilEnt.CreatureLook(entityItem.getBukkitEntity(), player.getLocation().getPitch(), player.getLocation().getYaw());

		GameProfile profile = UtilGameProfile.getGameProfile(player);

		GameProfile cloned = new GameProfile(UUID.randomUUID(), name);
		cloned.getProperties().putAll(profile.getProperties());

		DisguisePlayer disguise = new DisguisePlayer(entityItem.getBukkitEntity(), cloned);
		disguise.setSleeping(getSleepingFace(player.getLocation()));

		_deadBodies.put(player.getName(), entityItem.getBukkitEntity());

		if (DeadBodiesExpire > 0)
		{
			_deadBodiesExpire.put(player.getName(), System.currentTimeMillis() + (DeadBodiesExpire * 1000));
		}

		getArcadeManager().GetDisguise().disguise(disguise);
	}

	private void setupDeadBodyScoreboard(Scoreboard scoreboard, String name)
	{
		Team team = scoreboard.getTeam(ChatColor.COLOR_CHAR + "DeadBodies");

		if (team == null)
		{
			team = scoreboard.registerNewTeam(ChatColor.COLOR_CHAR + "DeadBodies");
			team.setNameTagVisibility(NameTagVisibility.NEVER);
		}

		team.addEntry(name);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDeadBodyItemSpawn(ItemSpawnEvent event)
	{
		if (_deadBodies.containsValue(event.getEntity()))
		{
			event.setCancelled(false);

			((CraftEntity) event.getEntity()).getHandle().dead = false;
		}
	}

	public NautHashMap<String, Entity> getDeadBodies()
	{
		return _deadBodies;
	}

	public void cleanDeadBodies()
	{
		for (Entity entity : _deadBodies.values())
		{
			entity.remove();
		}

		_deadBodies.clear();
		_deadBodiesExpire.clear();
		_deadBodyCount = 0;
	}

	private BlockFace getSleepingFace(Location loc)
	{
		Block block = loc.getBlock();

		while (block.getY() > 0 && !UtilBlock.fullSolid(block.getRelative(BlockFace.DOWN))
				&& !UtilBlock.solid(block.getRelative(BlockFace.DOWN)))
		{
			block = block.getRelative(BlockFace.DOWN);
		}

		BlockFace proper = BlockFace.values()[Math.round(loc.getYaw() / 90F) & 0x3].getOppositeFace();

		// A complicated way to get the face the dead body should be towards.
		for (HashSet<Byte> validBlocks : new HashSet[]
				{
						UtilBlock.blockAirFoliageSet,
						UtilBlock.blockPassSet
				})
		{

			if (validBlocks.contains((byte) block.getRelative(proper).getTypeId()))
			{
				return proper;
			}

			for (BlockFace face : new BlockFace[]
					{
							BlockFace.EAST,
							BlockFace.SOUTH,
							BlockFace.NORTH,
							BlockFace.WEST
					})
			{
				if (validBlocks.contains((byte) block.getRelative(face).getTypeId()))
				{
					return face;
				}
			}
		}

		return proper;
	}

	@EventHandler
	public void onDeadBodiesExpire(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		Iterator<Entry<String, Long>> itel = _deadBodiesExpire.entrySet().iterator();

		while (itel.hasNext())
		{
			Entry<String, Long> entry = itel.next();

			if (entry.getValue() < System.currentTimeMillis())
			{
				if (_deadBodies.containsKey(entry.getKey()))
				{
					_deadBodies.remove(entry.getKey()).remove();
				}

				itel.remove();
			}
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		if (!IsLive())
			return;

		if (!JoinInProgress)
			return;

		_playerInTime.put(event.getPlayer(), System.currentTimeMillis());
	}

	public long getPlayerIngameTime(Player player)
	{
		if (_playerInTime.containsKey(player))
			return _playerInTime.get(player);

		return 0;
	}

	public void addPlayerInTime(Player player)
	{
		_playerInTime.put(player, System.currentTimeMillis());
	}

	public void addTutorials()
	{
	}

	public void disable()
	{
		cleanupModules();
		cleanupCommands();
		Managers.get(AntiHack.class).resetIgnoredChecks();
		Managers.get(AntiHack.class).setStrict(false);
		getLifetime().end();
		getStatTrackers().forEach(HandlerList::unregisterAll);
		getArcadeManager().getMissionsManager().clearTrackers(tracker -> tracker instanceof GameMissionTracker);
	}
	
	@EventHandler
	public void registerLocs(UpdateEvent event)
	{
		if (!SpeedMeasurement)
			return;
		
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Player player : GetPlayers(true))
		{
			if (!_playerPastLocs.containsKey(player.getUniqueId()))
				_playerPastLocs.put(player.getUniqueId(), new LinkedList<>());
			
			LinkedList<Triple<Double, Double, Double>> locList = _playerPastLocs.get(player.getUniqueId());
			locList.add(Triple.of(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));
			
			if (locList.size() > MAX_TICK_SPEED_MEASUREMENT)
				locList.removeFirst();
		}
	}
	
	/**
	 * The players Location while the specified amount of ticks ago
	 * 
	 * @param player to check
	 * @param ticksAgo amount of time ago
	 * @return Location of the specified time
	 */
	public Location getPastLocation(Player player, int ticksAgo)
	{
		if (!SpeedMeasurement)
		{
			throw new IllegalStateException("Speed Measurements are not enabled");
		}
		
		if (!IsAlive(player))
		{
			throw new IllegalArgumentException("this only works for ingame players");
		}
		
		if (ticksAgo > MAX_TICK_SPEED_MEASUREMENT)
		{
			throw new IllegalArgumentException("ticksAgo needs to be 1 - 40");
		}
		
		if (!_playerPastLocs.containsKey(player.getUniqueId()))
			return player.getLocation();
		
		int tick = MAX_TICK_SPEED_MEASUREMENT - ticksAgo;
		
		LinkedList<Triple<Double, Double, Double>> pastLocs = _playerPastLocs.get(player.getUniqueId());
		if (pastLocs.size() < tick)
			return player.getLocation();
		
		Triple<Double, Double, Double> triple = _playerPastLocs.get(player.getUniqueId()).get(tick);
		return new Location(player.getWorld(), triple.getLeft(), triple.getMiddle(), triple.getRight(), player.getLocation().getYaw(), player.getLocation().getPitch());
	}

	/**
	 * The Vector of movement for the specified time
	 * 
	 * @param player to check
	 * @param ticksAgo amount of time ago
	 * @return vector of movement
	 */
	public Vector getMovement(Player player, int ticksAgo)
	{
		Location past = getPastLocation(player, ticksAgo);
		return player.getLocation().toVector().subtract(past.clone().toVector());
	}
	
	/**
	 * The amount of blocks moved for the specified time
	 * 
	 * @param player
	 * @param ticksAgo amount of time ago
	 * @return blocks moved
	 */
	public double getSpeed(Player player, int ticksAgo)
	{
		Location past = getPastLocation(player, ticksAgo);
		return UtilMath.offset(past, player.getLocation());
	}
	
	public void cleanupModules()
	{
		for (Module module : this._modules.values())
		{
			module.cleanup();
			HandlerList.unregisterAll(module);
		}
		this._modules.clear();
	}

	public void cleanupCommands()
	{
		_debugCommands.forEach(command -> CommandCenter.Instance.removeCommand(command));
		_debugCommands.clear();
	}

	public <T extends Module> T getModule(Class<T> clazz)
	{
		return clazz.cast(_modules.get(clazz));
	}

	@Override
	public PhasedLifetime<GameState> getLifetime()
	{
		return _lifetime;
	}
}
