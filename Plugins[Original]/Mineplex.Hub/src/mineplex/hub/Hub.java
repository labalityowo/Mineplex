package mineplex.hub;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.CustomTagFix;
import mineplex.core.PacketsInteractionFix;
import mineplex.core.TwitchIntegrationFix;
import mineplex.core.account.CoreClientManager;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.admin.command.AdminCommands;
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
import mineplex.core.creature.Creature;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.elo.EloManager;
import mineplex.core.energy.Energy;
import mineplex.core.friend.FriendManager;
import mineplex.core.gadget.gadgets.particle.king.CastleManager;
import mineplex.core.game.MineplexGameManager;
import mineplex.core.game.kit.GameKit;
import mineplex.core.give.Give;
import mineplex.core.hologram.HologramManager;
import mineplex.core.ignore.IgnoreManager;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.memory.MemoryFix;
import mineplex.core.message.MessageManager;
import mineplex.core.monitor.LagMeter;
import mineplex.core.movement.Movement;
import mineplex.core.npc.NpcManager;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.party.PartyManager;
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
import mineplex.core.resourcepack.ResourcePackManager;
import mineplex.core.serverConfig.ServerConfiguration;
import mineplex.core.sound.SoundNotifier;
import mineplex.core.stats.StatsManager;
import mineplex.core.status.ServerStatusManager;
import mineplex.core.teamspeak.TeamspeakManager;
import mineplex.core.teleport.Teleport;
import mineplex.core.thank.ThankManager;
import mineplex.core.titles.Titles;
import mineplex.core.twofactor.TwoFactorAuth;
import mineplex.core.updater.FileUpdater;
import mineplex.core.updater.Updater;
import mineplex.core.velocity.VelocityFix;
import mineplex.core.visibility.VisibilityManager;
import mineplex.core.website.WebsiteLinkManager;
import mineplex.hub.server.ServerManager;
import mineplex.minecraft.game.classcombat.Class.ClassManager;
import mineplex.minecraft.game.classcombat.Condition.SkillConditionManager;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.item.ItemFactory;
import mineplex.minecraft.game.classcombat.shop.ClassCombatShop;
import mineplex.minecraft.game.classcombat.shop.ClassShopManager;
import mineplex.minecraft.game.core.IRelation;
import mineplex.minecraft.game.core.combat.CombatManager;
import mineplex.minecraft.game.core.damage.DamageManager;
import mineplex.minecraft.game.core.fire.Fire;

import static mineplex.core.Managers.require;

public class Hub extends JavaPlugin implements IRelation
{

	@Override
	public void onEnable()
	{
		Bukkit.setSpawnRadius(0);
		getConfig().addDefault(Constants.WEB_CONFIG_KEY, Constants.WEB_ADDRESS);
		getConfig().set(Constants.WEB_CONFIG_KEY, getConfig().getString(Constants.WEB_CONFIG_KEY));
		saveConfig();

		//Static Modules
		require(ProfileCacheManager.class);
		CommandCenter.Initialize(this);
		CoreClientManager clientManager = new CoreClientManager(this);
		CommandCenter.Instance.setClientManager(clientManager);

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

		HologramManager hologramManager = require(HologramManager.class);

		Creature creature = new Creature(this);
		creature.SetDisableCustomDrops(true);
		NpcManager npcManager = new NpcManager(this, creature);
		InventoryManager inventoryManager = new InventoryManager(this, clientManager);
		CastleManager castleManager = new CastleManager(this, clientManager, hologramManager, false);
		PetManager petManager = new PetManager(this, clientManager, donationManager, inventoryManager, disguiseManager, creature, blockRestore);
		PollManager pollManager = new PollManager(this, clientManager, donationManager);

		ProjectileManager throwManager = new ProjectileManager(this);

		Portal portal = new Portal();

		require(AntiHack.class);

		IgnoreManager ignoreManager = new IgnoreManager(this, clientManager, preferenceManager, portal);

		FriendManager friendManager = require(FriendManager.class);

		StatsManager statsManager = new StatsManager(this, clientManager);
		EloManager eloManager = new EloManager(this, clientManager);
		AchievementManager achievementManager = new AchievementManager(statsManager, clientManager, donationManager, incognito, eloManager);

		PartyManager partyManager = new PartyManager();

		SkillConditionManager conditionManager = new SkillConditionManager(this);

		String boosterGroup = serverConfiguration.getServerGroup().getBoosterGroup();
		ThankManager thankManager = new ThankManager(this, clientManager, donationManager);
		BoosterManager boosterManager = new BoosterManager(this, boosterGroup, clientManager, donationManager, inventoryManager, thankManager);
		HubManager hubManager = new HubManager(clientManager, donationManager, inventoryManager, disguiseManager, portal, partyManager, preferenceManager, petManager, pollManager, statsManager, achievementManager, hologramManager, npcManager, punish, thankManager, boosterManager);

		require(ServerManager.class);

		require(MineplexGameManager.class);

		new MessageManager(this, incognito, clientManager, preferenceManager, ignoreManager, punish, friendManager, require(Chat.class));
		new MemoryFix(this);
		new FileUpdater(GenericServer.HUB);
		new CustomTagFix(this, packetHandler);
		new PacketsInteractionFix(this, packetHandler);
		new ResourcePackManager(this, portal);

		SnapshotManager snapshotManager = new SnapshotManager(this, new SnapshotRepository(serverStatusManager.getCurrentServerName(), getLogger()));
		ReportManager reportManager = new ReportManager(this, snapshotManager, clientManager, incognito, punish, serverStatusManager.getRegion(), serverStatusManager.getCurrentServerName(), 3);
		new SnapshotPlugin(this, snapshotManager, clientManager);
		new ReportPlugin(this, reportManager);

		CombatManager combatManager = require(CombatManager.class);

		DamageManager damage = new DamageManager(this, combatManager, npcManager, disguiseManager, conditionManager);
		conditionManager.setDamageManager(damage);

		Fire fire = new Fire(this, conditionManager, damage);
		Teleport teleport = new Teleport(this, clientManager);
		Energy energy = new Energy(this);
		energy.setEnabled(false);

		ItemFactory itemFactory = new ItemFactory(this, blockRestore, conditionManager, damage, energy, fire, throwManager, this);
		SkillFactory skillManager = new SkillFactory(this, damage, this, combatManager, conditionManager, throwManager, disguiseManager, blockRestore, fire, new Movement(this), teleport, energy);
		ClassManager classManager = new ClassManager(this, clientManager, donationManager, hubManager.GetGadget(), skillManager, itemFactory);
		itemFactory.deregisterSelf();
		skillManager.deregisterSelf();

		ClassShopManager shopManager = new ClassShopManager(this, classManager, skillManager, itemFactory, achievementManager, clientManager);

		new ClassCombatShop(shopManager, clientManager, donationManager, false, "Brute", GameKit.CHAMPIONS_BRUTE, classManager.GetClass("Brute"), true);
		new ClassCombatShop(shopManager, clientManager, donationManager, false, "Mage", GameKit.CHAMPIONS_MAGE, classManager.GetClass("Mage"), true);
		new ClassCombatShop(shopManager, clientManager, donationManager, false, "Ranger",GameKit.CHAMPIONS_ARCHER,  classManager.GetClass("Ranger"), true);
		new ClassCombatShop(shopManager, clientManager, donationManager, false, "Knight", GameKit.CHAMPIONS_KNIGHT, classManager.GetClass("Knight"), true);
		new ClassCombatShop(shopManager, clientManager, donationManager, false, "Assassin", GameKit.CHAMPIONS_ASSASSIN, classManager.GetClass("Assassin"), true);

		//Velocity Fix
		new VelocityFix(this);

		//Updates
		require(Updater.class);

		require(Titles.class);
		require(TwoFactorAuth.class);
		require(TeamspeakManager.class);
		require(WebsiteLinkManager.class);
		require(TwitchIntegrationFix.class);
		require(SoundNotifier.class);

		new AdminCommands();
	}

	@Override
	public void onDisable()
	{
		getServer().getPluginManager().callEvent(new ServerShutdownEvent(this));
	}

	@Override
	public boolean canHurt(Player a, Player b)
	{
		return false;
	}

	@Override
	public boolean canHurt(String a, String b)
	{
		return false;
	}

	@Override
	public boolean isSafe(Player a)
	{
		return true;
	}
}
