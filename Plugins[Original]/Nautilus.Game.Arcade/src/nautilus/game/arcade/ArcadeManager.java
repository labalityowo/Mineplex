package nautilus.game.arcade;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.server.v1_8_R3.EntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import mineplex.core.Managers;
import mineplex.core.MiniPlugin;
import mineplex.core.PlayerSelector;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.admin.command.AdminCommands;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.blood.Blood;
import mineplex.core.bonuses.BonusManager;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.chat.Chat;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.timing.TimingManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilLambda;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.communities.CommunityManager;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.creature.Creature;
import mineplex.core.customdata.CustomDataManager;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.disguise.playerdisguise.PlayerDisguiseManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.elo.EloManager;
import mineplex.core.energy.Energy;
import mineplex.core.event.JoinMessageBroadcastEvent;
import mineplex.core.events.AddConditionEvent;
import mineplex.core.events.EnableArcadeSpawnEvent;
import mineplex.core.explosion.Explosion;
import mineplex.core.explosion.ExplosionEvent;
import mineplex.core.gadget.event.GadgetEnableEvent;
import mineplex.core.gadget.event.ToggleMobsEvent;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.game.GameDisplay;
import mineplex.core.game.MineplexGameManager;
import mineplex.core.game.status.GameInfo;
import mineplex.core.game.status.GameInfo.GameDisplayStatus;
import mineplex.core.game.status.GameInfo.GameJoinStatus;
import mineplex.core.game.winstreaks.WinStreakManager;
import mineplex.core.google.GoogleSheetsManager;
import mineplex.core.hologram.HologramManager;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.incognito.events.IncognitoStatusChangeEvent;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.menu.MenuManager;
import mineplex.core.mission.MissionManager;
import mineplex.core.movement.Movement;
import mineplex.core.npc.NpcManager;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.party.PartyManager;
import mineplex.core.party.event.PartySelectServerEvent;
import mineplex.core.personalServer.PersonalServerManager;
import mineplex.core.pet.PetManager;
import mineplex.core.poll.PollManager;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.preferences.UserPreferences;
import mineplex.core.progression.KitProgressionManager;
import mineplex.core.projectile.ProjectileManager;
import mineplex.core.punish.Punish;
import mineplex.core.rankGiveaway.eternal.EternalGiveawayManager;
import mineplex.core.rankGiveaway.titangiveaway.TitanGiveawayManager;
import mineplex.core.resourcepack.ResourcePackManager;
import mineplex.core.scoreboard.MineplexScoreboard;
import mineplex.core.scoreboard.ScoreboardManager;
import mineplex.core.stats.StatsManager;
import mineplex.core.status.ServerStatusManager;
import mineplex.core.task.TaskManager;
import mineplex.core.teleport.Teleport;
import mineplex.core.thank.ThankManager;
import mineplex.core.titles.Titles;
import mineplex.core.titles.tracks.TrackManager;
import mineplex.core.youtube.YoutubeManager;
import mineplex.minecraft.game.classcombat.Class.ClassManager;
import mineplex.minecraft.game.classcombat.Condition.SkillConditionManager;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;
import mineplex.minecraft.game.classcombat.item.ItemFactory;
import mineplex.minecraft.game.classcombat.item.event.ItemTriggerEvent;
import mineplex.minecraft.game.classcombat.shop.ClassCombatShop;
import mineplex.minecraft.game.classcombat.shop.ClassShopManager;
import mineplex.minecraft.game.core.IRelation;
import mineplex.minecraft.game.core.combat.event.CombatQuitEvent;
import mineplex.minecraft.game.core.condition.Condition;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;
import mineplex.minecraft.game.core.fire.Fire;

import nautilus.game.arcade.addons.SoupAddon;
import nautilus.game.arcade.booster.GameBoosterManager;
import nautilus.game.arcade.command.CancelNextGameCommand;
import nautilus.game.arcade.command.GameCommand;
import nautilus.game.arcade.command.GoToNextGameCommand;
import nautilus.game.arcade.command.KitUnlockCommand;
import nautilus.game.arcade.command.MapCommand;
import nautilus.game.arcade.command.ReturnToHubCommand;
import nautilus.game.arcade.command.SpectatorCommand;
import nautilus.game.arcade.command.TauntCommand;
import nautilus.game.arcade.command.TournamentStopCommand;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameServerConfig;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.event.EventModule;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.GameAchievementManager;
import nautilus.game.arcade.managers.GameCreationManager;
import nautilus.game.arcade.managers.GameFlagManager;
import nautilus.game.arcade.managers.GameHostManager;
import nautilus.game.arcade.managers.GameLootManager;
import nautilus.game.arcade.managers.GameManager;
import nautilus.game.arcade.managers.GamePlayerManager;
import nautilus.game.arcade.managers.GameRewardManager;
import nautilus.game.arcade.managers.GameSpectatorManager;
import nautilus.game.arcade.managers.GameStatManager;
import nautilus.game.arcade.managers.GameWorldManager;
import nautilus.game.arcade.managers.HubClockManager;
import nautilus.game.arcade.managers.IdleManager;
import nautilus.game.arcade.managers.NextBestGameManager;
import nautilus.game.arcade.managers.ServerUptimeManager;
import nautilus.game.arcade.managers.chat.GameChatManager;
import nautilus.game.arcade.managers.lobby.LobbyManager;
import nautilus.game.arcade.managers.lobby.current.NewGameLobbyManager;
import nautilus.game.arcade.managers.lobby.legacy.LegacyGameLobbyManager;
import nautilus.game.arcade.managers.voting.Vote;
import nautilus.game.arcade.managers.voting.Voteable;

public class ArcadeManager extends MiniPlugin implements IRelation
{
	public enum Perm implements Permission
	{
		USE_MENU_DURING_GAME,
		NEXT_BEST_GAME,
		KIT_UNLOCK_COMMAND,
		TAUNT_COMMAND,
		GAME_COMMAND,
		AUTO_OP,
		FEATURED_SERVER,
		INFORM_RANKED_MODERATION_POTENTIAL,
		JOIN_FULL,
		JOIN_FULL_STAFF,
		BYPASS_WHITELIST,
		BYPASS_MPS_WHITELIST,
		RETURN_TO_HUB_COMMAND,
		TOURNAMENT_STOP_COMMAND,
		SPECTATOR_COMMAND,
		MAP_COMMAND
	}

	// Modules
	private BlockRestore _blockRestore;
	private Blood _blood;
	private Chat _chat;
	private CoreClientManager _clientManager;
	private DisguiseManager _disguiseManager;
	private DonationManager _donationManager;
	private ConditionManager _conditionManager;
	private PetManager _petManager;
	private Creature _creature;
	private DamageManager _damageManager;
	private Explosion _explosionManager;
	private EventModule _eventManager;

	private Fire _fire;
	private ProjectileManager _projectileManager;

	private Portal _portal;

	//Champions Modules
	private boolean _enabled = true;
	private ClassManager _classManager;
	private SkillFactory _skillFactory;
	private ItemFactory _itemFactory;
	private Energy _energy;
	private ClassShopManager _classShopManager;
	private ClassCombatShop _classShop;
	private EloManager _eloManager;

	// Managers
	private GameCreationManager _gameCreationManager;
	private GameRewardManager _gameRewardManager;
	private GameManager _gameManager;
	private LobbyManager _gameLobbyManager;
	private GamePlayerManager _gamePlayerManager;
	private GameWorldManager _gameWorldManager;
	private final GameHostManager _gameHostManager;
	private GameChatManager _gameChatManager;
	private ServerStatusManager _serverStatusManager;
	private InventoryManager _inventoryManager;
	private CosmeticManager _cosmeticManager;
	private final IdleManager _idleManager;
	private HologramManager _hologramManager;
	private AchievementManager _achievementManager;
	private StatsManager _statsManager;
	private PartyManager _partyManager;
	private PreferencesManager _preferencesManager;
	private ResourcePackManager _resourcePackManager;
	private CustomDataManager _customDataManager;
	private Punish _punishmentManager;
	private BonusManager _bonusManager;
	private KitProgressionManager _kitProgressionManager;
	private BoosterManager _boosterManager;
	private GameSpectatorManager _spectatorManager;
	private ServerUptimeManager _serverUptimeManager;
	private ScoreboardManager _scoreboardManager;
	private NextBestGameManager _nextBestGameManager;
	private TrackManager _trackManager;
	private final MissionManager _missionsManager;
	private GoogleSheetsManager _sheetsManager;
	private IncognitoManager _incognitoManager;
	private WinStreakManager _winStreakManager;
	private MineplexGameManager _mineplexGameManager;

	private TaskManager _taskManager;
	private PacketHandler _packetHandler;

	// Observers
	private Set<Player> _specList = new HashSet<>();

	// Server Games
	private GameServerConfig _serverConfig;

	// Games
	private Game _game;

	//Server Property

	private final Titles _titles;

	public ArcadeManager(Arcade plugin, ServerStatusManager serverStatusManager, GameServerConfig serverConfig,
						 CoreClientManager clientManager, DonationManager donationManager, DamageManager damageManager,
						 StatsManager statsManager, IncognitoManager incognitoManager, AchievementManager achievementManager, DisguiseManager disguiseManager, Creature creature, Teleport teleport, Blood blood, Chat chat,
						 Portal portal, PreferencesManager preferences, InventoryManager inventoryManager, PacketHandler packetHandler,
						 CosmeticManager cosmeticManager, ProjectileManager projectileManager, PetManager petManager, HologramManager hologramManager, PollManager pollManager,
						 NpcManager npcManager, CustomDataManager customDataManager, Punish punish, EloManager eloManager, ThankManager thankManager, BoosterManager boosterManager)
	{
		super("Game", plugin);

		_serverConfig = serverConfig;

		// Modules
		_blockRestore = Managers.get(BlockRestore.class);

		_incognitoManager = incognitoManager;

		_blood = blood;
		_preferencesManager = preferences;

		_explosionManager = new Explosion(plugin, _blockRestore);
		_explosionManager.SetDebris(false);

		_conditionManager = new SkillConditionManager(plugin);

		_boosterManager = boosterManager;

		_clientManager = clientManager;
		_serverStatusManager = serverStatusManager;
		_chat = chat;
		_creature = creature;

		_damageManager = damageManager;
		_damageManager.UseSimpleWeaponDamage = true;
		_damageManager.setConditionManager(_conditionManager);
		_conditionManager.setDamageManager(_damageManager);

		_disguiseManager = disguiseManager;

		_donationManager = donationManager;

		_fire = new Fire(plugin, _conditionManager, damageManager);

		_projectileManager = projectileManager;

		_packetHandler = packetHandler;

		_partyManager = new PartyManager();
		_statsManager = statsManager;
		_taskManager = new TaskManager(plugin, clientManager);
		_achievementManager = achievementManager;
		_inventoryManager = inventoryManager;
		_cosmeticManager = cosmeticManager;
		_portal = portal;
		_petManager = petManager;
		_eventManager = new EventModule(this, getPlugin());
		_resourcePackManager = new ResourcePackManager(plugin, portal);

		_customDataManager = customDataManager;

		// Managers
		_gameChatManager = new GameChatManager(this);
		_gameCreationManager = new GameCreationManager(this);
		_gameRewardManager = new GameRewardManager(this);
		_gameManager = new GameManager(this);
		_gameLobbyManager = new File("world/NEW.dat").exists() ? new NewGameLobbyManager(this) : new LegacyGameLobbyManager(this);
		_gameHostManager = new GameHostManager(this);
		new GameFlagManager(this);
		_gamePlayerManager = new GamePlayerManager(this);
		new GameAchievementManager(this);
		new GameStatManager(this);
		YoutubeManager youtubeManager = new YoutubeManager(plugin, clientManager, donationManager);
		_bonusManager = new BonusManager(plugin, _gameLobbyManager.getCarl(), clientManager, donationManager, pollManager, npcManager, hologramManager, statsManager, _inventoryManager, petManager, youtubeManager, _cosmeticManager.getGadgetManager(), thankManager, "Carl");

		new GameLootManager(this, petManager);
		_spectatorManager = new GameSpectatorManager(this);
		_gameWorldManager = new GameWorldManager(this);
		_hologramManager = hologramManager;
		_idleManager = new IdleManager(this);
		TitanGiveawayManager titanGiveaway = new TitanGiveawayManager(getPlugin(), clientManager, serverStatusManager);
		EternalGiveawayManager eternalGiveawayManager = new EternalGiveawayManager(getPlugin(), clientManager, serverStatusManager);

		//new HolidayManager(this, titanGiveaway, eternalGiveawayManager);

		//new ValentinesGiftManager(plugin, clientManager, _bonusManager.getRewardManager(), inventoryManager, _cosmeticManager.getGadgetManager(), statsManager);
		require(PlayerDisguiseManager.class);
		require(GameBoosterManager.class);

		// Game Addons
		new SoupAddon(plugin, this);

		//Champions Modules
		_energy = new Energy(plugin);

		_itemFactory = new ItemFactory(_plugin, _blockRestore, _conditionManager, damageManager, _energy, _fire, _projectileManager, this);

		_skillFactory = new SkillFactory(plugin, damageManager, this, _damageManager.GetCombatManager(),
				_conditionManager, _projectileManager, _disguiseManager, _blockRestore, _fire, new Movement(plugin), teleport,
				_energy);

		_classManager = new ClassManager(plugin, clientManager, donationManager, _cosmeticManager.getGadgetManager(), _skillFactory, _itemFactory);

		_classShopManager = new ClassShopManager(_plugin, _classManager, _skillFactory, _itemFactory, _achievementManager, clientManager);

		_classShop = new ClassCombatShop(_classShopManager, clientManager, donationManager, false, "Class Shop");

		_eloManager = eloManager;

		_punishmentManager = punish;

		_kitProgressionManager = new KitProgressionManager(getPlugin(), donationManager, clientManager);
		_serverUptimeManager = new ServerUptimeManager(this);
		_missionsManager = require(MissionManager.class);
		_missionsManager.createNPC(_gameLobbyManager.getMissions());
		_missionsManager.setCanIncrement(() -> false);

		if (GetHost() != null && !GetHost().isEmpty() && !GetHost().startsWith("COM-"))
		{
			Bukkit.getScheduler().runTaskLater(plugin, () -> Portal.transferPlayer(GetHost(), _serverStatusManager.getCurrentServerName()), 80L);
		}

		_nextBestGameManager = new NextBestGameManager(serverConfig.ServerGroup, UtilServer.getRegion(), _partyManager);

		addCommand(new GoToNextGameCommand(this));
		addCommand(new CancelNextGameCommand(this));
		addCommand(new TauntCommand(this));
		addCommand(new ReturnToHubCommand(this));
		addCommand(new SpectatorCommand(this));

		if (IsTournamentServer())
		{
			addCommand(new TournamentStopCommand(this));
		}

		require(PersonalServerManager.class);
		require(CommunityManager.class);

		_scoreboardManager = new ScoreboardManager(_plugin)
		{
			@Override
			public void handlePlayerJoin(String playerName)
			{
				if (GetGame() != null && !GetGame().inLobby() && GetGame().UseCustomScoreboard)
				{
					return;
				}

				CoreClient client = GetClients().Get(playerName);

				for (MineplexScoreboard scoreboard : getScoreboards().values())
				{
					scoreboard.getHandle().getTeam(client.getRealOrDisguisedPrimaryGroup().name()).addEntry(playerName);
				}

				Player player = Bukkit.getPlayerExact(playerName);

				if (player != null && get(player) != null)
				{
					for (Player player1 : Bukkit.getOnlinePlayers())
					{
						client = GetClients().Get(player1);

						get(player).getHandle().getTeam(client.getRealOrDisguisedPrimaryGroup().name()).addEntry(player1.getName());
					}
				}

				for (Player onlinePlayer : Bukkit.getOnlinePlayers())
				{
					GameTeam gameTeam = null;
					if (GetGame() != null && GetGame().GetTeam(onlinePlayer) != null)
					{
						gameTeam = GetGame().GetTeam(onlinePlayer);
					}

					_gameLobbyManager.AddPlayerToScoreboards(onlinePlayer, gameTeam);
				}

				if (GetGame() != null)
				{
					if (GetGame().IsAlive(player))
					{
						GetGame().GetScoreboard().setPlayerTeam(player, GetGame().GetTeam(player));
					}
					else
					{
						GetGame().GetScoreboard().setSpectating(player);
					}
				}
			}

			@Override
			public void handlePlayerQuit(String playerName)
			{
				CoreClient client = GetClients().Get(playerName);

				for (MineplexScoreboard scoreboard : getScoreboards().values())
				{
					scoreboard.getHandle().getTeam(client.getRealOrDisguisedPrimaryGroup().name()).removeEntry(playerName);
				}
			}

			@Override
			public void setup(MineplexScoreboard scoreboard)
			{
				for (PermissionGroup group : PermissionGroup.values())
				{
					if (!group.canBePrimary())
					{
						continue;
					}
					if (!group.getDisplay(false, false, false, false).isEmpty())
					{
						scoreboard.getHandle().registerNewTeam(group.name()).setPrefix(group.getDisplay(true, true, true, false) + ChatColor.RESET + " ");
					}
					else
					{
						scoreboard.getHandle().registerNewTeam(group.name()).setPrefix("");
					}
				}

				scoreboard.register(ArcadeScoreboardLine.PLAYERS_SPACER)
						.register(ArcadeScoreboardLine.PLAYERS_NAME)
						.register(ArcadeScoreboardLine.PLAYERS_VALUE)
						.register(ArcadeScoreboardLine.KIT_SPACER)
						.register(ArcadeScoreboardLine.KIT_NAME)
						.register(ArcadeScoreboardLine.KIT_VALUE)
						.register(ArcadeScoreboardLine.GEM_SPACER)
						.register(ArcadeScoreboardLine.GEM_NAME)
						.register(ArcadeScoreboardLine.GEM_VALUE)
						.register(ArcadeScoreboardLine.SERVER_SPACER)
						.register(ArcadeScoreboardLine.SERVER_NAME)
						.register(ArcadeScoreboardLine.SERVER_VALUE)
						.recalculate();

				scoreboard.get(ArcadeScoreboardLine.PLAYERS_NAME).write(C.cYellowB + "Players");
				scoreboard.get(ArcadeScoreboardLine.KIT_NAME).write(C.cGoldB + "Kit");
				scoreboard.get(ArcadeScoreboardLine.SERVER_NAME).write(C.cAquaB + "Server");
				scoreboard.get(ArcadeScoreboardLine.SERVER_VALUE).write(UtilServer.getServerName());
			}

			@Override
			public void draw(MineplexScoreboard scoreboard)
			{
				if (_gameCreationManager.getVotingManager().isVoteInProgress())
				{
					scoreboard.setSidebarName(C.Bold + "Vote ends in " + C.cGreenB + _gameCreationManager.getVotingManager().getCurrentVote().getTimer());
				}
				else if (GetGame() != null)
				{
					int countdown = GetGame().GetCountdown();
					GameState state = GetGame().GetState();

					if (countdown > 0)
					{
						scoreboard.setSidebarName(C.Bold + "Starting in " + C.cGreenB + countdown + " Second" + (countdown == 1 ? "" : "s"));
					}
					else if (countdown == 0)
					{
						scoreboard.setSidebarName(C.cGreenB + "In Progress...");
					}
					else if (state == GameState.Recruit || state == GameState.PreLoad)
					{
						scoreboard.setSidebarName(C.cGreenB + "Waiting for players");
					}
					else if (state == GameState.Loading)
					{
						scoreboard.setSidebarName(C.cGreenB + "Loading...");
					}
				}
				else
				{
					scoreboard.setSidebarName(C.cGreenB + "Waiting for players");
				}

				scoreboard.get(ArcadeScoreboardLine.PLAYERS_VALUE).write(getValidPlayersForGameStart().size() + "/" + GetPlayerFull());
				if (GetGame() != null && GetGame().CrownsEnabled)
				{
					scoreboard.get(ArcadeScoreboardLine.GEM_NAME).write(C.cGold + C.Bold + "Crowns");
					scoreboard.get(ArcadeScoreboardLine.GEM_VALUE).write(donationManager.getCrowns(scoreboard.getOwner()));
				}
				else
				{
					scoreboard.get(ArcadeScoreboardLine.GEM_NAME).write(C.cGreen + C.Bold + "Gems");
					scoreboard.get(ArcadeScoreboardLine.GEM_VALUE).write(donationManager.Get(scoreboard.getOwner()).getBalance(GlobalCurrency.GEM));
				}

				ChatColor teamColor = ChatColor.GOLD;
				String kitName = "None";

				if (GetGame() != null)
				{
					Kit kit = GetGame().GetKit(scoreboard.getOwner());
					GameTeam team = GetGame().GetTeam(scoreboard.getOwner());

					if (kit != null)
					{
						kitName = kit.GetName() + "";
					}

					if (team != null)
					{
						teamColor = team.GetColor();
					}
				}

				scoreboard.get(ArcadeScoreboardLine.KIT_NAME).write(teamColor + C.Bold + "Kit");
				scoreboard.get(ArcadeScoreboardLine.KIT_VALUE).write(kitName);

				if (GetGame() instanceof MinecraftLeague)
				{
					if (!scoreboard.isRegistered(ArcadeScoreboardLine.DIVISION_SPACER))
					{
						scoreboard.register(ArcadeScoreboardLine.DIVISION_SPACER)
								.register(ArcadeScoreboardLine.DIVISION_NAME)
								.register(ArcadeScoreboardLine.DIVISION_VALUE)
								.recalculate();
					}
					scoreboard.get(ArcadeScoreboardLine.DIVISION_NAME).write(C.cPurpleB + "Division");


					EloManager.EloDivision ed = EloManager.EloDivision.getDivision(getEloManager().getElo(scoreboard.getOwner(), GetGame().GetType().getGameId()));
					scoreboard.get(ArcadeScoreboardLine.DIVISION_VALUE).write(ed.getDisplayName());
				}
				else
				{
					if (scoreboard.isRegistered(ArcadeScoreboardLine.DIVISION_SPACER))
					{
						scoreboard.unregister(ArcadeScoreboardLine.DIVISION_SPACER)
								.unregister(ArcadeScoreboardLine.DIVISION_NAME)
								.unregister(ArcadeScoreboardLine.DIVISION_VALUE)
								.recalculate();
					}
				}
			}
		};
		new MenuManager(_plugin);
		Managers.put(_scoreboardManager, ScoreboardManager.class);
		_trackManager = require(TrackManager.class);
		_titles = require(Titles.class);
		Titles.BOOK_SLOT = 4;

		_sheetsManager = require(GoogleSheetsManager.class);
		_winStreakManager = require(WinStreakManager.class);
		_mineplexGameManager = require(MineplexGameManager.class);

		if (IsHotbarHubClock())
		{
			new HubClockManager(this);
		}

		new AdminCommands();

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.PLAYER.setPermission(Perm.RETURN_TO_HUB_COMMAND, true, true);
		PermissionGroup.CONTENT.setPermission(Perm.USE_MENU_DURING_GAME, true, true);
		PermissionGroup.BUILDER.setPermission(Perm.USE_MENU_DURING_GAME, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.NEXT_BEST_GAME, true, true);
		PermissionGroup.CONTENT.setPermission(Perm.KIT_UNLOCK_COMMAND, true, true);
		PermissionGroup.BUILDER.setPermission(Perm.KIT_UNLOCK_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.TAUNT_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.GAME_COMMAND, true, true);

		if (UtilServer.isTestServer())
		{
			PermissionGroup.QAT.setPermission(Perm.KIT_UNLOCK_COMMAND, true, true);
			PermissionGroup.QA.setPermission(Perm.GAME_COMMAND, true, true);
			PermissionGroup.MAPLEAD.setPermission(Perm.GAME_COMMAND, false, true);
		}

		if (UtilServer.isTestServer() || UtilServer.isDevServer())
		{
			PermissionGroup.ADMIN.setPermission(Perm.AUTO_OP, true, true);
			PermissionGroup.QAM.setPermission(Perm.AUTO_OP, false, true);
		}
		else
		{
			PermissionGroup.LT.setPermission(Perm.AUTO_OP, true, true);
		}

		PermissionGroup.BUILDER.setPermission(Perm.FEATURED_SERVER, true, true);
		PermissionGroup.CONTENT.setPermission(Perm.FEATURED_SERVER, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.INFORM_RANKED_MODERATION_POTENTIAL, true, true);
		PermissionGroup.ULTRA.setPermission(Perm.JOIN_FULL, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.JOIN_FULL_STAFF, true, true);
		PermissionGroup.BUILDER.setPermission(Perm.BYPASS_WHITELIST, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.BYPASS_WHITELIST, false, false);
		PermissionGroup.MOD.setPermission(Perm.BYPASS_MPS_WHITELIST, true, true);
		PermissionGroup.EVENTMOD.setPermission(Perm.TOURNAMENT_STOP_COMMAND, false, true);
		PermissionGroup.PLAYER.setPermission(Perm.SPECTATOR_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.MAP_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new GameCommand(this));
		addCommand(new KitUnlockCommand(this));
		addCommand(new MapCommand(this));
	}

	public GameChatManager getGameChatManager()
	{
		return _gameChatManager;
	}

	public BonusManager getBonusManager()
	{
		return _bonusManager;
	}

	public GameServerConfig GetServerConfig()
	{
		return _serverConfig;
	}

	public ResourcePackManager getResourcePackManager()
	{
		return _resourcePackManager;
	}

	public ArrayList<GameType> GetGameList()
	{
		return GetServerConfig().GameList;
	}

	public AchievementManager GetAchievement()
	{
		return _achievementManager;
	}

	public Blood GetBlood()
	{
		return _blood;
	}

	public Chat GetChat()
	{
		return _chat;
	}

	public BlockRestore GetBlockRestore()
	{
		return _blockRestore;
	}

	public CoreClientManager GetClients()
	{
		return _clientManager;
	}

	public ConditionManager GetCondition()
	{
		return _conditionManager;
	}

	public Creature GetCreature()
	{
		return _creature;
	}

	public PacketHandler getPacketHandler()
	{
		return _packetHandler;
	}

	public CosmeticManager getCosmeticManager()
	{
		return _cosmeticManager;
	}

	public DisguiseManager GetDisguise()
	{
		return _disguiseManager;
	}

	public HologramManager getHologramManager()
	{
		return _hologramManager;
	}

	public DamageManager GetDamage()
	{
		return _damageManager;
	}

	public DonationManager GetDonation()
	{
		return _donationManager;
	}

	public EloManager getEloManager()
	{
		return _eloManager;
	}

	public Explosion GetExplosion()
	{
		return _explosionManager;
	}

	public Fire GetFire()
	{
		return _fire;
	}

	public ProjectileManager GetProjectile()
	{
		return _projectileManager;
	}

	public Punish getPunishments()
	{
		return _punishmentManager;
	}

	public Portal GetPortal()
	{
		return _portal;
	}

	public LobbyManager GetLobby()
	{
		return _gameLobbyManager;
	}

	public TaskManager GetTaskManager()
	{
		return _taskManager;
	}

	public GameCreationManager GetGameCreationManager()
	{
		return _gameCreationManager;
	}

	public GameRewardManager getGameRewardManager()
	{
		return _gameRewardManager;
	}

	public GameHostManager GetGameHostManager()
	{
		return _gameHostManager;
	}

	public GameManager GetGameManager()
	{
		return _gameManager;
	}

	public GamePlayerManager GetGamePlayerManager()
	{
		return _gamePlayerManager;
	}

	public GameWorldManager GetGameWorldManager()
	{
		return _gameWorldManager;
	}

	public EventModule GetEventModule()
	{
		return _eventManager;
	}

	public PreferencesManager getPreferences()
	{
		return _preferencesManager;
	}

	public StatsManager GetStatsManager()
	{
		return _statsManager;
	}

	public ServerStatusManager GetServerStatusManager()
	{
		return _serverStatusManager;
	}

	public CustomDataManager getCustomDataManager()
	{
		return _customDataManager;
	}

	public WinStreakManager getWinStreakManager()
	{
		return _winStreakManager;
	}

	public ChatColor GetColor(Player player)
	{
		if (_game == null)
			return ChatColor.GRAY;

		GameTeam team = _game.GetTeam(player);
		if (team == null)
			return ChatColor.GRAY;

		return team.GetColor();
	}

	@Override
	public boolean canHurt(String a, String b)
	{
		return canHurt(UtilPlayer.searchExact(a), UtilPlayer.searchExact(b));
	}

	@Override
	public boolean canHurt(Player pA, Player pB)
	{
		if (pA == null || pB == null)
			return false;

		if (!_game.Damage)
			return false;

		if (!_game.DamagePvP)
			return false;

		// Self Damage
		if (pA.equals(pB))
			return _game.DamageSelf;

		GameTeam tA = _game.GetTeam(pA);
		if (tA == null)
			return false;

		GameTeam tB = _game.GetTeam(pB);
		if (tB == null)
			return false;

		if (tA.equals(tB) && !_game.DamageTeamSelf)
			return false;

		if (!tA.equals(tB) && !_game.DamageTeamOther)
			return false;

		return true;
	}

	@Override
	public boolean isSafe(Player player)
	{
		if (_game == null)
			return true;

		if (_game.IsPlaying(player))
			return false;

		return true;
	}

	public boolean canPlayerUseGameCmd(Player player)
	{
		if (!GetClients().Get(player).hasPermission(Perm.GAME_COMMAND) && !(_gameHostManager.isEventServer() && _gameHostManager.isAdmin(player, false)))
		{
			player.sendMessage(F.main("Game", "You are not allowed to use game commands."));
			return false;
		}

		return true;
	}

	@EventHandler
	public void StaffIncognito(IncognitoStatusChangeEvent event)
	{
		Player player = event.getPlayer();

		if (event.getNewState()) //Is going into incognito
		{
			GameTeam team = _game.GetTeam(player);

			// Color their name if they are on a team
			if (team != null)
			{
				UtilServer.broadcast(F.sys("Quit", team.GetColor() + player.getName()));
			}
			else
			{
				UtilServer.broadcast(F.sys("Quit", player.getName()));
			}

			if (_game != null && _game.GetCountdown() > 0) //Lobby is counting down
			{
				// Remove Data
				_game.getTeamModule().getPreferences().remove(player);
				_game.GetPlayerKits().remove(player);
				_game.GetPlayerGems().remove(player);

				// Leave Team
				if (team != null)
				{
					team.RemovePlayer(player);
				}
			}
			else if (_game != null && !_game.inLobby()) //Game is NOT in lobby
			{
				addSpectator(player, true);
				_specList.add(player);

				// Leave Team
				if (team != null)
				{
					team.RemovePlayer(player);
				}
			}
			else //Game is in lobby
			{
				_specList.add(player);
			}
		}
		else //Is coming out of incognito
		{
			UtilServer.broadcast(F.sys("Join", player.getName()));

			if (_game != null)
			{
				if (!_game.InProgress())
				{
					_specList.remove(player);
				}

				if (isSpectator(player))
				{
					event.show(false);
				}
			}
		}
	}

	@EventHandler
	public void motdPing(ServerListPingEvent event)
	{
		event.setMaxPlayers(_serverConfig.MaxPlayers);

		if (UtilServer.isTestServer(false))
		{
			event.setMotd(C.cGold + "Private Mineplex Test Server");
			return;
		}

		//MPS
		if (_gameHostManager.isPrivateServer())
		{
			if (_gameHostManager.isHostExpired())
			{
				event.setMotd(C.cRed + "Finished");
				return;
			}

			if (!GetServerConfig().PublicServer || GetServerConfig().PlayerServerWhitelist || _gameHostManager.isCommunityServer())
			{
				event.setMotd(C.cGray + "Private");
				return;
			}
		}

		boolean nullGame = _game == null;
		GameDisplay game = null;
		String mode = null, map = null;
		int timer = -1;
		String[] votingOn = null;
		PermissionGroup hostRank = null;
		GameDisplayStatus status;

		// Vote in progress
		if (_gameCreationManager.getVotingManager().isVoteInProgress())
		{
			status = GameDisplayStatus.VOTING;

			// Null game - GameVote
			if (nullGame)
			{
				game = null;
				map = null;
			}
			// MapVote
			else
			{
				game = _game.GetType().getDisplay();
				mode = _game.GetMode();
				map = null;
			}

			Vote<? extends Voteable> vote = _gameCreationManager.getVotingManager().getCurrentVote();

			if (vote != null)
			{
				timer = vote.getTimer();
				votingOn = vote.getValues().stream()
						.map(Voteable::getDisplayName)
						.toArray(String[]::new);
			}
		}
		// No vote and game null. Staff-1 or pre game voting
		else if (nullGame)
		{
			status = GameDisplayStatus.WAITING;
		}
		// Game not null
		else
		{
			game = _game.GetType().getDisplay();
			mode = _game.GetMode();

			// Has WorldData
			if (_game.WorldData != null)
			{
				map = _game.WorldData.MapName;
			}

			// Game not in progress
			if (_game.inLobby())
			{
				timer = _game.GetCountdown();

				// Counting down
				if (timer != -1)
				{
					status = GameDisplayStatus.STARTING;
				}
				else
				{
					status = GameDisplayStatus.WAITING;
				}
			}
			// Game in progress
			else
			{
				status = GameDisplayStatus.IN_PROGRESS;
			}
		}

		// MPS
		if (_gameHostManager.isPrivateServer() && _gameHostManager.getHostRank() != null)
		{
			hostRank = _gameHostManager.getHostRank();
		}

		event.setMotd(new GameInfo(game, mode, map, timer, votingOn, hostRank, status, getJoinable()).toString());
	}

	@EventHandler
	public void onClickCompassPartyIcon(PartySelectServerEvent event)
	{
		UtilPlayer.message(event.getPlayer(), F.main("Party", "This option cannot be used here"));
	}

	@EventHandler
	public void MessageJoin(PlayerJoinEvent event)
	{
		if (event.getJoinMessage() == null)
		{
			return;
		}

		if (_incognitoManager.Get(event.getPlayer()).Status)
		{
			event.setJoinMessage(null);
			return;
		}

		if (_game == null || _game.AnnounceJoinQuit)
		{
			JoinMessageBroadcastEvent joinMessageBroadcastEvent = new JoinMessageBroadcastEvent(event.getPlayer());
			UtilServer.CallEvent(joinMessageBroadcastEvent);

			if (joinMessageBroadcastEvent.getUsername() != null)
			{
				event.setJoinMessage(F.sys("Join", GetColor(event.getPlayer()) + joinMessageBroadcastEvent.getUsername()));
			}
		}
		else
		{
			event.setJoinMessage(null);
		}
	}

	@EventHandler
	public void MessageQuit(PlayerQuitEvent event)
	{
		if (_incognitoManager.Get(event.getPlayer()).Status)
		{
			event.setQuitMessage(null);
			return;
		}

		String name = event.getPlayer().getName();

		if (event.getQuitMessage() == null)
			return;

		if (_game == null || _game.AnnounceJoinQuit)
			event.setQuitMessage(F.sys("Quit", GetColor(event.getPlayer()) + name));
		else
			event.setQuitMessage(null);
	}

	public Game GetGame()
	{
		return _game;
	}

	public void SetGame(Game game)
	{
		_game = game;
		_nextBestGameManager.setGame(game);
	}

	public int GetPlayerMin()
	{
		return GetServerConfig().MinPlayers;
	}

	public int GetPlayerFull()
	{
		return GetServerConfig().MaxPlayers;
	}

	@EventHandler
	public void Login(PlayerLoginEvent event)
	{
		Player player = event.getPlayer();
		CoreClient client = _clientManager.Get(event.getPlayer());

		if (Bukkit.getServer().hasWhitelist())
		{
			if (client.hasPermission(Perm.BYPASS_WHITELIST))
			{
				event.allow();
				event.setResult(PlayerLoginEvent.Result.ALLOWED);

				if (_serverConfig.Tournament)
				{
					event.getPlayer().setOp(true);
				}
			}
			else
			{
				for (OfflinePlayer other : Bukkit.getWhitelistedPlayers())
				{
					if (other.getName().equalsIgnoreCase(player.getName()))
					{
						event.allow();
						event.setResult(PlayerLoginEvent.Result.ALLOWED);
						return;
					}
				}

				event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Server Whitelisted!");
			}

			return;
		}

		GameJoinStatus joinable = getJoinable();

		if (joinable != GameJoinStatus.OPEN)
		{
			if (client.hasPermission(Perm.JOIN_FULL_STAFF))
			{
				event.allow();
				return;
			}

			boolean canOverflow = client.hasPermission(Perm.JOIN_FULL) || _donationManager.Get(player).ownsUnknownSalesPackage(_serverConfig.ServerType + " ULTRA");

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
			if (_serverConfig.HardMaxPlayerCap || (double) Bukkit.getServer().getOnlinePlayers().size() / Bukkit.getMaxPlayers() > 1.5 || (_gameHostManager.isEventServer() && Bukkit.getServer().getOnlinePlayers().size() >= 128))
			{
				return GameJoinStatus.CLOSED;
			}

			return GameJoinStatus.RANKS_ONLY;
		}

		return GameJoinStatus.OPEN;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void adminOP(PlayerJoinEvent event)
	{
		event.getPlayer().setOp(_clientManager.Get(event.getPlayer()).hasPermission(Perm.AUTO_OP));
	}

	public boolean IsAlive(Player player)
	{
		return _game != null && _game.IsAlive(player);
	}

	/**
	 * Checks if a player is participating in/has been participating in the current game
	 *
	 * @param player The player to check
	 * @return Whether they are/have been playing
	 */
	public boolean hasBeenPlaying(Player player)
	{
		return _game != null && _game.GetTeam(player) != null;
	}

	public void Clear(Player player)
	{
		if (player.getGameMode() == GameMode.SPECTATOR)
			player.setSpectatorTarget(null);

		player.setGameMode(GameMode.SURVIVAL);
		player.setAllowFlight(false);
		player.setFlySpeed(0.1F);
		player.setWalkSpeed(0.2F);

		UtilInv.Clear(player);

		UtilPlayer.setAutoDeploy(player, false);
		UtilPlayer.setGlidableWithoutWings(player, false);
		UtilPlayer.setGliding(player, false);
		UtilPlayer.setAutoDeployDistance(player, 1.15F);

		((CraftEntity) player).getHandle().getDataWatcher().watch(0, (byte) 0, EntityLiving.META_ENTITYDATA, (byte) 0);

		player.setCustomName("");
		player.setCustomNameVisible(false);

		player.setSprinting(false);
		player.setSneaking(false);

		player.setFoodLevel(20);
		player.setSaturation(3f);
		player.setExhaustion(0f);

		player.setMaxHealth(20);
		player.setHealth(player.getMaxHealth());

		player.setFireTicks(0);
		player.setFallDistance(0);

		player.eject();
		player.leaveVehicle();

		player.setLevel(0);
		player.setExp(0f);

		player.resetPlayerTime();
		player.resetPlayerWeather();

		((CraftPlayer) player).getHandle().spectating = false;
		((CraftPlayer) player).getHandle().setGhost(false);
		((CraftPlayer) player).getHandle().k = true;

		// Arrows go bye bye.
		((CraftPlayer) player).getHandle().o(0);

		//Remove all conditions
		GetCondition().EndCondition(player, null, null);
		for (PotionEffect potion : player.getActivePotionEffects())
			player.removePotionEffect(potion.getType());

		Gadget morph = getCosmeticManager().getGadgetManager().getActive(player, GadgetType.MORPH);
		if (morph != null && morph.isActive(player))
			morph.disable(player);
	}

	public ArrayList<String> LoadFiles(String gameName)
	{
		TimingManager.start("ArcadeManager LoadFiles");

		File folder = new File(".." + File.separatorChar + ".." + File.separatorChar + "update" + File.separatorChar
				+ "maps" + File.separatorChar + gameName);
		System.out.println(folder.getAbsolutePath() + " -=-=-=-=-=");
		if (!folder.exists())
			folder.mkdirs();

		ArrayList<String> maps = new ArrayList<String>();

		System.out.println("Searching Maps in: " + folder);

		if (folder.listFiles() != null)
		{
			for (File file : folder.listFiles())
			{
				if (!file.isFile())
				{
					System.out.println(file.getName() + " is not a file!");
					continue;
				}

				String name = file.getName();

				if (name.length() < 5)
					continue;

				name = name.substring(name.length() - 4, name.length());

				if (!name.equals(".zip"))
				{
					System.out.println(file.getName() + " is not a zip.");
					continue;
				}

				maps.add(file.getName().substring(0, file.getName().length() - 4));
			}
		}

		for (String map : maps)
			System.out.println("Found Map: " + map);

		TimingManager.stop("ArcadeManager LoadFiles");

		return maps;
	}

	public ClassManager getClassManager()
	{
		return _classManager;
	}

	public ClassCombatShop getClassShop()
	{
		return _classShop;
	}

	public void openClassShop(Player player)
	{
		_classShop.attemptShopOpen(player);
	}

	@EventHandler
	public void BlockBurn(BlockBurnEvent event)
	{
		if (_game == null)
			event.setCancelled(true);
	}

	@EventHandler
	public void BlockSpread(BlockSpreadEvent event)
	{
		if (_game == null)
			event.setCancelled(true);
	}

	@EventHandler
	public void BlockFade(BlockFadeEvent event)
	{
		if (_game == null)
			event.setCancelled(true);
	}

	@EventHandler
	public void BlockDecay(LeavesDecayEvent event)
	{
		if (_game == null)
			event.setCancelled(true);
	}

	@EventHandler
	public void SkillTrigger(SkillTriggerEvent event)
	{
		if (_game == null || !_game.IsLive())
		{
			event.SetCancelled(true);
		}
	}

	@EventHandler
	public void ItemTrigger(ItemTriggerEvent event)
	{
		if (_game == null || !_game.IsLive())
		{
			event.SetCancelled(true);
		}
	}

	public void toggleSpectator(Player player)
	{
		if (_game != null && _game.InProgress())
		{
			UtilPlayer.message(player, F.main("Game", "You cannot toggle Spectator during games."));
			return;
		}

		if (isVanished(player))
		{
			UtilPlayer.message(player, F.main("Game", "You cannot toggle spectator while vanished."));
			return;
		}

		if (!_specList.remove(player))
		{
			if (_game != null && !_game.SpectatorAllowed)
			{
				UtilPlayer.message(player, F.main("Game", "You are not allowed to toggle Spectator in this game!"));
				return;
			}
			_specList.add(player);

			UtilPlayer.message(player, F.main("Game", "You are now a Spectator!"));
		}
		else
		{
			UtilPlayer.message(player, F.main("Game", "You are no longer a Spectator!"));
		}

		// Clean
		if (_game != null)
		{
			// Remove Data
			_game.getTeamModule().getPreferences().remove(player);
			_game.GetPlayerKits().remove(player);
			_game.GetPlayerGems().remove(player);

			// Leave Team
			GameTeam team = _game.GetTeam(player);

			if (team != null)
			{
				team.RemovePlayer(player);
			}
		}
	}

	@EventHandler
	public void ObserverQuit(PlayerQuitEvent event)
	{
		_specList.remove(event.getPlayer());
	}

	public boolean IsObserver(Player player)
	{
		if (_incognitoManager.Get(player).Status)
		{
			_specList.add(player);
		}
		return _specList.contains(player);
	}

	public boolean isVanished(Player player)
	{
		return _incognitoManager.Get(player).Status;
	}

	public boolean IsTournamentServer()
	{
		return _serverConfig.Tournament;
	}

	public boolean IsTournamentPoints()
	{
		return _serverConfig.TournamentPoints;
	}

	public boolean IsTeamRejoin()
	{
		return _serverConfig.TeamRejoin;
	}

	public boolean IsTeamAutoJoin()
	{
		return _serverConfig.TeamAutoJoin;
	}

	public boolean IsGameAutoStart()
	{
		return _serverConfig.GameAutoStart;
	}

	public boolean IsGameTimeout()
	{
		return _serverConfig.GameTimeout;
	}

	public boolean IsTeamBalance()
	{
		return _serverConfig.TeamForceBalance;
	}

	public boolean IsRewardGems()
	{
		return _serverConfig.RewardGems;
	}

	public boolean IsRewardItems()
	{
		return _serverConfig.RewardItems;
	}

	public boolean IsRewardStats()
	{
		return _serverConfig.RewardStats;
	}

	public boolean IsRewardAchievements()
	{
		return _serverConfig.RewardAchievements;
	}

	public boolean IsHotbarInventory()
	{
		return _serverConfig.HotbarInventory;
	}

	public boolean IsHotbarHubClock()
	{
		return _serverConfig.HotbarHubClock;
	}

	public boolean IsPlayerKickIdle()
	{
		return _serverConfig.PlayerKickIdle;
	}

	public String GetHost()
	{
		return _serverConfig.HostName;
	}

	@EventHandler
	public void ObserverQuit(GameStateChangeEvent event)
	{
		if (_skillFactory != null)
		{
			_skillFactory.ResetAll();
		}
	}

	public InventoryManager getInventoryManager()
	{
		return _inventoryManager;
	}

	@EventHandler
	public void toggleSpec(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Loading)
			return;
		if (_game != null)
		{
			if (_game.SpectatorAllowed)
			{
				return;
			}
		}

		for (Player player : _specList)
		{
			_specList.remove(player);

			// Clean
			if (_game != null)
			{
				// Remove Data
				_game.getTeamModule().getPreferences().remove(player);
				_game.GetPlayerKits().remove(player);
				_game.GetPlayerGems().remove(player);

				// Leave Team
				GameTeam team = _game.GetTeam(player);

				if (team != null)
				{
					team.RemovePlayer(player);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void cosmeticState(GameStateChangeEvent event)
	{
		GameState state = event.GetState();

		if (state == GameState.PreLoad || state == GameState.Dead)
		{
			getTitles().forceEnable();
			getCosmeticManager().setActive(true);
			getCosmeticManager().setHideParticles(false);
		}
		else if (state == GameState.Prepare)
		{
			Game game = event.GetGame();

			if (game.GadgetsDisabled)
			{
				if (getCosmeticManager().isShowingInterface())
				{
					getCosmeticManager().setActive(false);
					getCosmeticManager().disableItemsForGame();
				}
			}

			if (!game.AllowParticles)
			{
				getCosmeticManager().setHideParticles(true);
			}

			if (game.ShowWeaponNames)
			{
				getCosmeticManager().getGadgetManager().setShowWeaponNames(true);
			}
		}
	}

	@EventHandler
	public void disableGadget(PlayerJoinEvent event)
	{
		updateGadgetEnabled();
	}

	@EventHandler
	public void disableGadget(PlayerQuitEvent event)
	{
		updateGadgetEnabled();
	}

	private void updateGadgetEnabled()
	{
		// Disables gadgets if player count is greater than 40
		int playerCount = UtilServer.getPlayers().length;
		getCosmeticManager().getGadgetManager().setGadgetEnabled(playerCount <= (GetGameHostManager().isEventServer() ? 120 : 40));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void disableGadgetsInGame(GadgetEnableEvent event)
	{
		if (_game != null && _game.InProgress() && _game.GadgetsDisabled && event.getGadget().getGadgetType() == GadgetType.COSTUME)
		{
			event.setShowMessage(false);
			event.setCancelled(true);
		}
	}

	public boolean isGameInProgress()
	{
		return _game != null && _game.InProgress();
	}

	public BoosterManager getBoosterManager()
	{
		return _boosterManager;
	}

	public void toggleUnlockKits(Player caller)
	{
		UserPreferences preferences = _preferencesManager.get(caller);
		boolean newState = !preferences.isActive(Preference.UNLOCK_KITS);

		preferences.set(Preference.UNLOCK_KITS, newState);
		caller.sendMessage(F.main("Kit", "Unlock all kits " + F.ed(newState) + "."));
		_preferencesManager.save(preferences);
	}

	public void enableChampionsModules()
	{
		_classManager.setEnabled(true);
		_classShopManager.registerSelf();
		_skillFactory.registerSelf();
		_itemFactory.registerSelf();
		_energy.registerSelf();
		_eloManager.registerSelf();

		//Class Shop
		_plugin.getServer().getPluginManager().registerEvents(_classShop, _plugin);
	}

	public void disableChampionsModules()
	{
		_classManager.setEnabled(false);
		_classShopManager.deregisterSelf();
		_skillFactory.deregisterSelf();
		_itemFactory.deregisterSelf();
		_energy.deregisterSelf();
		_eloManager.deregisterSelf();

		//Class Shop
		HandlerList.unregisterAll(_classShop);
	}

	public void toggleChampionsModules(GameType gameType)
	{
		boolean isChamps = gameType == GameType.ChampionsDominate || gameType == GameType.ChampionsTDM || gameType == GameType.ChampionsCTF || gameType == GameType.BossBattles;

		if (_enabled == isChamps)
		{
			System.out.println("----------Champions Modules are still " + isChamps);
			return;
		}

		System.out.println("----------Champions Modules set to " + isChamps);
		_enabled = isChamps;

		if (_enabled)
		{
			enableChampionsModules();
		}
		else
		{
			disableChampionsModules();
		}
	}

	public boolean isChampionsEnabled()
	{
		return _enabled;
	}

	public PartyManager getPartyManager()
	{
		return _partyManager;
	}

	public void addSpectator(Player player, boolean teleport)
	{
		if (GetGame() == null)
			return;

		Clear(player);

		if (teleport)
			player.teleport(GetGame().GetSpectatorLocation());

		//Set Spec State
		UtilAction.velocity(player, new Vector(0, 1, 0));
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setFlySpeed(0.1f);
		((CraftPlayer) player).getHandle().spectating = true;
		((CraftPlayer) player).getHandle().setGhost(true);
		((CraftPlayer) player).getHandle().k = false;

		GetCondition().Factory().Cloak("Spectator", player, player, 7777, true, true);

		//Game Team
		GetGame().GetScoreboard().setSpectating(player);
	}

	public boolean isSpectator(Entity player)
	{
		return UtilPlayer.isSpectator(player);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void explosionCancel(EntityExplodeEvent event)
	{
		if (GetGame() == null || !GetGame().InProgress())
		{
			event.blockList().clear();
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void explosionCancel(ExplosionEvent event)
	{
		if (GetGame() == null || !GetGame().InProgress())
		{
			event.GetBlocks().clear();
		}
	}

	@EventHandler
	public void spawnDebug(PlayerCommandPreprocessEvent event)
	{
		if (event.getPlayer().isOp() && event.getMessage().contains("setmax"))
		{
			try
			{
				int i = Integer.parseInt(event.getMessage().split(" ")[1]);

				_serverConfig.MaxPlayers = i;
			}
			catch (Exception e)
			{

			}

			event.setCancelled(true);
		}
	}

	@EventHandler
	public void clearDisguises(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Dead)
		{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				while (true)
				{
					DisguiseBase activeDisguise = _disguiseManager.getActiveDisguise(player);
					if (activeDisguise == null)
					{
						break;
					}
					if (!(activeDisguise instanceof DisguisePlayer))
					{
						_disguiseManager.undisguise(player);
					}
					else
					{
						break;
					}
				}
			}
		}
	}

	@EventHandler
	public void clearGameTeams(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Dead)
		{
			for (MineplexScoreboard scoreboard : _scoreboardManager.getScoreboards().values())
			{
				for (Team team : scoreboard.getHandle().getTeams())
				{
					if (team.getName().startsWith("GT"))
					{
						team.unregister();
					}
				}
			}
			for (Player player : Bukkit.getOnlinePlayers())
			{
				_gameLobbyManager.AddPlayerToScoreboards(player, null);
			}
		}
	}

	/**
	 * Allows mob spawning from core
	 *
	 * @param event
	 */
	@EventHandler
	public void onEnableArcadeSpawn(EnableArcadeSpawnEvent event)
	{
		if (_game != null)
		{
			_game.CreatureAllowOverride = event.canEnable();
		}
	}

	/**
	 * Allows adding a condition from another modules
	 *
	 * @param event
	 */
	@EventHandler
	public void onAddCondition(AddConditionEvent event)
	{
		_conditionManager.AddCondition(new Condition(_conditionManager, event));
	}

	/**
	 * Allows toggling mob spawning from another module
	 *
	 * @param event
	 */
	@EventHandler
	public void toggleMobSpawning(ToggleMobsEvent event)
	{
		if (_game != null)
		{
			_game.CreatureAllowOverride = event.enable();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void InteractActive(PlayerInteractEvent event)
	{
		event.setCancelled(false);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void InteractClickCancel(PlayerInteractEvent event)
	{
		if (GetGame() == null)
			return;

		Player player = event.getPlayer();

		//BoneMeal
		if (!GetGame().WorldBoneMeal &&
				event.getAction() == Action.RIGHT_CLICK_BLOCK &&
				player.getItemInHand().getType() == Material.INK_SACK &&
				player.getItemInHand().getData().getData() == (byte) 15)
		{
			event.setCancelled(true);

			runSyncLater(player::updateInventory, 1L);
		}
		else if (GetGame().GetState() != GameState.Live)
		{
			event.setCancelled(true);

			runSyncLater(player::updateInventory, 1L);
		}
	}

	@EventHandler
	public void combatQuit(CombatQuitEvent event)
	{
		if (_game == null || !_game.IsLive())
		{
			return;
		}

		event.setCancelled(false);
	}

	public List<Player> getValidPlayersForGameStart()
	{
		return PlayerSelector.selectPlayers(
				UtilLambda.and(
						PlayerSelector.NOT_VANISHED,
						player -> !IsObserver(player)
				)
		);
	}

	public KitProgressionManager getKitProgressionManager()
	{
		return _kitProgressionManager;
	}

	public GameSpectatorManager getGameSpectatorManager()
	{
		return _spectatorManager;
	}

	public ScoreboardManager getScoreboardManager()
	{
		return _scoreboardManager;
	}

	public NextBestGameManager getNextBestGameManager()
	{
		return _nextBestGameManager;
	}

	public TrackManager getTrackManager()
	{
		return _trackManager;
	}

	public Titles getTitles()
	{
		return _titles;
	}

	public MissionManager getMissionsManager()
	{
		return _missionsManager;
	}

	public GoogleSheetsManager getSheetsManager()
	{
		return _sheetsManager;
	}

	public MineplexGameManager getMineplexGameManager()
	{
		return _mineplexGameManager;
	}
}
