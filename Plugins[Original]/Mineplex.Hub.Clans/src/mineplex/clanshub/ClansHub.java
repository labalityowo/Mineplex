package mineplex.clanshub;

import static mineplex.core.Managers.require;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.CustomTagFix;
import mineplex.core.PacketsInteractionFix;
import mineplex.core.account.CoreClientManager;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.antihack.AntiHack;
import mineplex.core.antihack.guardians.AntiHackGuardian;
import mineplex.core.antihack.guardians.GuardianManager;
import mineplex.core.aprilfools.AprilFoolsManager;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.chat.Chat;
import mineplex.core.command.CommandCenter;
import mineplex.core.common.events.ServerShutdownEvent;
import mineplex.core.creature.Creature;
import mineplex.core.customdata.CustomDataManager;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.elo.EloManager;
import mineplex.core.friend.FriendManager;
import mineplex.core.gadget.gadgets.particle.king.CastleManager;
import mineplex.core.give.Give;
import mineplex.core.hologram.HologramManager;
import mineplex.core.ignore.IgnoreManager;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.memory.MemoryFix;
import mineplex.core.message.MessageManager;
import mineplex.core.monitor.LagMeter;
import mineplex.core.npc.NpcManager;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.party.PartyManager;
import mineplex.core.pet.PetManager;
import mineplex.core.poll.PollManager;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.profileCache.ProfileCacheManager;
import mineplex.core.punish.Punish;
import mineplex.core.rankGiveaway.eternal.EternalGiveawayManager;
import mineplex.core.rankGiveaway.titangiveaway.TitanGiveawayManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.resourcepack.ResourcePackManager;
import mineplex.core.serverConfig.ServerConfiguration;
import mineplex.core.stats.StatsManager;
import mineplex.core.status.ServerStatusManager;
import mineplex.core.task.TaskManager;
import mineplex.core.teleport.Teleport;
import mineplex.core.thank.ThankManager;
import mineplex.core.titles.Titles;
import mineplex.core.titles.tracks.TrackManager;
import mineplex.core.twofactor.TwoFactorAuth;
import mineplex.core.updater.FileUpdater;
import mineplex.core.updater.Updater;
import mineplex.core.velocity.VelocityFix;
import mineplex.core.visibility.VisibilityManager;
import mineplex.core.website.WebsiteLinkManager;
import mineplex.minecraft.game.core.combat.CombatManager;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;

/**
 * Main class for clans hub
 */
public class ClansHub extends JavaPlugin
{
	private String WEB_CONFIG = "webServer";

	@Override
	public void onEnable()
	{
		Bukkit.setSpawnRadius(0);
		getConfig().addDefault(WEB_CONFIG, "http://accounts.mineplex.com/");
		getConfig().set(WEB_CONFIG, getConfig().getString(WEB_CONFIG));
		saveConfig();

		String webServerAddress = getConfig().getString(WEB_CONFIG);

		//Logger.initialize(this);

		//Velocity Fix
		new VelocityFix(this);

		//Static Modules
		require(ProfileCacheManager.class);
		CommandCenter.Initialize(this);
		CoreClientManager clientManager = new CoreClientManager(this);
		CommandCenter.Instance.setClientManager(clientManager);

//		new ProfileCacheManager(this);
		ItemStackFactory.Initialize(this, false);
		Recharge.Initialize(this);
		require(VisibilityManager.class);
		Give.Initialize(this);
		Punish punish = new Punish(this, clientManager);
		BlockRestore blockRestore = require(BlockRestore.class);
		DonationManager donationManager = require(DonationManager.class);

		ServerConfiguration serverConfiguration = new ServerConfiguration(this, clientManager);

		// Publish our server status now, to give us more time to start up
		ServerStatusManager serverStatusManager = new ServerStatusManager(this, clientManager, new LagMeter(this, clientManager));

		//Other Modules
		PacketHandler packetHandler = require(PacketHandler.class);
		DisguiseManager disguiseManager = require(DisguiseManager.class);
		IncognitoManager incognito = new IncognitoManager(this, clientManager, packetHandler);
		PreferencesManager preferenceManager = new PreferencesManager(this, incognito, clientManager);
		
		incognito.setPreferencesManager(preferenceManager);
		
		Creature creature = new Creature(this);
		NpcManager npcManager = new NpcManager(this, creature);
		InventoryManager inventoryManager = new InventoryManager(this, clientManager);
		HologramManager hologramManager = require(HologramManager.class);
		CastleManager castleManager = new CastleManager(this, clientManager, hologramManager, false);
		PetManager petManager = new PetManager(this, clientManager, donationManager, inventoryManager, disguiseManager, creature, blockRestore);
		PollManager pollManager = new PollManager(this, clientManager, donationManager);
		
		//Main Modules
		new TitanGiveawayManager(this, clientManager, serverStatusManager);

		Portal portal = new Portal();

		AntiHack antiHack = require(AntiHack.class);
		GuardianManager guardianManager = require(GuardianManager.class);

		for (int i = 0; i < 8; i++)
		{
			guardianManager.registerGuardian(new AntiHackGuardian(new Location(Bukkit.getWorld("world"), 0, 195, 0), 25, -8, 195, 185, 17, -16));
		}

        IgnoreManager ignoreManager = new IgnoreManager(this, clientManager, preferenceManager, portal);

		FriendManager friendManager = require(FriendManager.class);

		StatsManager statsManager = new StatsManager(this, clientManager);
		EloManager eloManager = new EloManager(this, clientManager);
		AchievementManager achievementManager = new AchievementManager(statsManager, clientManager, donationManager, incognito, eloManager);

		PartyManager partyManager = new PartyManager();

		CustomDataManager customDataManager = require(CustomDataManager.class);

		ConditionManager condition = new ConditionManager(this);
		ThankManager thankManager = new ThankManager(this, clientManager, donationManager);
		BoosterManager boosterManager = new BoosterManager(this, "", clientManager, donationManager, inventoryManager, thankManager);
		HubManager hubManager = new HubManager(this, blockRestore, clientManager, incognito, donationManager, inventoryManager, condition, disguiseManager, new TaskManager(this, clientManager), portal, partyManager, preferenceManager, petManager, pollManager, statsManager, achievementManager, hologramManager, npcManager, packetHandler, punish, serverStatusManager, customDataManager, thankManager, boosterManager, castleManager);

		ClansTransferManager serverManager = new ClansTransferManager(this, clientManager, donationManager, partyManager, portal);

		new MessageManager(this, incognito, clientManager, preferenceManager, ignoreManager, punish, friendManager, require(Chat.class));
		new MemoryFix(this);
		new FileUpdater(GenericServer.CLANS_HUB);
		new CustomTagFix(this, packetHandler);
		new PacketsInteractionFix(this, packetHandler);
		new ResourcePackManager(this, portal);

		AprilFoolsManager.getInstance();

		new EternalGiveawayManager(this, clientManager, serverStatusManager);

		CombatManager combatManager = require(CombatManager.class);
		
		DamageManager damage = new DamageManager(this, combatManager, npcManager, disguiseManager, condition);
		
		Teleport teleport = new Teleport(this, clientManager);

		//Updates
		require(Updater.class);

		require(TrackManager.class);
		require(Titles.class);
		require(TwoFactorAuth.class);
		require(WebsiteLinkManager.class);
	}

	@Override
	public void onDisable()
	{
		getServer().getPluginManager().callEvent(new ServerShutdownEvent(this));
	}
}