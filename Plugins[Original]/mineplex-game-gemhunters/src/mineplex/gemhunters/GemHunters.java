package mineplex.gemhunters;

import net.minecraft.server.v1_8_R3.MinecraftServer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;

import mineplex.core.CustomTagFix;
import mineplex.core.FoodDupeFix;
import mineplex.core.account.CoreClientManager;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.antihack.AntiHack;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.chat.Chat;
import mineplex.core.chatsnap.SnapshotManager;
import mineplex.core.chatsnap.SnapshotPlugin;
import mineplex.core.chatsnap.SnapshotRepository;
import mineplex.core.command.CommandCenter;
import mineplex.core.common.Constants;
import mineplex.core.common.events.ServerShutdownEvent;
import mineplex.core.communities.CommunityManager;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.creature.Creature;
import mineplex.core.delayedtask.DelayedTask;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.playerdisguise.PlayerDisguiseManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.elo.EloManager;
import mineplex.core.explosion.Explosion;
import mineplex.core.friend.FriendManager;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.particle.king.CastleManager;
import mineplex.core.give.Give;
import mineplex.core.hologram.HologramManager;
import mineplex.core.ignore.IgnoreManager;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.memory.MemoryFix;
import mineplex.core.menu.MenuManager;
import mineplex.core.message.MessageManager;
import mineplex.core.monitor.LagMeter;
import mineplex.core.npc.NpcManager;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.party.PartyManager;
import mineplex.core.pet.PetManager;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.projectile.ProjectileManager;
import mineplex.core.punish.Punish;
import mineplex.core.recharge.Recharge;
import mineplex.core.report.ReportManager;
import mineplex.core.report.ReportPlugin;
import mineplex.core.serverConfig.ServerConfiguration;
import mineplex.core.stats.StatsManager;
import mineplex.core.status.ServerStatusManager;
import mineplex.core.task.TaskManager;
import mineplex.core.teleport.Teleport;
import mineplex.core.texttutorial.TextTutorialManager;
import mineplex.core.thank.ThankManager;
import mineplex.core.titles.Titles;
import mineplex.core.twofactor.TwoFactorAuth;
import mineplex.core.updater.FileUpdater;
import mineplex.core.updater.Updater;
import mineplex.core.visibility.VisibilityManager;
import mineplex.gemhunters.beta.BetaModule;
import mineplex.gemhunters.chat.ChatModule;
import mineplex.gemhunters.death.DeathModule;
import mineplex.gemhunters.death.quitnpc.QuitNPCModule;
import mineplex.gemhunters.economy.CashOutModule;
import mineplex.gemhunters.economy.EconomyModule;
import mineplex.gemhunters.join.JoinModule;
import mineplex.gemhunters.loot.InventoryModule;
import mineplex.gemhunters.loot.LootModule;
import mineplex.gemhunters.map.ItemMapModule;
import mineplex.gemhunters.moderation.ModerationModule;
import mineplex.gemhunters.mount.MountModule;
import mineplex.gemhunters.persistence.PersistenceModule;
import mineplex.gemhunters.playerstatus.PlayerStatusModule;
import mineplex.gemhunters.quest.QuestModule;
import mineplex.gemhunters.safezone.SafezoneModule;
import mineplex.gemhunters.scoreboard.ScoreboardModule;
import mineplex.gemhunters.shop.ShopModule;
import mineplex.gemhunters.spawn.SpawnModule;
import mineplex.gemhunters.supplydrop.SupplyDropModule;
import mineplex.gemhunters.tutorial.GemHuntersTutorial;
import mineplex.gemhunters.world.Leaderboards;
import mineplex.gemhunters.world.TimeCycle;
import mineplex.gemhunters.world.UndergroundMobs;
import mineplex.gemhunters.world.WorldListeners;
import mineplex.gemhunters.worldevent.WorldEventModule;
import mineplex.minecraft.game.core.combat.CombatManager;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;

import static mineplex.core.Managers.require;

/**
 * Gem Hunters main class <br>
 * 
 * TODO make documentation and a nice header
 * 
 * @author Sam
 */
public class GemHunters extends JavaPlugin
{

	@Override
	public void onEnable()
	{
		// Load configuration
		getConfig().addDefault(Constants.WEB_CONFIG_KEY, Constants.WEB_ADDRESS);
		getConfig().set(Constants.WEB_CONFIG_KEY, getConfig().getString(Constants.WEB_CONFIG_KEY));
		saveConfig();

		// Load core modules
		CommandCenter.Initialize(this);

		// Client Manager
		CoreClientManager clientManager = new CoreClientManager(this);

		// Donation Manager
		DonationManager donationManager = require(DonationManager.class);

		// Command Centre
		CommandCenter.Instance.setClientManager(clientManager);

		// ItemStacks
		ItemStackFactory.Initialize(this, false);

		// Delayed Tasks
		DelayedTask.Initialize(this);

		// Recharge
		Recharge.Initialize(this);

		// Visibility
		require(VisibilityManager.class);

		// Give
		Give.Initialize(this);

		// Server config
		ServerConfiguration serverConfig = new ServerConfiguration(this, clientManager);

		// Teleport
		new Teleport(this, clientManager);

		// Packets
		PacketHandler packetHandler = require(PacketHandler.class);

		// Vanish
		IncognitoManager incognito = new IncognitoManager(this, clientManager, packetHandler);

		// Preferences
		PreferencesManager preferenceManager = new PreferencesManager(this, incognito, clientManager);

		// Why do these depend on each other... :(
		incognito.setPreferencesManager(preferenceManager);

		// Server Status
		ServerStatusManager serverStatusManager = new ServerStatusManager(this, clientManager, new LagMeter(this, clientManager));

		// Portal
		Portal portal = new Portal();

		// File Updater
		new FileUpdater(GenericServer.HUB);

		// Punish
		Punish punish = new Punish(this, clientManager);

		// Disguises
		DisguiseManager disguiseManager = require(DisguiseManager.class);
		require(PlayerDisguiseManager.class);
		
		// Creatures
		Creature creature = new Creature(this);
		creature.SetDisableCustomDrops(true);

		// The old classic Damage Manager
		DamageManager damageManager = new DamageManager(this, require(CombatManager.class), new NpcManager(this, creature), disguiseManager, new ConditionManager(this));
		damageManager.SetEnabled(false);
		
		// GWEN
		AntiHack antiHack = require(AntiHack.class);
		Bukkit.getScheduler().runTask(this, () ->
		{
			antiHack.setStrict(true);
			antiHack.enableAnticheat();
		});

		// Block Restore
		BlockRestore blockRestore = require(BlockRestore.class);

		// Ignoring
		IgnoreManager ignoreManager = new IgnoreManager(this, clientManager, preferenceManager, portal);

		// Statistics
		StatsManager statsManager = new StatsManager(this, clientManager);

		// Elo
		EloManager eloManager = new EloManager(this, clientManager);

		// Achievements
		AchievementManager achievementManager = new AchievementManager(statsManager, clientManager, donationManager, incognito, eloManager);

		// Chat/Messaging
		new MessageManager(this, incognito, clientManager, preferenceManager, ignoreManager, punish, require(FriendManager.class), require(Chat.class));

		// Parties
		new PartyManager();

		// Communities
		require(CommunityManager.class);

		// Fixes
		new MemoryFix(this);
		new FoodDupeFix(this);

		// Explosions
		Explosion explosion = new Explosion(this, blockRestore);

		explosion.SetDebris(true);
		explosion.SetTemporaryDebris(false);

		// Inventories
		InventoryManager inventoryManager = new InventoryManager(this, clientManager);

		// Reports
		SnapshotManager snapshotManager = new SnapshotManager(this, new SnapshotRepository(serverStatusManager.getCurrentServerName(), getLogger()));
		new SnapshotPlugin(this, snapshotManager, clientManager);
		new ReportPlugin(this, new ReportManager(this, snapshotManager, clientManager, incognito, punish, serverStatusManager.getRegion(), serverStatusManager.getCurrentServerName(), 1));

		// Tag fix
		new CustomTagFix(this, packetHandler);

		// Holograms
		HologramManager hologramManager = require(HologramManager.class);

		// Menus
		new MenuManager(this);

		// Gadgets
		CastleManager castleManager = new CastleManager(this, clientManager, hologramManager, false);
		PetManager petManager = new PetManager(this, clientManager, donationManager, inventoryManager, disguiseManager, creature, blockRestore);
		ProjectileManager projectileManager = new ProjectileManager(this);
		GadgetManager gadgetManager = require(GadgetManager.class);
		ThankManager thankManager = new ThankManager(this, clientManager, donationManager);
		BoosterManager boosterManager = new BoosterManager(this, null, clientManager, donationManager, inventoryManager, thankManager);
		CosmeticManager cosmeticManager = new CosmeticManager(this, clientManager, donationManager, inventoryManager, gadgetManager, petManager, null, boosterManager, punish);

		cosmeticManager.setActive(false);
		cosmeticManager.setHideParticles(true);
		cosmeticManager.disableTeamArmor();

		// Tutorials
		TextTutorialManager tutorialManager = new TextTutorialManager(this, donationManager, new TaskManager(this, clientManager));
		tutorialManager.addTutorial(new GemHuntersTutorial());

		require(Titles.class).forceDisable();

		// Now we finally get to enable the Gem Hunters modules
		// Though if any other module needs one of these it will be generated in
		// order, however they are all here just for good measure.
		require(BetaModule.class);
		require(CashOutModule.class);
		require(ChatModule.class);
		require(DeathModule.class);
		require(EconomyModule.class);
		require(InventoryModule.class);
		require(LootModule.class);
		require(ItemMapModule.class);
		require(JoinModule.class);
		require(ModerationModule.class);
		require(MountModule.class);
		require(PersistenceModule.class);
		require(PlayerStatusModule.class);
		require(QuestModule.class);
		require(QuitNPCModule.class);
		require(SafezoneModule.class);
		require(ScoreboardModule.class);
		require(SpawnModule.class);
		require(ShopModule.class);
		require(SupplyDropModule.class);
		require(WorldEventModule.class);

		// An arbitrary collection of world listeners such as block place/break,
		// interact events etc...
		new WorldListeners(this);
		new TimeCycle(this);
		new UndergroundMobs(this);
		new Leaderboards();
		
		// UpdateEvent!!!
		require(Updater.class);

		// Disable spigot's item merging
		for (World world : getServer().getWorlds())
		{
			((CraftWorld) world).getHandle().spigotConfig.itemMerge = 0;
		}

		// Turn off the server's debugging
		MinecraftServer.getServer().getPropertyManager().setProperty("debug", false);
		SpigotConfig.debug = false;

		// Two-factor auth
		require(TwoFactorAuth.class);
	}

	@Override
	public void onDisable()
	{
		getServer().getPluginManager().callEvent(new ServerShutdownEvent(this));
	}
}