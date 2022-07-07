package mineplex.game.clans.clans;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mineplex.clansqueue.common.ClansQueueMessenger;
import com.mineplex.clansqueue.common.QueueConstant;
import com.mineplex.clansqueue.common.messages.ClansServerStatusMessage;
import com.mineplex.clansqueue.common.messages.ServerOfflineMessage;
import com.mineplex.clansqueue.common.messages.ServerOnlineMessage;

import mineplex.core.Managers;
import mineplex.core.MiniClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.chat.Chat;
import mineplex.core.chat.ChatChannel;
import mineplex.core.chat.event.FormatPlayerChatEvent;
import mineplex.core.common.Pair;
import mineplex.core.common.events.PlayerMessageEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.communities.CommunityManager;
import mineplex.core.creature.Creature;
import mineplex.core.creature.event.CreatureSpawnCustomEvent;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.elo.EloManager;
import mineplex.core.energy.Energy;
import mineplex.core.explosion.Explosion;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.HologramManager;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.incognito.events.IncognitoStatusChangeEvent;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.menu.MenuManager;
import mineplex.core.movement.Movement;
import mineplex.core.npc.NpcManager;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.personalServer.PersonalServerManager;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.projectile.ProjectileManager;
import mineplex.core.punish.Punish;
import mineplex.core.punish.PunishClient;
import mineplex.core.recharge.Recharge;
import mineplex.core.stats.StatsManager;
import mineplex.core.task.TaskManager;
import mineplex.core.teleport.Teleport;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.Clans;
import mineplex.game.clans.clans.ClanTips.TipType;
import mineplex.game.clans.clans.ClansUtility.ClanRelation;
import mineplex.game.clans.clans.amplifiers.AmplifierManager;
import mineplex.game.clans.clans.antiafk.AfkManager;
import mineplex.game.clans.clans.banners.BannerManager;
import mineplex.game.clans.clans.boxes.BoxManager;
import mineplex.game.clans.clans.cash.CashShopManager;
import mineplex.game.clans.clans.commands.ClansAllyChatCommand;
import mineplex.game.clans.clans.commands.ClansChatCommand;
import mineplex.game.clans.clans.commands.ClansCommand;
import mineplex.game.clans.clans.commands.KillCommand;
import mineplex.game.clans.clans.commands.MapCommand;
import mineplex.game.clans.clans.commands.RegionsCommand;
import mineplex.game.clans.clans.commands.SpeedCommand;
import mineplex.game.clans.clans.data.PlayerClan;
import mineplex.game.clans.clans.event.ClansPlayerDeathEvent;
import mineplex.game.clans.clans.gui.ClanShop;
import mineplex.game.clans.clans.invsee.InvseeManager;
import mineplex.game.clans.clans.loot.LootManager;
import mineplex.game.clans.clans.map.ItemMapManager;
import mineplex.game.clans.clans.moderation.antialt.AltManager;
import mineplex.game.clans.clans.mounts.MountManager;
import mineplex.game.clans.clans.nameblacklist.ClansBlacklist;
import mineplex.game.clans.clans.nether.NetherManager;
import mineplex.game.clans.clans.observer.ObserverManager;
import mineplex.game.clans.clans.playtime.Playtime;
import mineplex.game.clans.clans.potato.PotatoManager;
import mineplex.game.clans.clans.pvptimer.PvPTimerManager;
import mineplex.game.clans.clans.redis.ClanDeleteCommandHandler;
import mineplex.game.clans.clans.redis.ClanLoadCommandHandler;
import mineplex.game.clans.clans.regions.ClansRegions;
import mineplex.game.clans.clans.scoreboard.ClansScoreboardManager;
import mineplex.game.clans.clans.siege.SiegeManager;
import mineplex.game.clans.clans.supplydrop.SupplyDropManager;
import mineplex.game.clans.clans.tntgenerator.TntGeneratorManager;
import mineplex.game.clans.clans.war.WarManager;
import mineplex.game.clans.clans.warpoints.WarPointEvasion;
import mineplex.game.clans.clans.worldevent.WorldEventManager;
import mineplex.game.clans.core.ClaimLocation;
import mineplex.game.clans.core.ClanDeleteCommand;
import mineplex.game.clans.core.ClanLoadCommand;
import mineplex.game.clans.core.repository.ClanTerritory;
import mineplex.game.clans.core.repository.tokens.ClanMemberToken;
import mineplex.game.clans.core.repository.tokens.ClanTerritoryToken;
import mineplex.game.clans.core.repository.tokens.ClanToken;
import mineplex.game.clans.economy.GoldManager;
import mineplex.game.clans.fields.Field;
import mineplex.game.clans.gameplay.Gameplay;
import mineplex.game.clans.gameplay.HiddenChestManager;
import mineplex.game.clans.gameplay.safelog.SafeLog;
import mineplex.game.clans.gameplay.safelog.npc.NPCManager;
import mineplex.game.clans.items.GearManager;
import mineplex.game.clans.restart.RestartManager;
import mineplex.game.clans.spawn.Spawn;
import mineplex.game.clans.tutorial.TutorialManager;
import mineplex.minecraft.game.classcombat.Class.ClassManager;
import mineplex.minecraft.game.classcombat.Class.ClientClass;
import mineplex.minecraft.game.classcombat.Class.IPvpClass;
import mineplex.minecraft.game.classcombat.Class.repository.token.CustomBuildToken;
import mineplex.minecraft.game.classcombat.Condition.SkillConditionManager;
import mineplex.minecraft.game.classcombat.Skill.Assassin.Blink;
import mineplex.minecraft.game.classcombat.Skill.Assassin.Flash;
import mineplex.minecraft.game.classcombat.Skill.Mage.events.FissureModifyBlockEvent;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.item.ItemFactory;
import mineplex.minecraft.game.classcombat.shop.ClassCombatShop;
import mineplex.minecraft.game.classcombat.shop.ClassShopManager;
import mineplex.minecraft.game.core.IRelation;
import mineplex.minecraft.game.core.combat.CombatManager;
import mineplex.minecraft.game.core.condition.Condition;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.minecraft.game.core.damage.DamageManager;
import mineplex.minecraft.game.core.fire.Fire;
import mineplex.minecraft.game.core.mechanics.Weapon;
import mineplex.serverdata.commands.ServerCommandManager;

public class ClansManager extends MiniClientPlugin<ClientClan> implements IRelation
{
	public enum Perm implements Permission
	{
		ALLY_CHAT_COMMAND,
		CLAN_CHAT_COMMAND,
		CLANS_COMMAND,
		SUICIDE_COMMAND,
		MAP_COMMAND,
		REGION_CLEAR_COMMAND,
		SPEED_COMMAND,
		FORCE_JOIN_COMMAND,
		AUTO_OP,
		PREFIX_SHOWN,
		JOIN_FULL,
	}
	
	public static final int CLAIMABLE_RADIUS = 800;
	public static final int WORLD_RADIUS = 1200;
	private static final TimeZone TIME_ZONE = TimeZone.getDefault();
	private static ClansManager _instance;
	
	public static ClansManager getInstance()
	{
		return _instance;
	}
	
	private String _serverName;
	
	private CoreClientManager _clientManager;
	private CombatManager _combatManager;
	private ClansUtility _clanUtility;
	private ClansScoreboardManager _scoreboard;
	private ClansDataAccessLayer _clanDataAccess;
	private ClansDisplay _clanDisplay;
	private ClansAdmin _clanAdmin;
	private ClansGame _clanGame;
	private ClansBlocks _clanBlocks;
	private ClansRegions _clanRegions;
	private BlockRestore _blockRestore;
	private Teleport _teleport;
	private ConditionManager _condition;
	private ClassCombatShop _classShop;
	private HologramManager _hologramManager;
	private GearManager _gearManager;
	private LootManager _lootManager;
	private DonationManager _donationManager;
	private InventoryManager _inventoryManager;
	private NetherManager _netherManager;
	private DamageManager _damageManager;
	private SiegeManager _siegeManager;
	private IncognitoManager _incognitoManager;
	
	private ClansBlacklist _blacklist;

	private Playtime _playTracker;

	private TutorialManager _tutorial;
	
	private ClassManager _classManager;
	private BannerManager _bannerManager;
	private AmplifierManager _amplifierManager;
	private RestartManager _restartManager;
	
	private SafeLog _safeLog;
	
	public ClassManager getClassManager()
	{
		return _classManager;
	}
	
	private WarManager _warManager;
	private ProjectileManager _projectileManager;
	private WorldEventManager _worldEvent;
	private Chat _chat;
	private ItemMapManager _itemMapManager;
	private DisguiseManager _disguiseManager;
	private NpcManager _npcManager;
	private Explosion _explosion;
	private GoldManager _goldManager;
	private WarPointEvasion _warPointEvasion;
	private ObserverManager _observerManager;
	private Punish _punish;
	private TaskManager _taskManager;
	private PvPTimerManager _timerManager = require(PvPTimerManager.class);

	private int _inviteExpire = 2;
	private int _nameMin = 3;
	private int _nameMax = 10;
	private long _reclaimTime = 1800000;
	private long _onlineTime = 1200000;
	
	// Command Shop
	private ClanShop _clanShop;
	
	// Clans
	private NautHashMap<String, ClanInfo> _clanMap = new NautHashMap<String, ClanInfo>();
	// private NautHashMap<String, ClanInfo> _clanMemberNameMap = new
	// NautHashMap<String, ClanInfo>();
	private NautHashMap<UUID, ClanInfo> _clanMemberUuidMap = new NautHashMap<UUID, ClanInfo>();
	private NautHashMap<UUID, Pair<ClanInfo, Long>> _clanMemberLeftMap = new NautHashMap<>();
	private NautHashMap<ClaimLocation, ClanTerritory> _claimMap = new NautHashMap<>();
	private NautHashMap<ClaimLocation, Long> _unclaimMap = new NautHashMap<>();
	
	public String UserDataDir = UtilServer.getServer().getWorlds().get(0).getWorldFolder().getPath() + File.separator + ".." + File.separator + "CLANS_USER_DATA" + File.separator;
	
	private PacketHandler _packetHandler;
	
	public ClanTips ClanTips;
	
	private boolean _disabling = false;
	
	// Spawn area
	
	public ClansManager(JavaPlugin plugin, String serverName, IncognitoManager incognitoManager, PacketHandler packetHandler, Punish punish, CoreClientManager clientManager, DonationManager donationManager, PreferencesManager preferencesManager, BlockRestore blockRestore, StatsManager statsManager, Teleport teleport, Chat chat, GearManager gearManager, HologramManager hologramManager, InventoryManager inventoryManager)
	{
		super("Clans Manager", plugin);
		
		_instance = this;
		_punish = punish;
		
		_packetHandler = packetHandler;
		
		_incognitoManager = incognitoManager;
		_serverName = serverName;
		_clientManager = clientManager;
		_combatManager = require(CombatManager.class);
		_hologramManager = hologramManager;
		
		_chat = chat;
		_blockRestore = blockRestore;
		_teleport = teleport;
		_warManager = new WarManager(plugin, this);
		
		_donationManager = donationManager;
		_inventoryManager = inventoryManager;
		
		_blacklist = new ClansBlacklist(plugin);
		
		_gearManager = gearManager;
		_lootManager = new LootManager(gearManager);
		_disguiseManager = Managers.get(DisguiseManager.class);
		_npcManager = new NpcManager(plugin, Managers.get(Creature.class));
		_condition = new SkillConditionManager(plugin);
		_damageManager = new DamageManager(plugin, _combatManager, _npcManager, _disguiseManager, _condition);
		_condition.setDamageManager(_damageManager);
		
		_worldEvent = new WorldEventManager(plugin, this, _damageManager, _lootManager, blockRestore, _clanRegions);
		
		_taskManager = new TaskManager(plugin, _clientManager);
		
		ClanTips = new ClanTips(plugin, this, preferencesManager);
		
		// new MurderManager(plugin, this);
		
		_clanAdmin = new ClansAdmin(this);
		_clanBlocks = new ClansBlocks();
		_clanDisplay = new ClansDisplay(plugin, this);
		_clanGame = new ClansGame(plugin, this);
		_clanUtility = new ClansUtility(this);
		_tutorial = new TutorialManager(plugin, clientManager, donationManager, hologramManager, this, _npcManager, _taskManager);
		_itemMapManager = new ItemMapManager(this, _tutorial, _worldEvent);
		new TntGeneratorManager(plugin, this);
		new SupplyDropManager(plugin);
		
		new InvseeManager(this);
		new MenuManager(plugin);

		_explosion = Managers.get(Explosion.class);
		_warPointEvasion = new WarPointEvasion(plugin);

//		 new ClansLoginManager(getPlugin(), clientManager, _serverName);
		
		_clanShop = new ClanShop(this, clientManager, donationManager);
		
		Energy energy = new Energy(plugin);
		// TODO: Re-enable customtagfix with NCP update?
		// new CustomTagFix(plugin, packetHandler);
		
		new Field(plugin, Managers.get(Creature.class), _condition, this, energy, serverName);
		
		// Required managers to be initialized
		new Spawn(plugin, this);
		new NPCManager(this, _hologramManager);
		_safeLog = new SafeLog(plugin, this);
		_observerManager = new ObserverManager(plugin, _condition, this);
		
		new ClanEnergyTracker(plugin, this);
//		new StuckManager(this);
		
		new PotatoManager(plugin, this);
		
		new Weapon(plugin, energy);
		new Gameplay(plugin, this, blockRestore, _damageManager);
		new HiddenChestManager(this);
		_projectileManager = new ProjectileManager(plugin);
		Fire fire = new Fire(plugin, _condition, _damageManager);
		
		HashSet<String> itemIgnore = new HashSet<String>();
		itemIgnore.add("Proximity Explosive");
		itemIgnore.add("Proximity Zapper");
		
		ItemFactory itemFactory = new ItemFactory(plugin, blockRestore, _condition, _damageManager, energy, fire, _projectileManager, this, itemIgnore);
		SkillFactory skillManager = new SkillFactory(plugin, _damageManager, this, _combatManager, _condition, _projectileManager, _disguiseManager, blockRestore, fire, new Movement(plugin), teleport, energy);
		skillManager.RemoveSkill("Dwarf Toss", "Block Toss");
		skillManager.removeSkill("Whirlwind Axe");
		skillManager.removeSkill("Shield Smash");
		// Check if any Ice Prison blocks will be placed inside a safe zone or world event
		// fixme Is there any way of checking the destination beforehand?
		// Although if the user is trying to launch an Ice Prison into a safezone they should know better
		skillManager.GetSkill("Ice Prison").setLocationFilter(location ->
		{
			{
				ClanTerritory territory = _clanUtility.getClaim(location);
				if (territory != null && territory.Safe)
				{
					return false;
				}
			}
			{
				if (_worldEvent.isInEvent(location, true))
				{
					return false;
				}
			}
			
			return true;
		});
		((Blink)skillManager.GetSkill("Blink")).setAllowTrapping(true);

		Flash flashSkill = (Flash) skillManager.GetSkill("Flash");
		flashSkill.setAllowTrapping(true);
		flashSkill.setStartWithCharges(false);

		registerEvents(new Listener()
		{
			@EventHandler
			public void on(FissureModifyBlockEvent event)
			{
				Material targetType = event.getTargetBlock().getType();
				event.setCancelled(targetType == Material.POTATO || targetType == Material.CARROT);
			}

			@EventHandler
			public void on(CustomDamageEvent event)
			{
				if (event.GetCause() == EntityDamageEvent.DamageCause.CUSTOM
						&& event.GetDamageInitial() == 0.1
						&& event.GetDamageePlayer() != null)
				{
					Condition poisonShock = _condition.GetActiveCondition(event.GetDamageePlayer(), Condition.ConditionType.POISON_SHOCK);
					if (poisonShock != null)
					{
						event.SetIgnoreArmor(true);
					}
				}
			}
		});
		
		_classManager = new ClassManager(plugin, _clientManager, donationManager, skillManager, itemFactory);
		
		// Register redis based server commands
		ServerCommandManager.getInstance().registerCommandType(ClanDeleteCommand.class, new ClanDeleteCommandHandler());
		ServerCommandManager.getInstance().registerCommandType(ClanLoadCommand.class, new ClanLoadCommandHandler());

		EloManager eloManager = Managers.get(EloManager.class);
		AchievementManager achievementManager = Managers.get(AchievementManager.class);
		ClassShopManager shopManager = new ClassShopManager(plugin, _classManager, skillManager, itemFactory, achievementManager, _clientManager);
		_classShop = new ClassCombatShop(shopManager, _clientManager, donationManager, true, "Class Shop");
		
		new ClanEnergyManager(this);
		
		_playTracker = new Playtime(this, statsManager);

		_scoreboard = new ClansScoreboardManager(plugin, this, _warManager, _worldEvent, _tutorial, clientManager, donationManager);
		_clanDataAccess = new ClansDataAccessLayer(this, _scoreboard);
		
		HelmetPacketManager.getInstance();
		_bannerManager = new BannerManager(plugin);
		
		_goldManager = new GoldManager(this, _clientManager, donationManager, _clanDataAccess);

		for (ClanToken token : _clanDataAccess.getRepository().retrieveClans())
		{
			loadClan(token, false);
		}
		_bannerManager.loadBanners(this);

		require(PersonalServerManager.class);
		require(CommunityManager.class);

		Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "Replay|Restrict");


//		new ClaimVisualizer(plugin, this);
		
		// RedisDataRepository(ConnectionData writeConn, ConnectionData
		// readConn, Region region, Class<T> elementType, String elementLabel)
		// Initialize default region factions and territory
		// (spawn/fields/borderlands)
		_clanRegions = new ClansRegions(plugin, this);
		_clanRegions.initializeRegions();
		
		List<Location> jumpOffHolograms = Arrays.asList(
				// West Spawn
				new Location(Spawn.getSpawnWorld(), 8, 200, 359),
				new Location(Spawn.getSpawnWorld(), 34, 200, 390),
				new Location(Spawn.getSpawnWorld(), 8, 200, 418),
				new Location(Spawn.getSpawnWorld(), -25, 200, 390),
				
				// East Spawn
				new Location(Spawn.getSpawnWorld(), 34, 206, -393),
				new Location(Spawn.getSpawnWorld(), 8, 206, -365),
				new Location(Spawn.getSpawnWorld(), -25, 206, -393),
				new Location(Spawn.getSpawnWorld(), 8, 206, -424)
		);
		
		List<Location> welcomeHolograms = Arrays.asList(
				new Location(Spawn.getSpawnWorld(), 17, 200, 390),
				new Location(Spawn.getSpawnWorld(), 8, 200, 399),
				new Location(Spawn.getSpawnWorld(), 0, 200, 390),
				new Location(Spawn.getSpawnWorld(), 8, 200, 381),
				new Location(Spawn.getSpawnWorld(), 8, 206, -384),
				new Location(Spawn.getSpawnWorld(), 0, 206, -393),
				new Location(Spawn.getSpawnWorld(), 8, 206, -402),
				new Location(Spawn.getSpawnWorld(), 17, 206, -393)
		);
		
		for (Location location : jumpOffHolograms)
		{
			Hologram hologram = new Hologram(hologramManager, location, 
					C.cGreen + "Jump Off",
					C.cGreen + "to begin your Clans adventure!");
			hologram.start();
		}
		
		for (Location location : welcomeHolograms)
		{
			Hologram hologram = new Hologram(hologramManager, location, 
					C.cGreenB + "Welcome to Clans!", 
					C.cWhite + "Type " + C.cYellow + "/clan" + C.cWhite + " to get started!"
			);
			hologram.start();
		}


		_siegeManager = new SiegeManager(this);
		_netherManager = new NetherManager(this);
		_amplifierManager = new AmplifierManager(plugin);
		
		new MountManager(plugin, clientManager, donationManager);
		
		new BoxManager(plugin);
		
		new AltManager();
		
		_restartManager = new RestartManager(plugin);
		
		require(CashShopManager.class);
		
		require(AfkManager.class);
		
		generatePermissions();
		
		ServerOnlineMessage message = new ServerOnlineMessage();
		message.ServerName = UtilServer.getServerName();
		ClansQueueMessenger.getMessenger(UtilServer.getServerName()).transmitMessage(message, QueueConstant.SERVICE_MESSENGER_IDENTIFIER);
	}
	
	private void generatePermissions()
	{
		PermissionGroup.MOD.revokePermission(Teleport.Perm.TELEPORT_COMMAND);
		PermissionGroup.ADMIN.setPermission(Teleport.Perm.TELEPORT_COMMAND, true, true);

		PermissionGroup.PLAYER.setPermission(Perm.CLANS_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.ALLY_CHAT_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.CLAN_CHAT_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.MAP_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.SUICIDE_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.REGION_CLEAR_COMMAND, true, true);
		PermissionGroup.CMOD.setPermission(Perm.SPEED_COMMAND, false, true);
		PermissionGroup.ADMIN.setPermission(Perm.SPEED_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.FORCE_JOIN_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.AUTO_OP, true, true);
		if (UtilServer.isTestServer())
		{
			PermissionGroup.QAM.setPermission(Perm.AUTO_OP, false, true);
		}
		PermissionGroup.CONTENT.setPermission(Perm.PREFIX_SHOWN, true, true);
		PermissionGroup.CONTENT.setPermission(Perm.JOIN_FULL, true, true);
		PermissionGroup.BUILDER.setPermission(Perm.PREFIX_SHOWN, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.JOIN_FULL, true, true);
		PermissionGroup.PLAYER.setPermission(CoreClientManager.Perm.JOIN_FULL, true, true);
	}
	
	@Override
	public void addCommands()
	{
		addCommand(new ClansCommand(this));
		addCommand(new RegionsCommand(this));
		addCommand(new ClansChatCommand(this));
		addCommand(new ClansAllyChatCommand(this));
		addCommand(new MapCommand(this));
		addCommand(new SpeedCommand(this));
		addCommand(new KillCommand(this));
	}
	
	public void loadClan(ClanToken clanToken, boolean loadBanner)
	{
		ClanInfo clan = new ClanInfo(this, clanToken);
		_clanMap.put(clanToken.Name, clan);
		
		for (ClanMemberToken memberToken : clanToken.Members)
		{
			_clanMemberUuidMap.put(memberToken.PlayerUUID, clan);
			// _clanMemberMap.put(memberToken.Name, clan);
		}
		
		for (ClanTerritoryToken territoryToken : clanToken.Territories)
		{
			String[] split = territoryToken.Chunk.split(",");
			ClaimLocation location = ClaimLocation.of(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]));
			_claimMap.put(location, new ClanTerritory(ClaimLocation.fromStoredString(territoryToken.Chunk), territoryToken.ClanName, territoryToken.Safe));
		}

		if (loadBanner)
		{
			_bannerManager.loadBanner(clan);
		}
	}
	
	public void loadClan(ClanToken clanToken)
	{
		loadClan(clanToken, true);
	}
	
	public DisguiseManager getDisguiseManager()
	{
		return _disguiseManager;
	}

	public TutorialManager getTutorial()
	{
		return _tutorial;
	}
	
	public NpcManager getNPCManager()
	{
		return _npcManager;
	}
	
	public ClansRegions getClanRegions()
	{
		return _clanRegions;
	}
	
	public DonationManager getDonationManager()
	{
		return _donationManager;
	}

	public GoldManager getGoldManager()
	{
		return _goldManager;
	}
	
	public InventoryManager getInventoryManager()
	{
		return _inventoryManager;
	}
	
	public ItemMapManager getItemMapManager()
	{
		return _itemMapManager;
	}
	
	public Explosion getExplosion()
	{
		return _explosion;
	}
	
	public PacketHandler getPacketHandler()
	{
		return _packetHandler;
	}
	
	public BannerManager getBannerManager()
	{
		return _bannerManager;
	}
	
	public AmplifierManager getAmplifierManager()
	{
		return _amplifierManager;
	}
	
	public int getInviteExpire()
	{
		return _inviteExpire;
	}
	
	public NautHashMap<String, ClanInfo> getClanMap()
	{
		return _clanMap;
	}
	
	public Set<String> getClanNameSet()
	{
		return _clanMap.keySet();
	}
	
	public NautHashMap<UUID, ClanInfo> getClanMemberUuidMap()
	{
		return _clanMemberUuidMap;
	}
	
	public static boolean isClaimable(Location location)
	{
		int x = Math.abs(location.getBlockX());
		int z = Math.abs(location.getBlockZ());
		
		return (x <= CLAIMABLE_RADIUS && z <= CLAIMABLE_RADIUS) && !Spawn.getInstance().isSafe(location);
	}
	
	public boolean isFields(Location location)
	{
		return getClanUtility().isSpecial(location, "Fields");
	}
	
	public boolean canUnclaimChunk(ClanInfo stealer, ClanInfo owner)
	{
		return owner.getClaims() > owner.getClaimsMax() && !owner.isAdmin() && !owner.isAlly(stealer);
	}
	
	public ClanInfo getClan(Player player)
	{
		return getClan(player.getUniqueId());
	}
	
	public ClanInfo getClan(UUID uuid)
	{
		return _clanMemberUuidMap.get(uuid);
	}
	
	public boolean isInClan(Player player)
	{
		return _clanMemberUuidMap.containsKey(player.getUniqueId());
	}
	
	public ClanInfo getClan(String clan)
	{
		return _clanMap.get(clan);
	}
	
	/**
	 * @param clanName
	 * @return true, if a Clan with matching {@code clanName} exists, false
	 *         otherwise.
	 */
	public boolean clanExists(String clanName)
	{
		return getClan(clanName) != null;
	}
	
	public NautHashMap<ClaimLocation, ClanTerritory> getClaimMap()
	{
		return _claimMap;
	}
	
	public long lastPower = System.currentTimeMillis();
	
	@EventHandler
	public void displayHardcoreMode(ServerListPingEvent event)
	{
		if (Clans.HARDCORE)
		{
			event.setMotd("Hardcore");
		}
		else
		{
			event.setMotd("Casual");
		}
	}
	
	@EventHandler
	public void savePlayerActiveBuild(PlayerQuitEvent event)
	{
		if (_classManager.Get(event.getPlayer()) != null && _classManager.Get(event.getPlayer()).GetGameClass() != null)
		{
			CustomBuildToken activeBuild = _classManager.Get(event.getPlayer()).GetActiveCustomBuild(_classManager.Get(event.getPlayer()).GetGameClass());
			
			if (activeBuild == null) return;
			
			activeBuild.PlayerName = event.getPlayer().getName();
			
			// 0 is set aside for active build so we just dupe build to this row
			// whenever we update it.
			activeBuild.CustomBuildNumber = 0;
			_classManager.GetRepository().SaveCustomBuild(activeBuild);
		}
	}

	@EventHandler
	public void StaffIncognito(IncognitoStatusChangeEvent event)
	{
		if (event.getNewState())
		{
			UtilServer.broadcast(F.sys("Quit", event.getPlayer().getName()));
		}
		else
		{
			UtilServer.broadcast(F.sys("Join", event.getPlayer().getName()));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void Join(PlayerJoinEvent event)
	{
		event.setJoinMessage(null);
		UtilPlayer.message(event.getPlayer(), C.cDAquaB + "Welcome to Mineplex Clans!");

		if (_incognitoManager.Get(event.getPlayer()).Status)
		{
			return;
		}

        for (Player other : UtilServer.getPlayers())
        {
            if (_tutorial.inTutorial(other))
            {
                // Don't display join message if player in tutorial.
                continue;
            }

            other.sendMessage(F.sys("Join", event.getPlayer().getName()));
        }
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void Quit(PlayerQuitEvent event)
	{
		event.setQuitMessage(null);

		if (_incognitoManager.Get(event.getPlayer()).Status)
		{
			return;
		}
		
		for (Player other : UtilServer.getPlayers())
		{
			if (_tutorial.inTutorial(other))
			{
				// Don't display quit message if player in tutorial.
				continue;
			}

			other.sendMessage(F.sys("Quit", event.getPlayer().getName()));
		}
	}

	@EventHandler
	public void disableEnderpeal(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			ItemStack item = event.getPlayer().getItemInHand();
			if (item != null && item.getType() == Material.ENDER_PEARL) event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void BlockCreatureSpawn(CreatureSpawnCustomEvent event)
	{
		ClanInfo clan = _clanUtility.getOwner(event.GetLocation());
		
		if (clan != null && !clan.isAdmin() && !clan.getName().equals("Spawn") && event.getReason() != SpawnReason.CUSTOM)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void Interact(PlayerInteractEvent event)
	{
		getClanGame().Interact(event);
	}
	
	@EventHandler
	public void join(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		ClanInfo clanInfo = _clanMemberUuidMap.get(player.getUniqueId());
		if (clanInfo != null)
		{
			clanInfo.playerOnline(player);
		}

		if (_clientManager.Get(player).hasPermission(Perm.AUTO_OP))
		{
			player.setOp(true);
		}
		if (player.getInventory().getHelmet() != null) //Reset helmet to fix 1 damage bug
		{
			ItemStack helmet = player.getInventory().getHelmet().clone();
			player.getInventory().setHelmet(null);
			runSyncLater(() ->
			{
				player.getInventory().setHelmet(helmet);
			}, 20L);
		}
	}
	
	@EventHandler
	public void disallowReplayMod(PlayerJoinEvent event)
	{
		// happens 20 ticks later because player channels don't
		// seem to work immediately after joining.
		runSyncLater(() ->
		{
			ByteArrayDataOutput bado = ByteStreams.newDataOutput();
			
			bado.writeUTF("no_xray");
			bado.writeBoolean(true);
			
			bado.writeUTF("no_noclip");
			bado.writeBoolean(true);
			
			bado.writeUTF("only_recording_player");
			bado.writeBoolean(true);
			
			event.getPlayer().sendPluginMessage(_plugin, "Replay|Restrict", bado.toByteArray());
		}, 20L);
	}
	
	@EventHandler
	public void denyBow(EntityShootBowEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			Player player = ((Player) event.getEntity());
			ClientClass client = _classManager.Get(player);
			if (client.IsGameClass(IPvpClass.ClassType.Mage) || client.IsGameClass(IPvpClass.ClassType.Knight) || client.IsGameClass(IPvpClass.ClassType.Brute))
			{
				event.setCancelled(true);
				UtilPlayer.message(player, F.main("Clans", "You cannot use " + F.elem("Bow") + " as a " + F.elem(client.GetGameClass().GetName())));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void commandPreProcess(PlayerCommandPreprocessEvent event)
	{
		String[] messages = { "ver", "version", "pl", "plugins"};

		for (String message : messages)
		{
			if (!event.getMessage().equalsIgnoreCase("/" + message) && !event.getMessage().toLowerCase().startsWith("/" + message + " "))
			{
				continue;
			}

			UtilPlayer.message(event.getPlayer(), F.main("Clans", "Server is running Mineplex Clans version " + _plugin.getDescription().getVersion() + "!"));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void quit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		ClanInfo clanInfo = getClanMemberUuidMap().get(player.getUniqueId());
		if (clanInfo != null)
		{
			clanInfo.playerOffline(player);
		}
	}
	
	private void handleClanChat(Player player, String message, ClanInfo clan, String rank)
	{
		message = _chat.filterMessage(player, true, message);

		if (message.isEmpty())
		{
			return;
		}

		for (Player cur : clan.getOnlinePlayers())
		{
			UtilPlayer.message(cur, String.format(rank + C.cAqua + "%s " + C.cDAqua + "%s", player.getName(), message));
		}
	}
	
	private void handleAllyChat(Player player, String message, ClanInfo clan, String rank)
	{
		final String filtered = _chat.filterMessage(player, true, message);

		if (filtered.isEmpty())
		{
			return;
		}

		List<Player> recipients = new ArrayList<>(clan.getOnlinePlayers());
		
		for (String allyName : clan.getAllyMap().keySet())
		{
			ClanInfo ally = _clanUtility.getClanByClanName(allyName);
			if (ally == null) continue;

			recipients.addAll(ally.getOnlinePlayers());
		}

		recipients.forEach(p -> UtilPlayer.message(p, String.format(rank + C.cDGreen + clan.getName() + " " + C.cDGreen + "%s " + C.cGreen + "%s", player.getName(), filtered)));
		
		recipients.clear();
	}
	
	private void handleRegularChat(FormatPlayerChatEvent event, ClanInfo clan, String rank)
	{
		String message = _chat.filterMessage(event.getPlayer(), true, event.getMessage());

		if (message.isEmpty())
		{
			return;
		}

		if (clan == null)
		{
			for (Player other : event.getRecipients())
			{
				if (_tutorial.inTutorial(other))
				{
					continue;
				}
				
				UtilPlayer.message(other, String.format(rank + C.cYellow + "%s " + C.cWhite + "%s", event.getPlayer().getName(), message));
			}
			return;
		}
		
		List<Player> recipients = new ArrayList<>();

		for (Player other : event.getRecipients())
		{
			if (_tutorial.inTutorial(other))
			{
				continue;
			}

			ClanInfo otherClan = _clanUtility.getClanByPlayer(other);
			
			if (otherClan == null)
			{
				recipients.add(other);
			}
			else
			{
				ClanRelation rel = _clanUtility.rel(clan, otherClan);
				other.sendMessage(rank + rel.getColor(true) + clan.getName() + " " + rel.getColor(false) + event.getPlayer().getName() + " " + C.cWhite + message);
			}
		}
		
		recipients.forEach(p -> p.sendMessage(String.format(rank + C.cGold + clan.getName() + " " + C.cYellow + "%s " + C.cWhite + "%s", event.getPlayer().getName(), message)));
		
		recipients.clear();
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void disableObsidian(BlockBreakEvent event)
	{
		if (event.getBlock().getType().equals(Material.OBSIDIAN))
		{
			event.setCancelled(true);
			event.getBlock().setType(Material.AIR);
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event)
	{
		PunishClient punishclient = _punish.GetClient(event.getPlayer().getName());
        
		if (punishclient != null && punishclient.IsMuted())
		{
			for (int i = 0; i < event.getLines().length; i++)
			{
				event.setLine(i, "");
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void handlePlayerChat(FormatPlayerChatEvent event)
	{
		if (event.getChatChannel() == ChatChannel.COMMUNITY)
		{
			return;
		}

		ClientClan client = Get(event.getPlayer());
		
		if (client == null)
		{
			return;
		}
		
		ClanInfo clan = _clanUtility.getClanByPlayer(event.getPlayer());
		
		String rank = _clientManager.Get(event.getPlayer()).getPrimaryGroup().getDisplay(true, true, true, false) + " ";
		
		if (!_clientManager.Get(event.getPlayer()).hasPermission(Perm.PREFIX_SHOWN))
		{
			rank = "";
		}
		
		if (client.isClanChat() && clan != null)
		{
			handleClanChat(event.getPlayer(), event.getMessage(), clan, rank);
		}
		else if (client.isAllyChat() && clan != null)
		{
			handleAllyChat(event.getPlayer(), event.getMessage(), clan, rank);
		}
		else
		{
			handleRegularChat(event, clan, rank);
		}

		event.setCancelled(true);

		System.out.println((clan == null ? "" : clan.getName()) + " " + _clientManager.Get(event.getPlayer()).getPrimaryGroup().name() + " " + event.getPlayer().getName() + " " + event.getMessage());
	}
	
	
	public void messageClan(ClanInfo clan, String message)
	{
		for (Player player : clan.getOnlinePlayers())
		{
			UtilPlayer.message(player, message);
		}
	}
	
	public void sendTipToClan(ClanInfo clan, TipType tip)
	{
		for (Player player : clan.getOnlinePlayers())
		{
			ClanTips.displayTip(tip, player);
		}
	}
	
	public void middleTextClan(ClanInfo clan, String header, String footer)
	{
		middleTextClan(clan, header, footer, 20, 60, 20);
	}
	
	public void middleTextClan(ClanInfo clan, String header, String footer, int fadeInTicks, int stayTicks, int fadeOutTicks)
	{
		UtilTextMiddle.display(header, footer, fadeInTicks, stayTicks, fadeOutTicks, clan.getOnlinePlayersArray());
	}
	
	public void chatClan(ClanInfo clan, Player caller, String message)
	{
		String rank = _clientManager.Get(caller).getPrimaryGroup().getDisplay(true, true, true, false) + " ";
		
		if (!_clientManager.Get(caller).hasPermission(Perm.PREFIX_SHOWN))
		{
			rank = "";
		}
		
		handleClanChat(caller, message, clan, rank);
	}
	
	public void chatAlly(ClanInfo clan, Player caller, String message)
	{
		String rank = _clientManager.Get(caller).getPrimaryGroup().getDisplay(true, true, true, false) + " ";
		
		if (!_clientManager.Get(caller).hasPermission(Perm.PREFIX_SHOWN))
		{
			rank = "";
		}
		
		handleAllyChat(caller, message, clan, rank);
	}
	
	public int getNameMin()
	{
		return _nameMin;
	}
	
	public int getNameMax()
	{
		return _nameMax;
	}
	
	public long getReclaimTime()
	{
		return _reclaimTime;
	}
	
	public boolean canHurt(Player a, Player b)
	{
		if (a.equals(b)) return false;
		
		return _clanUtility.canHurt(a, b);
	}
	
	public boolean canHurt(String a, String b)
	{
		if (a.equals(b)) return false;
		
		return _clanUtility.canHurt(UtilPlayer.searchExact(a), UtilPlayer.searchExact(b));
	}
	
	public boolean isSafe(Player a)
	{
		return _clanUtility.isSafe(a);
	}
	
	// public ClanRelation getRelation(String playerA, String playerB)
	// {
	// return getClanUtility().rel(_clanMemberMap.get(playerA),
	// _clanMemberMap.get(playerB));
	// }
	
	public ClanRelation getRelation(Player playerA, Player playerB)
	{
		return getRelation(playerA.getUniqueId(), playerB.getUniqueId());
	}
	
	public ClanRelation getRelation(UUID playerA, UUID playerB)
	{
		return getClanUtility().rel(getClanMemberUuidMap().get(playerA), getClanMemberUuidMap().get(playerB));
	}
	
	public long getOnlineTime()
	{
		return _onlineTime;
	}
	
	public CombatManager getCombatManager()
	{
		return _combatManager;
	}
	
	public ClansUtility getClanUtility()
	{
		return _clanUtility;
	}
	
	@Override
	protected ClientClan addPlayer(UUID uuid)
	{
		return new ClientClan();
	}
	
	public BlockRestore getBlockRestore()
	{
		return _blockRestore;
	}
	
	public ClansDataAccessLayer getClanDataAccess()
	{
		return _clanDataAccess;
	}
	
	public Teleport getTeleport()
	{
		return _teleport;
	}
	
	public ClansDisplay getClanDisplay()
	{
		return _clanDisplay;
	}
	
	public NautHashMap<ClaimLocation, Long> getUnclaimMap()
	{
		return _unclaimMap;
	}
	
	public ClansAdmin getClanAdmin()
	{
		return _clanAdmin;
	}
	
	public ClansGame getClanGame()
	{
		return _clanGame;
	}
	
	public ClansBlocks getClanBlocks()
	{
		return _clanBlocks;
	}
	
	public String getServerName()
	{
		return _serverName;
	}
	
	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}
	
	public ConditionManager getCondition()
	{
		return _condition;
	}
	
	public ClassCombatShop getClassShop()
	{
		return _classShop;
	}
	
	public ClanShop getClanShop()
	{
		return _clanShop;
	}
	
	public WarManager getWarManager()
	{
		return _warManager;
	}
	
	public ProjectileManager getProjectile()
	{
		return _projectileManager;
	}
	
	public WorldEventManager getWorldEvent()
	{
		return _worldEvent;
	}
	
	public HologramManager getHologramManager()
	{
		return _hologramManager;
	}
	
	public GearManager getGearManager()
	{
		return _gearManager;
	}
	
	public LootManager getLootManager()
	{
		return _lootManager;
	}
	
	public Chat getChat()
	{
		return _chat;
	}

	public ClansScoreboardManager getScoreboard()
	{
		return _scoreboard;
	}

	/**
	 * Get the timezone for this server. This may be used in the future if we
	 * have clans servers with varying timezones.
	 * 
	 * @return {@link java.util.TimeZone} that this server should run at
	 */
	public TimeZone getServerTimeZone()
	{
		return TIME_ZONE;
	}
	
	@Override
	public void disable()
	{
		// Kind of confusing, Clans.java calls this so that we can pass the
		// disable event to WorldEventManager
		// This is so that we can prevent any permanent world changes with
		// events
		_disabling = true;
		_blockRestore.onDisable();
		_worldEvent.onDisable();
		_goldManager.onDisable();
		_playTracker.onDisable();
		_netherManager.onDisable();
		_safeLog.onDisable();
		_restartManager.onDisable();
		_observerManager.onDisable();
		Managers.get(MountManager.class).onDisable();
		Managers.get(SupplyDropManager.class).onDisable();
		ServerOfflineMessage message = new ServerOfflineMessage();
		message.ServerName = UtilServer.getServerName();
		ClansQueueMessenger.getMessenger(UtilServer.getServerName()).transmitMessage(message, QueueConstant.SERVICE_MESSENGER_IDENTIFIER);
	}
	
	@EventHandler
	public void transmitQueueStatus(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		
		int online = 0;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (_clientManager.Get(player).hasPermission(Perm.JOIN_FULL))
			{
				continue;
			}
			
			online++;
		}
		
		ClansServerStatusMessage message = new ClansServerStatusMessage();
		message.ServerName = UtilServer.getServerName();
		message.OpenSlots = Math.max(0, Bukkit.getMaxPlayers() - online);
		message.Online = !_restartManager.isRestarting() && !_disabling;
		ClansQueueMessenger.getMessenger(UtilServer.getServerName()).transmitMessage(message, QueueConstant.SERVICE_MESSENGER_IDENTIFIER);
	}
	
	@EventHandler
	public void hunger(FoodLevelChangeEvent event)
	{
		if (event.getFoodLevel() < ((Player) event.getEntity()).getFoodLevel() && _clanUtility.getClaim(event.getEntity().getLocation())!=null && _clanUtility.getClaim(event.getEntity().getLocation()).isSafe(event.getEntity().getLocation()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void hubCommand(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().toLowerCase().equals("/lobby") || event.getMessage().toLowerCase().equals("/hub") || event.getMessage().toLowerCase().equals("/leave"))
		{
			Portal.getInstance().sendPlayerToGenericServer(event.getPlayer(), GenericServer.CLANS_HUB, Intent.PLAYER_REQUEST);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void openShop(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().equals("/cgui"))
		{
			_clanShop.attemptShopOpen(event.getPlayer());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void updateBedStatus(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TWOSEC)
		{
			return;
		}
		
		for (String name : getClanNameSet())
		{
			ClanInfo clan = _clanUtility.getClanByClanName(name);
			
			if (clan.getHome() != null)
			{
				if (UtilBlock.isValidBed(clan.getHome()))
				{
					if (clan.getHome().clone().add(0, 1,0).getBlock().getType().equals(Material.AIR))
					{
						clan.setBedStatus(BedStatus.EXISTS_AND_UNOBSTRUCTED);
					}
					else
					{
						clan.setBedStatus(BedStatus.EXISTS_AND_OBSTRUCTED);
					}
				}
				else
				{
					clan.setBedStatus(BedStatus.DESTROYED);
				}
			}
			else
			{
				clan.setBedStatus(BedStatus.DOESNT_EXIST);
			}
		}
	}
	
	@EventHandler
	public void message(PlayerMessageEvent event)
	{
		if (!_tutorial.inTutorial(event.getPlayer()))
		{
			return;
		}
		
		if (event.getMessage().startsWith(C.cBlue + "Death>"))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void handleClansDeath(PlayerDeathEvent event)
	{
		PlayerClan playerClan;
		PlayerClan killerClan = null;
		
		Player player = event.getEntity();
		ClanInfo pClan = _clanMemberUuidMap.get(player.getUniqueId());
		playerClan = new PlayerClan(player, pClan);
		
		if (player.getKiller() != null)
		{
			Player killer = player.getKiller();
			ClanInfo kClan = _clanMemberUuidMap.get(killer.getUniqueId());
			killerClan = new PlayerClan(killer, kClan);
		}
		
		ClansPlayerDeathEvent clansPlayerDeathEvent = new ClansPlayerDeathEvent(event, playerClan, killerClan);
		Bukkit.getServer().getPluginManager().callEvent(clansPlayerDeathEvent);
	}

	public void justLeft(UUID uniqueId, ClanInfo clan)
	{
		_clanMemberLeftMap.put(uniqueId, Pair.create(clan, System.currentTimeMillis()));
	}

	public void resetLeftTimer(UUID uuid)
	{
		_clanMemberLeftMap.remove(uuid);
		_warPointEvasion.resetCooldown(uuid);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void setWalkSpeed(PlayerJoinEvent event)
	{
		event.getPlayer().setWalkSpeed(0.2f);
	}

	@EventHandler
	public void disableHorses(VehicleEnterEvent event)
	{
		if (event.getEntered() instanceof Player && event.getVehicle() instanceof Horse)
		{
			if (!Recharge.Instance.use((Player) event.getEntered(), "Ride Horse", 2 * 20L, true, false))
			{
				event.setCancelled(true);
			}
		}
	}

	public Pair<ClanInfo, Long> leftRecently(UUID uniqueId, long time)
	{
		if (_clanMemberLeftMap.containsKey(uniqueId) && (System.currentTimeMillis() - _clanMemberLeftMap.get(uniqueId).getRight()) <= time)
		{
			return Pair.create(_clanMemberLeftMap.get(uniqueId).getLeft(), time - (System.currentTimeMillis() - _clanMemberLeftMap.get(uniqueId).getRight()));
		}

		return null;
	}

	public ObserverManager getObserverManager()
	{
		return _observerManager;
	}


	public int getServerId()
	{
		return _clanDataAccess.getRepository().getServerId();
	}

	public NetherManager getNetherManager()
	{
		return _netherManager;
	}

	public void message(Player player, String message)
	{
		UtilPlayer.message(player, F.main("Clans", message));
	}

	public DamageManager getDamageManager()
	{
		return _damageManager;
	}

	public boolean hasTimer(Player player) 
	{ 
		return _timerManager.hasTimer(player); 
	}

	public ClansBlacklist getBlacklist()
	{
		return _blacklist;
	}

	public SiegeManager getSiegeManager()
	{
		return _siegeManager;
	}

	public IncognitoManager getIncognitoManager()
	{
		return _incognitoManager;
	}
}