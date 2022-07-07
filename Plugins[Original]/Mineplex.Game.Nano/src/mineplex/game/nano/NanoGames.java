package mineplex.game.nano;

import static mineplex.core.Managers.require;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.CustomTagFix;
import mineplex.core.FoodDupeFix;
import mineplex.core.PacketsInteractionFix;
import mineplex.core.TwitchIntegrationFix;
import mineplex.core.account.CoreClientManager;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.admin.command.AdminCommands;
import mineplex.core.antihack.RelationProvider;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.blood.Blood;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.chat.Chat;
import mineplex.core.chatsnap.SnapshotManager;
import mineplex.core.chatsnap.SnapshotPlugin;
import mineplex.core.chatsnap.SnapshotRepository;
import mineplex.core.command.CommandCenter;
import mineplex.core.common.Constants;
import mineplex.core.communities.CommunityManager;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.creature.Creature;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.playerdisguise.PlayerDisguiseManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.elo.EloManager;
import mineplex.core.friend.FriendManager;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.give.Give;
import mineplex.core.ignore.IgnoreManager;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.memory.MemoryFix;
import mineplex.core.menu.MenuManager;
import mineplex.core.message.MessageManager;
import mineplex.core.monitor.LagMeter;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.party.PartyManager;
import mineplex.core.pet.PetManager;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.profileCache.ProfileCacheManager;
import mineplex.core.punish.Punish;
import mineplex.core.recharge.Recharge;
import mineplex.core.report.ReportManager;
import mineplex.core.report.ReportPlugin;
import mineplex.core.serverConfig.ServerConfiguration;
import mineplex.core.sound.SoundNotifier;
import mineplex.core.stats.StatsManager;
import mineplex.core.status.ServerStatusManager;
import mineplex.core.task.TaskManager;
import mineplex.core.teamspeak.TeamspeakManager;
import mineplex.core.teleport.Teleport;
import mineplex.core.thank.ThankManager;
import mineplex.core.titles.Titles;
import mineplex.core.treasure.TreasureManager;
import mineplex.core.twofactor.TwoFactorAuth;
import mineplex.core.updater.FileUpdater;
import mineplex.core.updater.Updater;
import mineplex.core.velocity.VelocityFix;
import mineplex.core.visibility.VisibilityManager;
import mineplex.core.website.WebsiteLinkManager;
import mineplex.game.nano.game.Game;
import mineplex.minecraft.game.core.combat.CombatManager;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;

public class NanoGames extends JavaPlugin
{

	private NanoManager _gameManager;

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
				Game game = _gameManager.getGame();
				return target instanceof LivingEntity && game != null && game.isLive();
			}
		}, this, ServicePriority.Normal);

		Bukkit.setSpawnRadius(0);
		getConfig().addDefault(Constants.WEB_CONFIG_KEY, Constants.WEB_ADDRESS);
		getConfig().set(Constants.WEB_CONFIG_KEY, getConfig().getString(Constants.WEB_CONFIG_KEY));
		saveConfig();

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
		ServerStatusManager serverStatusManager = new ServerStatusManager(this, clientManager, new LagMeter(this, clientManager));

		PacketHandler packetHandler = require(PacketHandler.class);
		DisguiseManager disguiseManager = require(DisguiseManager.class);
		require(PlayerDisguiseManager.class);
		IncognitoManager incognito = new IncognitoManager(this, clientManager, packetHandler);
		PreferencesManager preferenceManager = new PreferencesManager(this, incognito, clientManager);

		incognito.setPreferencesManager(preferenceManager);

		Creature creature = new Creature(this);
		creature.SetDisableCustomDrops(true);
		InventoryManager inventoryManager = new InventoryManager(this, clientManager);
		PetManager petManager = new PetManager(this, clientManager, donationManager, inventoryManager, disguiseManager, creature, blockRestore);

		Portal portal = new Portal();

		new Teleport(this, clientManager);

		IgnoreManager ignoreManager = new IgnoreManager(this, clientManager, preferenceManager, portal);

		FriendManager friendManager = require(FriendManager.class);

		StatsManager statsManager = new StatsManager(this, clientManager);
		EloManager eloManager = new EloManager(this, clientManager);
		AchievementManager achievementManager = new AchievementManager(statsManager, clientManager, donationManager, incognito, eloManager);
		TaskManager taskManager = new TaskManager(this, clientManager);

		PartyManager partyManager = new PartyManager();

		String boosterGroup = serverConfiguration.getServerGroup().getBoosterGroup();
		ThankManager thankManager = new ThankManager(this, clientManager, donationManager);
		BoosterManager boosterManager = new BoosterManager(this, boosterGroup, clientManager, donationManager, inventoryManager, thankManager);

		CosmeticManager cosmeticManager = new CosmeticManager(this, clientManager, donationManager, inventoryManager, require(GadgetManager.class), petManager, require(TreasureManager.class), boosterManager, punish);
		cosmeticManager.setInterfaceSlot(7);

		new MessageManager(this, incognito, clientManager, preferenceManager, ignoreManager, punish, friendManager, require(Chat.class));
		new MemoryFix(this);
		new MenuManager(this);
		new FileUpdater(GenericServer.HUB);
		new CustomTagFix(this, packetHandler);
		new PacketsInteractionFix(this, packetHandler);

		SnapshotManager snapshotManager = new SnapshotManager(this, new SnapshotRepository(serverStatusManager.getCurrentServerName(), getLogger()));
		ReportManager reportManager = new ReportManager(this, snapshotManager, clientManager, incognito, punish, serverStatusManager.getRegion(), serverStatusManager.getCurrentServerName(), 3);
		new SnapshotPlugin(this, snapshotManager, clientManager);
		new ReportPlugin(this, reportManager);
		new VelocityFix(this);
		new FoodDupeFix(this);

		CombatManager combatManager = require(CombatManager.class);
		ConditionManager conditionManager = new ConditionManager(this);
		DamageManager damage = new DamageManager(this, combatManager, null, disguiseManager, conditionManager);
		conditionManager.setDamageManager(damage);
		new Blood(this);

		require(CommunityManager.class);
		require(Updater.class);
		require(Titles.class).forceDisable();
		require(TwoFactorAuth.class);
		require(TeamspeakManager.class);
		require(WebsiteLinkManager.class);
		require(TwitchIntegrationFix.class);
		require(SoundNotifier.class);

		new AdminCommands();

		_gameManager = require(NanoManager.class);
	}
}
