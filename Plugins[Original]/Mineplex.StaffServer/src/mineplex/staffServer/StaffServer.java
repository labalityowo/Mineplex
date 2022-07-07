package mineplex.staffServer;

import java.util.UUID;

import net.minecraft.server.v1_8_R3.MinecraftServer;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;

import com.mojang.authlib.GameProfile;

import mineplex.core.account.CoreClientManager;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.bonuses.BonusRepository;
import mineplex.core.command.CommandCenter;
import mineplex.core.common.Constants;
import mineplex.core.creature.Creature;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.elo.EloManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.memory.MemoryFix;
import mineplex.core.monitor.LagMeter;
import mineplex.core.npc.NpcManager;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Portal;
import mineplex.core.powerplayclub.PowerPlayClubRepository;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.profileCache.ProfileCacheManager;
import mineplex.core.punish.Punish;
import mineplex.core.recharge.Recharge;
import mineplex.core.stats.StatsManager;
import mineplex.core.status.ServerStatusManager;
import mineplex.core.updater.FileUpdater;
import mineplex.core.updater.Updater;
import mineplex.staffServer.customerSupport.CustomerSupport;
import mineplex.staffServer.salespackage.SalesPackageManager;

import static mineplex.core.Managers.require;

public class StaffServer extends JavaPlugin
{
	@Override
	public void onEnable()
	{
		getConfig().addDefault(Constants.WEB_CONFIG_KEY, Constants.WEB_ADDRESS);
		getConfig().set(Constants.WEB_CONFIG_KEY, getConfig().getString(Constants.WEB_CONFIG_KEY));
		saveConfig();

		//Static Modules
		CommandCenter.Initialize(this); 
		CoreClientManager clientManager = new CoreClientManager(this);
		CommandCenter.Instance.setClientManager(clientManager);
		Recharge.Initialize(this);
		
		DonationManager donationManager = require(DonationManager.class);
		
		Punish punish = new Punish(this, clientManager);
		new NpcManager(this, new Creature(this));
		ServerStatusManager serverStatusManager = new ServerStatusManager(this, clientManager, new LagMeter(this, clientManager));
		PreferencesManager preferenceManager = new PreferencesManager(this, null, clientManager);

		Portal portal = new Portal();
		EloManager eloManager = new EloManager(this, clientManager);
		StatsManager statsManager = new StatsManager(this, clientManager);
		InventoryManager inventoryManager = new InventoryManager(this, clientManager);
		BonusRepository bonusRepository = new BonusRepository(this, null, donationManager);
		new AchievementManager(statsManager, clientManager, donationManager, null, eloManager);
		new MemoryFix(this);  
		new FileUpdater(GenericServer.HUB);

		require(PacketHandler.class);
		require(DisguiseManager.class);

		PowerPlayClubRepository powerPlayRepo = new PowerPlayClubRepository(this, clientManager, donationManager);

		SalesPackageManager salesPackageManager = new SalesPackageManager(this, clientManager, donationManager, inventoryManager, statsManager, powerPlayRepo);

		new CustomerSupport(this, clientManager, donationManager, powerPlayRepo, inventoryManager, bonusRepository);
		
		//Updates
		require(Updater.class);
		
		MinecraftServer.getServer().getPropertyManager().setProperty("debug", false);
		SpigotConfig.debug = false;
		
		Bukkit.getWorlds().get(0).setSpawnLocation(0, 102, 0);
		
		((CraftServer)getServer()).setWhitelist(true);
		
		((CraftServer)getServer()).getHandle().addWhitelist(new GameProfile(UUID.fromString("377bdea3-badc-448d-81c1-65db43b17ea4"), "Strutt20"));
		((CraftServer)getServer()).getHandle().addWhitelist(new GameProfile(UUID.fromString("cf1b629c-cc55-4eb4-be9e-3ca86dfc7b9d"), "mannalou"));
		((CraftServer)getServer()).getHandle().addWhitelist(new GameProfile(UUID.fromString("492ff708-fe76-4c5a-b9ed-a747b5fa20a0"), "cherdy"));
		((CraftServer)getServer()).getHandle().addWhitelist(new GameProfile(UUID.fromString("6edf17d5-6bb2-4ed9-92e9-bed8e96fff68"), "BlueBeetleHD"));
		((CraftServer)getServer()).getHandle().addWhitelist(new GameProfile(UUID.fromString("a47a4d04-9f51-44ba-9d35-8de6053e9289"), "AlexTheCoder"));
		((CraftServer)getServer()).getHandle().addWhitelist(new GameProfile(UUID.fromString("63ad2db3-7c62-4a10-ac58-d267973190ce"), "Crumplex"));
		((CraftServer)getServer()).getHandle().addWhitelist(new GameProfile(UUID.fromString("a20d59d1-cfd8-4116-ac27-45d9c7eb4a97"), "Artix"));
		((CraftServer)getServer()).getHandle().addWhitelist(new GameProfile(UUID.fromString("852a8acf-7337-40d7-99ec-b08fd99650b5"), "KingCrazy_"));
		((CraftServer)getServer()).getHandle().addWhitelist(new GameProfile(UUID.fromString("d514022f-f6e3-4fb0-8d8c-90a6c2802711"), "sjsampson"));
		((CraftServer)getServer()).getHandle().addWhitelist(new GameProfile(UUID.fromString("627070a4-c6e0-46a4-a6b8-97f440dc37b4"), "Toki"));

		((CraftServer)getServer()).getHandle().addOp(new GameProfile(UUID.fromString("a47a4d04-9f51-44ba-9d35-8de6053e9289"), "AlexTheCoder"));
		((CraftServer)getServer()).getHandle().addOp(new GameProfile(UUID.fromString("cf1b629c-cc55-4eb4-be9e-3ca86dfc7b9d"), "mannalou"));
		((CraftServer)getServer()).getHandle().addOp(new GameProfile(UUID.fromString("377bdea3-badc-448d-81c1-65db43b17ea4"), "Strutt20"));
		((CraftServer)getServer()).getHandle().addOp(new GameProfile(UUID.fromString("6edf17d5-6bb2-4ed9-92e9-bed8e96fff68"), "BlueBeetleHD"));
		
		require(ProfileCacheManager.class);
	}
}
