package nautilus.game.arcade;

import java.io.File;

import net.minecraft.server.v1_8_R3.MinecraftServer;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;

import mineplex.core.CustomTagFix;
import mineplex.core.FoodDupeFix;
import mineplex.core.PacketsInteractionFix;
import mineplex.core.TwitchIntegrationFix;
import mineplex.core.account.CoreClientManager;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.antihack.AntiHack;
import mineplex.core.antihack.RelationProvider;
import mineplex.core.antihack.logging.AntihackLogger;
import mineplex.core.aprilfools.AprilFoolsManager;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.blood.Blood;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.chat.Chat;
import mineplex.core.chatsnap.SnapshotManager;
import mineplex.core.chatsnap.SnapshotPlugin;
import mineplex.core.chatsnap.SnapshotRepository;
import mineplex.core.command.CommandCenter;
import mineplex.core.common.Constants;
import mineplex.core.common.events.ServerShutdownEvent;
import mineplex.core.common.util.FileUtil;
import mineplex.core.common.util.UtilServer;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.creature.Creature;
import mineplex.core.customdata.CustomDataManager;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.elo.EloManager;
import mineplex.core.friend.FriendManager;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.particle.king.CastleManager;
import mineplex.core.give.Give;
import mineplex.core.hologram.HologramManager;
import mineplex.core.ignore.IgnoreManager;
import mineplex.core.imagemap.CustomItemFrames;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.memory.MemoryFix;
import mineplex.core.message.MessageManager;
import mineplex.core.monitor.LagMeter;
import mineplex.core.npc.NpcManager;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.pet.PetManager;
import mineplex.core.poll.PollManager;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.profileCache.ProfileCacheManager;
import mineplex.core.projectile.ProjectileManager;
import mineplex.core.punish.Punish;
import mineplex.core.recharge.Recharge;
import mineplex.core.report.ReportManager;
import mineplex.core.report.ReportPlugin;
import mineplex.core.serverConfig.ServerConfiguration;
import mineplex.core.sound.SoundNotifier;
import mineplex.core.stats.StatsManager;
import mineplex.core.status.ServerStatusManager;
import mineplex.core.teleport.Teleport;
import mineplex.core.thank.ThankManager;
import mineplex.core.twofactor.TwoFactorAuth;
import mineplex.core.updater.FileUpdater;
import mineplex.core.updater.Updater;
import mineplex.core.velocity.VelocityFix;
import mineplex.core.visibility.VisibilityManager;
import mineplex.core.website.WebsiteLinkManager;
import mineplex.minecraft.game.core.combat.CombatManager;
import mineplex.minecraft.game.core.damage.DamageManager;

import nautilus.game.arcade.anticheatmetadata.GameInfoMetadata;
import nautilus.game.arcade.game.GameServerConfig;
import static mineplex.core.Managers.require;

public class Arcade extends JavaPlugin
{

	private ArcadeManager _gameManager;
	 
	private ServerConfiguration _serverConfiguration;

	@Override     
	public void onEnable() 
	{
		getServer().getServicesManager().register(RelationProvider.class, (player, target) ->
		{
			if (target instanceof Player)
			{
				return _gameManager.canHurt(player, (Player) target);
			}
			else
			{
				return target instanceof LivingEntity && _gameManager.GetGame() != null && _gameManager.GetGame().IsLive();
			}
		}, this, ServicePriority.Normal);

		Bukkit.setSpawnRadius(0);
		//Delete Old Games Folders
		DeleteFolders();
 
		//Configs
		getConfig().addDefault(Constants.WEB_CONFIG_KEY, Constants.WEB_ADDRESS);
		getConfig().set(Constants.WEB_CONFIG_KEY, getConfig().getString(Constants.WEB_CONFIG_KEY));
		saveConfig();

		//Logger.initialize(this);

		//Static Modules
		CommandCenter.Initialize(this);
		CoreClientManager clientManager = new CoreClientManager(this);
		CommandCenter.Instance.setClientManager(clientManager);
		require(ProfileCacheManager.class);

		
		ItemStackFactory.Initialize(this, false);  
		Recharge.Initialize(this);   
		require(VisibilityManager.class);
		Give.Initialize(this);

		// Publish our server status now, to give us more time to start up
		ServerStatusManager serverStatusManager = new ServerStatusManager(this, clientManager, new LagMeter(this, clientManager));

		//Velocity Fix
		new VelocityFix(this);

		DonationManager donationManager = require(DonationManager.class);

		_serverConfiguration = new ServerConfiguration(this, clientManager);
		
		PacketHandler packetHandler = require(PacketHandler.class);

		IncognitoManager incognito = new IncognitoManager(this, clientManager, packetHandler);
		PreferencesManager preferenceManager = new PreferencesManager(this, incognito, clientManager);

		incognito.setPreferencesManager(preferenceManager);

		Creature creature = new Creature(this);
		Teleport teleport = new Teleport(this, clientManager);
		Portal portal = new Portal();
		new FileUpdater(GenericServer.HUB);
		
		DisguiseManager disguiseManager = require(DisguiseManager.class);

		NpcManager npcmanager = new NpcManager(this, creature);
		DamageManager damageManager = new DamageManager(this, require(CombatManager.class), npcmanager, disguiseManager, null);

		Punish punish = new Punish(this, clientManager);

		require(AntiHack.class);

        IgnoreManager ignoreManager = new IgnoreManager(this, clientManager, preferenceManager, portal);
		StatsManager statsManager = new StatsManager(this, clientManager);
		EloManager eloManager = new EloManager(this, clientManager);
		AchievementManager achievementManager = new AchievementManager(statsManager, clientManager, donationManager, incognito, eloManager);
        FriendManager friendManager = require(FriendManager.class);
        Chat chat = require(Chat.class);
        new MessageManager(this, incognito, clientManager, preferenceManager, ignoreManager, punish, friendManager, chat);

		SnapshotManager snapshotManager = new SnapshotManager(this, new SnapshotRepository(serverStatusManager.getCurrentServerName(), getLogger()));
		ReportManager reportManager = new ReportManager(this, snapshotManager, clientManager, incognito, punish, serverStatusManager.getRegion(), serverStatusManager.getCurrentServerName(), 1);
		new SnapshotPlugin(this, snapshotManager, clientManager);
		new ReportPlugin(this, reportManager);

		BlockRestore blockRestore = require(BlockRestore.class);
		
		ProjectileManager projectileManager = new ProjectileManager(this);
		HologramManager hologramManager = require(HologramManager.class);
		
		//Inventory
		InventoryManager inventoryManager = new InventoryManager(this, clientManager);
		CastleManager castleManager = new CastleManager(this, clientManager, hologramManager, false);
		PetManager petManager = new PetManager(this, clientManager, donationManager, inventoryManager, disguiseManager, creature, blockRestore);
		GadgetManager gadgetManager = require(GadgetManager.class);
		ThankManager thankManager = new ThankManager(this, clientManager, donationManager);
		BoosterManager boosterManager = new BoosterManager(this, _serverConfiguration.getServerGroup().getBoosterGroup(), clientManager, donationManager, inventoryManager, thankManager);
		CosmeticManager cosmeticManager = new CosmeticManager(this, clientManager, donationManager, inventoryManager, gadgetManager, petManager, null, boosterManager, punish);
		cosmeticManager.setInterfaceSlot(6);
		gadgetManager.setActiveItemSlot(3);
		cosmeticManager.disableTeamArmor();
		achievementManager.setGadgetManager(gadgetManager);

		CustomDataManager customDataManager = require(CustomDataManager.class);

		//Arcade Manager  
		PollManager pollManager = new PollManager(this, clientManager, donationManager);
		_gameManager = new ArcadeManager(this, serverStatusManager, ReadServerConfig(), clientManager, donationManager, damageManager, statsManager, incognito, achievementManager, disguiseManager, creature, teleport, new Blood(this), chat, portal, preferenceManager, inventoryManager, packetHandler, cosmeticManager, projectileManager, petManager, hologramManager, pollManager, npcmanager, customDataManager, punish, eloManager, thankManager, boosterManager);

		require(AntihackLogger.class).registerMetadata(new GameInfoMetadata());

		//new BroadcastManager(this, _gameManager);
		
		new MemoryFix(this);
		new CustomTagFix(this, packetHandler);
		new PacketsInteractionFix(this, packetHandler);
		new FoodDupeFix(this);

		require(TwoFactorAuth.class);
		require(WebsiteLinkManager.class);
		require(TwitchIntegrationFix.class);
		
		AprilFoolsManager.getInstance();

		require(CustomItemFrames.class);
		require(SoundNotifier.class);

		//Updates
		require(Updater.class);

		MinecraftServer.getServer().getPropertyManager().setProperty("debug", false);
		SpigotConfig.debug = false;
	}

	@Override 
	public void onDisable() 
	{
		for (Player player : UtilServer.getPlayers())
			player.kickPlayer("Server Shutdown");

		if (_gameManager.GetGame() != null)
			if (_gameManager.GetGame().WorldData != null)
				_gameManager.GetGame().WorldData.Uninitialize();

		getServer().getPluginManager().callEvent(new ServerShutdownEvent(this));
	}

	public GameServerConfig ReadServerConfig() 
	{
		GameServerConfig config = new GameServerConfig();

		try
		{
			config.ServerGroup = _serverConfiguration.getServerGroup().getName();
			config.BoosterGroup = _serverConfiguration.getServerGroup().getBoosterGroup();
			config.HostName = _serverConfiguration.getServerGroup().getHost();
			config.ServerType = _serverConfiguration.getServerGroup().getServerType();
			config.MinPlayers = _serverConfiguration.getServerGroup().getMinPlayers();
			config.MaxPlayers = _serverConfiguration.getServerGroup().getMaxPlayers();
			config.Uptimes = _serverConfiguration.getServerGroup().getUptimes();
			config.Tournament = _serverConfiguration.getServerGroup().getTournament();
			config.TournamentPoints = _serverConfiguration.getServerGroup().getTournamentPoints();
			config.TeamRejoin = _serverConfiguration.getServerGroup().getTeamRejoin();
			config.TeamAutoJoin = _serverConfiguration.getServerGroup().getTeamAutoJoin();
			config.TeamForceBalance = _serverConfiguration.getServerGroup().getTeamForceBalance();
			config.GameAutoStart = _serverConfiguration.getServerGroup().getGameAutoStart();
			config.GameTimeout = _serverConfiguration.getServerGroup().getGameTimeout();
			config.RewardGems = _serverConfiguration.getServerGroup().getRewardGems();
			config.RewardItems = _serverConfiguration.getServerGroup().getRewardItems();
			config.RewardStats = _serverConfiguration.getServerGroup().getRewardStats();
			config.RewardAchievements = _serverConfiguration.getServerGroup().getRewardAchievements();
			config.HotbarInventory = _serverConfiguration.getServerGroup().getHotbarInventory();
			config.HotbarHubClock = _serverConfiguration.getServerGroup().getHotbarHubClock();
			config.PlayerKickIdle = _serverConfiguration.getServerGroup().getPlayerKickIdle();
			config.HardMaxPlayerCap = _serverConfiguration.getServerGroup().getHardMaxPlayerCap();
			config.GameVoting = _serverConfiguration.getServerGroup().getGameVoting();
			config.MapVoting = _serverConfiguration.getServerGroup().getMapVoting();

			for (String gameName : _serverConfiguration.getServerGroup().getGames().split(","))
			{
				try
				{
					System.out.println("Found GameType: " + gameName);
					GameType type = GameType.valueOf(gameName);
					config.GameList.add(type);
				}
				catch (IllegalArgumentException e)
				{
					System.out.println("Error reading GameType values : " + gameName);
				}
			}

			for (String gameMode : _serverConfiguration.getServerGroup().getModes().split(","))
			{
				System.out.println("Found GameMode: " + gameMode);
				config.GameModeList.add(gameMode);
			}
		}
		catch (Exception ex)
		{
			System.out.println("Error reading ServerConfiguration values : " + ex.getMessage());
		}

		if (!config.IsValid())
			config = GetDefaultConfig();

		return config;
	}

	public GameServerConfig GetDefaultConfig()
	{
		GameServerConfig config = new GameServerConfig();

		config.ServerType = "Minigames";
		config.MinPlayers = 8;
		config.MaxPlayers = 16;
		config.Tournament = false;

		return config;
	}

	public ArcadeManager getArcadeManager()
	{
		return _gameManager;
	}

	private void DeleteFolders() 
	{
		File curDir = new File(".");

		File[] filesList = curDir.listFiles();
		for(File file : filesList)
		{
			if (!file.isDirectory())
				continue;

			if (file.getName().length() < 4)
				continue;

			if (!file.getName().substring(0, 4).equalsIgnoreCase("Game"))
				continue;

			FileUtil.DeleteFolder(file);

			System.out.println("Deleted Old Game: " + file.getName());
		}
	}
}
