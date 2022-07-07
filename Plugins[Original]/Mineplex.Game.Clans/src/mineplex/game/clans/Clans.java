package mineplex.game.clans;

import static mineplex.core.Managers.require;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;

import mineplex.core.CustomTagFix;
import mineplex.core.FoodDupeFix;
import mineplex.core.account.CoreClientManager;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.antihack.AntiHack;
import mineplex.core.antihack.guardians.GuardianManager;
import mineplex.core.aprilfools.AprilFoolsManager;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.chat.Chat;
import mineplex.core.chatsnap.SnapshotManager;
import mineplex.core.chatsnap.SnapshotPlugin;
import mineplex.core.chatsnap.SnapshotRepository;
import mineplex.core.command.CommandCenter;
import mineplex.core.common.Constants;
import mineplex.core.common.MinecraftVersion;
import mineplex.core.common.Pair;
import mineplex.core.common.events.ServerShutdownEvent;
import mineplex.core.creature.Creature;
import mineplex.core.delayedtask.DelayedTask;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.elo.EloManager;
import mineplex.core.explosion.Explosion;
import mineplex.core.fallingblock.FallingBlocks;
import mineplex.core.friend.FriendManager;
import mineplex.core.give.Give;
import mineplex.core.hologram.HologramManager;
import mineplex.core.ignore.IgnoreManager;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.memory.MemoryFix;
import mineplex.core.message.MessageManager;
import mineplex.core.monitor.LagMeter;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.punish.Punish;
import mineplex.core.rankGiveaway.eternal.EternalGiveawayManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.report.ReportManager;
import mineplex.core.report.ReportPlugin;
import mineplex.core.resourcepack.ResourcePackManager;
import mineplex.core.serverConfig.ServerConfiguration;
import mineplex.core.stats.StatsManager;
import mineplex.core.status.ServerStatusManager;
import mineplex.core.teleport.Teleport;
import mineplex.core.twofactor.TwoFactorAuth;
import mineplex.core.updater.FileUpdater;
import mineplex.core.updater.Updater;
import mineplex.core.visibility.VisibilityManager;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.freeze.ClansFreezeManager;
import mineplex.game.clans.items.GearManager;
import mineplex.game.clans.shop.building.BuildingShop;
import mineplex.game.clans.shop.farming.FarmingShop;
import mineplex.game.clans.shop.mining.MiningShop;
import mineplex.game.clans.shop.pvp.PvpShop;
import mineplex.game.clans.spawn.travel.TravelShop;
import mineplex.game.clans.world.WorldManager;
import net.minecraft.server.v1_8_R3.MinecraftServer;

public class Clans extends JavaPlugin
{

	private static final String MAP = "Season 5.5";
	public static boolean HARDCORE = false;

	// Modules
	private CoreClientManager _clientManager;
	private DonationManager _donationManager;
	private ClansManager _clansManager;

	@Override
	public void onEnable()
	{
		try
		{
			HARDCORE = new File(new File(".").getCanonicalPath() + File.separator + "Hardcore.dat").exists();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		Bukkit.setSpawnRadius(0);

		// Configs
		getConfig().addDefault(Constants.WEB_CONFIG_KEY, Constants.WEB_ADDRESS);
		getConfig().set(Constants.WEB_CONFIG_KEY, getConfig().getString(Constants.WEB_CONFIG_KEY));
		saveConfig();

		// Logger.initialize(this);

		// Static Modules
		CommandCenter.Initialize(this);
		_clientManager = new CoreClientManager(this);
		CommandCenter.Instance.setClientManager(_clientManager);

		ItemStackFactory.Initialize(this, false);

		DelayedTask.Initialize(this);

		Recharge.Initialize(this);
		require(VisibilityManager.class);
		// new ProfileCacheManager(this);

		_donationManager = require(DonationManager.class);

		new FallingBlocks(this);

		new ServerConfiguration(this, _clientManager);

		PacketHandler packetHandler = require(PacketHandler.class);
		IncognitoManager incognito = new IncognitoManager(this, _clientManager, packetHandler);
		PreferencesManager preferenceManager = new PreferencesManager(this, incognito, _clientManager);

		incognito.setPreferencesManager(preferenceManager);

		ServerStatusManager serverStatusManager = new ServerStatusManager(this, _clientManager, new LagMeter(this, _clientManager));

		// TODO: Add spawn locations to a configuration file of some sort?
		Give.Initialize(this);

		Teleport teleport = new Teleport(this, _clientManager);
		Portal portal = new Portal();
		new FileUpdater(GenericServer.CLANS_HUB);

		ClansFreezeManager clansFreeze = new ClansFreezeManager(this, _clientManager);

		Punish punish = new Punish(this, _clientManager, true);

		DisguiseManager disguiseManager = require(DisguiseManager.class);
		Creature creature = new Creature(this);

		AntiHack antiHack = require(AntiHack.class);
		GuardianManager guardianManager = require(GuardianManager.class);

		Bukkit.getScheduler().runTask(this, antiHack::enableAnticheat);

		new EternalGiveawayManager(this, _clientManager, serverStatusManager);

		BlockRestore blockRestore = require(BlockRestore.class);

		IgnoreManager ignoreManager = new IgnoreManager(this, _clientManager, preferenceManager, portal);

		StatsManager statsManager = new StatsManager(this, _clientManager);
		EloManager eloManager = new EloManager(this, _clientManager);
		AchievementManager achievementManager = new AchievementManager(statsManager, _clientManager, _donationManager, incognito, eloManager);
		Chat chat = require(Chat.class);
		new MessageManager(this, incognito, _clientManager, preferenceManager, ignoreManager, punish, require(FriendManager.class), chat);

		new MemoryFix(this);
		new FoodDupeFix(this);
		new Explosion(this, blockRestore);
		InventoryManager inventory = new InventoryManager(this, _clientManager);
		ResourcePackManager resourcePackManager = new ResourcePackManager(this, portal);
		resourcePackManager.setResourcePack(new Pair[]
				{
						//Pair.create(MinecraftVersion.Version1_8, "http://file.mineplex.com/ResClans.zip"),
						//Pair.create(MinecraftVersion.Version1_9, "http://file.mineplex.com/ResClans19.zip")
						Pair.create(MinecraftVersion.Version1_8, "http://198.20.72.74/ResClans.zip"),
						Pair.create(MinecraftVersion.Version1_9, "http://198.20.72.74/ResClans19.zip")
				}, true);

		SnapshotManager snapshotManager = new SnapshotManager(this, new SnapshotRepository(serverStatusManager.getCurrentServerName(), getLogger()));
		new SnapshotPlugin(this, snapshotManager, _clientManager);
		new ReportPlugin(this, new ReportManager(this, snapshotManager, _clientManager, incognito, punish, serverStatusManager.getRegion(), serverStatusManager.getCurrentServerName(), 1));

		// Enable custom-gear related managers
		new CustomTagFix(this, packetHandler);
		GearManager customGear = new GearManager(this, packetHandler, _clientManager, _donationManager);

		HologramManager hologram = require(HologramManager.class);
		_clansManager = new ClansManager(this, serverStatusManager.getCurrentServerName(), incognito, packetHandler, punish, _clientManager, _donationManager, preferenceManager, blockRestore, statsManager, teleport, chat, customGear, hologram, inventory);
		new Recipes(this);
		new Farming(this);
		new BuildingShop(_clansManager, _clientManager, _donationManager);
		new PvpShop(_clansManager, _clientManager, _donationManager);
		new FarmingShop(_clansManager, _clientManager, _donationManager);
		new TravelShop(_clansManager, _clientManager, _donationManager);
		new MiningShop(_clansManager, _clientManager, _donationManager);
		new WorldManager(this);

		require(TwoFactorAuth.class);

		AprilFoolsManager.getInstance();

		// Disable spigot item merging
		for (World world : getServer().getWorlds())
		{
			// Disable item merging
			((CraftWorld) world).getHandle().spigotConfig.itemMerge = 0;
		}

		//Updates
		require(Updater.class);

		MinecraftServer.getServer().getPropertyManager().setProperty("debug", false);
		SpigotConfig.debug = false;
	}

	public static String prettifyName(Material material)
	{
		String name = "";
		String[] words = material.toString().split("_");

		for (String word : words)
		{
			word = word.toLowerCase();
			name += word.substring(0, 1).toUpperCase() + word.substring(1) + " ";
		}

		return name;
	}

	@Override
	public void onDisable()
	{
		// Need to notify WorldEventManager of server shutdown, this seemed like
		// the only decent way to do it
		_clansManager.onDisable();

		getServer().getPluginManager().callEvent(new ServerShutdownEvent(this));
	}

	public static String getMap()
	{
		return MAP;
	}
}