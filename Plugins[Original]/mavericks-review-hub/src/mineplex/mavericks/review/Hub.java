package mineplex.mavericks.review;

import net.minecraft.server.v1_8_R3.MinecraftServer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;

import mineplex.core.CustomTagFix;
import mineplex.core.FoodDupeFix;
import mineplex.core.PacketsInteractionFix;
import mineplex.core.account.CoreClientManager;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.antihack.AntiHack;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.chat.Chat;
import mineplex.core.command.CommandCenter;
import mineplex.core.common.Constants;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.creature.Creature;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.elo.EloManager;
import mineplex.core.friend.FriendManager;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.particle.king.CastleManager;
import mineplex.core.give.Give;
import mineplex.core.hologram.HologramManager;
import mineplex.core.ignore.IgnoreManager;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.mavericks.MavericksApprovedRepository;
import mineplex.core.mavericks.MavericksBuildRepository;
import mineplex.core.memory.MemoryFix;
import mineplex.core.message.MessageManager;
import mineplex.core.monitor.LagMeter;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.pet.PetManager;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.profileCache.ProfileCacheManager;
import mineplex.core.projectile.ProjectileManager;
import mineplex.core.punish.Punish;
import mineplex.core.recharge.Recharge;
import mineplex.core.serverConfig.ServerConfiguration;
import mineplex.core.stats.StatsManager;
import mineplex.core.status.ServerStatusManager;
import mineplex.core.thank.ThankManager;
import mineplex.core.treasure.TreasureManager;
import mineplex.core.updater.FileUpdater;
import mineplex.core.updater.Updater;
import mineplex.core.velocity.VelocityFix;
import mineplex.core.visibility.VisibilityManager;

import static mineplex.core.Managers.require;

/**
 * Main JavaPlugin class for this plugin. Initializes the rest of this plugin.
 */
public class Hub extends JavaPlugin
{
	// Modules
	private CoreClientManager _clientManager;
	private DonationManager _donationManager;

	@Override
	public void onEnable()
	{
		Bukkit.setSpawnRadius(0);
		// Delete Old Games Folders

		// Configs
		getConfig().addDefault(Constants.WEB_CONFIG_KEY, Constants.WEB_ADDRESS);
		getConfig().set(Constants.WEB_CONFIG_KEY, getConfig().getString(Constants.WEB_CONFIG_KEY));
		saveConfig();

		// Static Modules
		CommandCenter.Initialize(this);
		_clientManager = new CoreClientManager(this);
		CommandCenter.Instance.setClientManager(_clientManager);

		ItemStackFactory.Initialize(this, false);
		Recharge.Initialize(this);
		require(VisibilityManager.class);
		Give.Initialize(this);

		// Velocity Fix
		new VelocityFix(this);

		_donationManager = require(DonationManager.class);

		PacketHandler packetHandler = require(PacketHandler.class);

		IncognitoManager incognito = new IncognitoManager(this, _clientManager, packetHandler);
		PreferencesManager preferenceManager = new PreferencesManager(this, incognito, _clientManager);

		incognito.setPreferencesManager(preferenceManager);

		Creature creature = new Creature(this);
		ServerStatusManager serverStatusManager = new ServerStatusManager(this, _clientManager, new LagMeter(this, _clientManager));
		Portal portal = new Portal();
		new FileUpdater(GenericServer.HUB);

		DisguiseManager disguiseManager = require(DisguiseManager.class);

		Punish punish = new Punish(this, _clientManager);
		require(AntiHack.class);

		IgnoreManager ignoreManager = new IgnoreManager(this, _clientManager, preferenceManager, portal);
		StatsManager statsManager = new StatsManager(this, _clientManager);
		EloManager eloManager = new EloManager(this, _clientManager);
		AchievementManager achievementManager = new AchievementManager(statsManager, _clientManager, _donationManager, incognito, eloManager);
		FriendManager friendManager = require(FriendManager.class);
		new MessageManager(this, incognito, _clientManager, preferenceManager, ignoreManager, punish, friendManager, require(Chat.class));

		BlockRestore blockRestore = require(BlockRestore.class);

		ProjectileManager projectileManager = new ProjectileManager(this);
		HologramManager hologramManager = require(HologramManager.class);
		
		ServerConfiguration serverConfiguration = new ServerConfiguration(this, _clientManager);

		// Inventory
		CastleManager castleManager = new CastleManager(this, _clientManager, hologramManager, false);
		InventoryManager inventoryManager = new InventoryManager(this, _clientManager);
		PetManager petManager = new PetManager(this, _clientManager, _donationManager, inventoryManager, disguiseManager, creature, blockRestore);
		GadgetManager gadgetManager = require(GadgetManager.class);
		ThankManager thankManager = new ThankManager(this, _clientManager, _donationManager);
		BoosterManager boosterManager = new BoosterManager(this, serverConfiguration.getServerGroup().getBoosterGroup(), _clientManager, _donationManager, inventoryManager, thankManager);
		CosmeticManager cosmeticManager = new CosmeticManager(this, _clientManager, _donationManager, inventoryManager, gadgetManager, petManager, require(TreasureManager.class), boosterManager, punish);
		cosmeticManager.setInterfaceSlot(7);
		cosmeticManager.disableTeamArmor();

		new MemoryFix(this);
		new CustomTagFix(this, packetHandler);
		new PacketsInteractionFix(this, packetHandler);
		new FoodDupeFix(this);
		
		new MavericksReviewManager(this, new MavericksBuildRepository(), new MavericksApprovedRepository());

		require(ProfileCacheManager.class);
		
		new SimpleChatManager(this, _clientManager, achievementManager);

		// Updates
		require(Updater.class);

		MinecraftServer.getServer().getPropertyManager().setProperty("debug", false);
		SpigotConfig.debug = false;
	}
}