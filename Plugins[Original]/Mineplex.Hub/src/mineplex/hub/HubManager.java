package mineplex.hub;

import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.Managers;
import mineplex.core.MiniClientPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.antispam.AntiSpamManager;
import mineplex.core.benefit.BenefitManager;
import mineplex.core.bonuses.BonusManager;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.botspam.BotSpamManager;
import mineplex.core.chat.Chat;
import mineplex.core.chat.format.LevelFormatComponent;
import mineplex.core.chat.format.RankFormatComponent;
import mineplex.core.common.generator.VoidGenerator;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.communities.CommunityManager;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.playerdisguise.PlayerDisguiseManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.GadgetBlockEvent;
import mineplex.core.gadget.event.GadgetCollideEntityEvent;
import mineplex.core.gadget.event.GadgetSelectLocationEvent;
import mineplex.core.game.rejoin.GameRejoinManager;
import mineplex.core.hologram.HologramManager;
import mineplex.core.incognito.events.IncognitoHidePlayerEvent;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.menu.MenuManager;
import mineplex.core.message.PrivateMessageEvent;
import mineplex.core.mission.MissionManager;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.notifier.NotificationManager;
import mineplex.core.npc.NpcManager;
import mineplex.core.party.PartyManager;
import mineplex.core.pet.PetManager;
import mineplex.core.poll.PollManager;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.punish.Punish;
import mineplex.core.scoreboard.ScoreboardManager;
import mineplex.core.scoreboard.TabListSorter;
import mineplex.core.stats.StatsManager;
import mineplex.core.thank.ThankManager;
import mineplex.core.treasure.TreasureManager;
import mineplex.core.twofactor.TwoFactorAuth;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.world.MineplexWorld;
import mineplex.core.youtube.YoutubeManager;
import mineplex.hub.commands.GadgetToggle;
import mineplex.hub.doublejump.JumpManager;
import mineplex.hub.gimmicks.AdminPunch;
import mineplex.hub.gimmicks.SecretAreas;
import mineplex.hub.gimmicks.staffbuild.StaffBuild;
import mineplex.hub.hubgame.HubGameManager;
import mineplex.hub.kit.HubKitManager;
import mineplex.hub.modules.ForcefieldManager;
import mineplex.hub.modules.HubVisibilityManager;
import mineplex.hub.modules.salesannouncements.SalesAnnouncementManager;
import mineplex.hub.news.NewsManager;
import mineplex.hub.parkour.ParkourManager;
import mineplex.hub.player.CreativeManager;
import mineplex.hub.player.HubPlayerManager;
import mineplex.hub.plugin.HubPlugin;
import mineplex.hub.scoreboard.HubScoreboard;
import mineplex.hub.world.HubWorldManager;
import mineplex.minecraft.game.core.combat.DeathMessageType;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

public class HubManager extends MiniClientPlugin<HubClient>
{
	public enum Perm implements Permission
	{
		GADGET_TOGGLE_COMMAND,
		LIST_COMMAND,
		GAMEMODE_COMMAND,
		AUTO_OP,
		VANISH,
		SPAWN_PM,
		JOIN_FULL,
	}

	// ☃❅ Snowman!

	private final CoreClientManager _clientManager;
	private final DonationManager _donationManager;
	private final DisguiseManager _disguiseManager;
	private final PartyManager _partyManager;
	private final Portal _portal;
	private final GadgetManager _gadgetManager;
	private final HubVisibilityManager _visibilityManager;
	private final PreferencesManager _preferences;
	private final AchievementManager _achievementManager;
	private final PetManager _petManager;
	private final JumpManager _jumpManager;
	private final HologramManager _hologramManager;
	private final ParkourManager _parkourManager;
	private final HubGameManager _hubGameManager;
	private final MissionManager _missionManager;

	private final HubPlugin _hubPlugin;

	private final MineplexWorld _worldData;
	private final Location _spawn;
	private final List<Location> _lookAt;

	private boolean _shuttingDown;

	public HubManager(CoreClientManager clientManager, DonationManager donationManager, InventoryManager inventoryManager, DisguiseManager disguiseManager, Portal portal, PartyManager partyManager, PreferencesManager preferences, PetManager petManager, PollManager pollManager, StatsManager statsManager, AchievementManager achievementManager, HologramManager hologramManager, NpcManager npcManager, Punish punish, ThankManager thankManager, BoosterManager boosterManager)
	{
		super("Hub Manager");

		_clientManager = clientManager;
		_donationManager = donationManager;
		_disguiseManager = disguiseManager;

		_portal = portal;

		World world = Bukkit.getWorld("world");

		_worldData = new MineplexWorld(world);
		_spawn = _worldData.getSpongeLocation("SPAWN");
		_lookAt = _worldData.getSpongeLocations("LOOK_AT");

		// Disable item merging
		WorldServer nmsWorld = ((CraftWorld) _spawn.getWorld()).getHandle();
		nmsWorld.spigotConfig.itemMerge = 0;

		require(NewsManager.class);
		require(CreativeManager.class);
		require(HubWorldManager.class);
		require(HubKitManager.class);

		new BenefitManager(_plugin, clientManager, inventoryManager);
		_gadgetManager = require(GadgetManager.class);
		achievementManager.setGadgetManager(_gadgetManager);

		YoutubeManager youtubeManager = new YoutubeManager(_plugin, clientManager, donationManager);

		new BonusManager(_plugin, null, clientManager, donationManager, pollManager, npcManager, hologramManager, statsManager, inventoryManager, petManager, youtubeManager, _gadgetManager, thankManager, "Carl");

		TreasureManager treasureManager = require(TreasureManager.class);
		new CosmeticManager(_plugin, clientManager, donationManager, inventoryManager, _gadgetManager, petManager, treasureManager, boosterManager, punish);

		for (Location location : _worldData.getSpongeLocations("TREASURE CHEST"))
		{
			treasureManager.addTreasureLocation(location);
		}

		//new MavericksManager(_plugin, cosmeticManager, hologramManager, this);

		//new SoccerManager(this, _gadgetManager);

		new MenuManager(_plugin);
		require(AntiSpamManager.class);
		require(Chat.class).setFormatComponents(
				new LevelFormatComponent(achievementManager),
				new RankFormatComponent(clientManager),
				player ->
				{
					TextComponent component = new TextComponent(player.getName());
					component.setColor(ChatColor.YELLOW);
					return component;
				}
		);

		_petManager = petManager;
		_partyManager = partyManager;
		_preferences = preferences;
		_visibilityManager = require(HubVisibilityManager.class);

		new ForcefieldManager(this);

		_achievementManager = achievementManager;
		_missionManager = require(MissionManager.class);
		Location location = _worldData.getSpongeLocation("MISSIONS");
		location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, GetSpawn())));
		_missionManager.createNPC(location);

		new NotificationManager(getPlugin(), clientManager);
		new BotSpamManager(_plugin, clientManager, punish);

		require(PlayerDisguiseManager.class);

		new SalesAnnouncementManager(_plugin);

		require(CommunityManager.class);

		_hologramManager = hologramManager;

		//new EasterEggHunt(_plugin, _clientManager);

		require(MissionManager.class);

		//new TemporaryGemHuntersServerSender(_portal);

		require(TabListSorter.class);

		Managers.put(new HubScoreboard(_plugin, this), ScoreboardManager.class);

		require(TwoFactorAuth.class);
		_hubGameManager = require(HubGameManager.class);
		require(HubPlayerManager.class);
		require(AdminPunch.class);
		require(StaffBuild.class);
		require(SecretAreas.class);
//		require(TreasureHuntManager.class);

		new GameRejoinManager(this)
				.searchToRejoin();

		_parkourManager = require(ParkourManager.class);
		_jumpManager = new JumpManager(this);

		_hubPlugin = new HubPlugin();

		// Disable chunk generation
		nmsWorld.generator = new VoidGenerator();

		// Disable saving, enable chunk unloading
		//nmsWorld.spigotConfig.saveWorld = false;
		nmsWorld.spigotConfig.unloadChunks = true;

		// Unload chunks every 60 seconds
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				int count = 0;
				for (World world : _plugin.getServer().getWorlds())
				{
					WorldServer nmsWorld = ((CraftWorld) world).getHandle();
					if (nmsWorld.spigotConfig.unloadChunks != Boolean.TRUE)
						continue;

					boolean save = !nmsWorld.isSavingDisabled();
					for (Chunk chunk : world.getLoadedChunks())
					{
						ChunkUnloadEvent event = new ChunkUnloadEvent(chunk);
						_plugin.getServer().getPluginManager().callEvent(event);
						if (!event.isCancelled() && chunk.unload(save, true))
							count++;
					}
				}

				if (count > 9)
					System.out.println("Unloaded " + count + " chunks.");
			}
		}.runTaskTimer(_plugin, 20L, 20L * 60L);

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.GADGET_TOGGLE_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.GAMEMODE_COMMAND, true, true);
		if (UtilServer.isDevServer() || UtilServer.isTestServer())
		{
			PermissionGroup.ADMIN.setPermission(Perm.AUTO_OP, true, true);
			PermissionGroup.QAM.setPermission(Perm.AUTO_OP, false, true);
		}
		else
		{
			PermissionGroup.LT.setPermission(Perm.AUTO_OP, true, true);
		}
		PermissionGroup.ADMIN.setPermission(Perm.VANISH, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.SPAWN_PM, true, true);
		PermissionGroup.ULTRA.setPermission(Perm.JOIN_FULL, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.LIST_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new GadgetToggle(this));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void reflectMotd(ServerListPingEvent event)
	{
		if (_shuttingDown)
		{
			event.setMotd("Restarting soon");
		}
		else if (UtilServer.isTestServer())
		{
			event.setMotd(C.cGreen + "Private Mineplex Test Server");
		}
	}

	@EventHandler
	public void redirectStopCommand(PlayerCommandPreprocessEvent event)
	{
		if (event.getPlayer().isOp() && event.getMessage().equalsIgnoreCase("/stop"))
		{
			_shuttingDown = true;
			event.setCancelled(true);

			runSyncLater(() ->
			{
				_portal.sendAllPlayersToGenericServer(GenericServer.HUB, Intent.KICK);

				runSyncLater(Bukkit::shutdown, 40);
			}, 60);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void login(final PlayerLoginEvent event)
	{
		CoreClient client = _clientManager.Get(event.getPlayer().getUniqueId());

		// Reserved Slot Check
		if (Bukkit.getOnlinePlayers().size() - Bukkit.getServer().getMaxPlayers() >= 20)
		{
			if (!client.hasPermission(Perm.JOIN_FULL))
			{
				runSyncLater(() -> _portal.sendPlayerToGenericServer(event.getPlayer(), GenericServer.HUB, Intent.KICK), 0);

				event.allow();
			}
		}
		else
		{
			event.allow();
		}
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		player.setOp(_clientManager.Get(event.getPlayer()).hasPermission(Perm.AUTO_OP));
		_missionManager.incrementProgress(player, 1, MissionTrackerType.LOBBY_JOIN, null, null);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		event.setQuitMessage(null);
	}

	@EventHandler
	public void combatDeath(CombatDeathEvent event)
	{
		event.SetBroadcastType(DeathMessageType.None);
	}

	@EventHandler
	public void playerPrivateMessage(PrivateMessageEvent event)
	{
		//Dont Let PM Near Spawn!
		if (UtilMath.offset2dSquared(GetSpawn(), event.getSender().getLocation()) == 0 && _clientManager.Get(event.getSender()).hasPermission(Perm.SPAWN_PM))
		{
			UtilPlayer.message(event.getSender(), F.main("Chat", "You must leave spawn before you can Private Message!"));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void Incog(IncognitoHidePlayerEvent event)
	{
		if (!_clientManager.Get(event.getPlayer()).hasPermission(Perm.VANISH))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void playerChat(AsyncPlayerChatEvent event)
	{
		//Don't Let Chat Near Spawn!
		if (UtilMath.offset2dSquared(GetSpawn(), event.getPlayer().getLocation()) == 0 && !_clientManager.Get(event.getPlayer()).hasPermission(Perm.SPAWN_PM))
		{
			UtilPlayer.message(event.getPlayer(), F.main("Chat", "You must leave spawn before you can chat!"));
			event.setCancelled(true);
		}
	}

	@Override
	protected HubClient addPlayer(UUID uuid)
	{
		return new HubClient(Bukkit.getPlayer(uuid).getName());
	}

	public CoreClientManager GetClients()
	{
		return _clientManager;
	}

	public DonationManager GetDonation()
	{
		return _donationManager;
	}

	public DisguiseManager GetDisguise()
	{
		return _disguiseManager;
	}

	public HologramManager getHologram()
	{
		return _hologramManager;
	}

	public GadgetManager GetGadget()
	{
		return _gadgetManager;
	}

	public PreferencesManager getPreferences()
	{
		return _preferences;
	}

	public Location GetSpawn()
	{
		return _spawn.clone();
	}

	public PetManager getPetManager()
	{
		return _petManager;
	}

	public HubVisibilityManager GetVisibility()
	{
		return _visibilityManager;
	}

	public JumpManager getJumpManager()
	{
		return _jumpManager;
	}

	public ParkourManager getParkourManager()
	{
		return _parkourManager;
	}

	public HubGameManager getHubGameManager()
	{
		return _hubGameManager;
	}

	public MissionManager getMissionManager()
	{
		return _missionManager;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void ignoreVelocity(PlayerVelocityEvent event)
	{
		if (_clientManager.Get(event.getPlayer()).hasPermission(Preference.IGNORE_VELOCITY) && _preferences.get(event.getPlayer()).isActive(Preference.IGNORE_VELOCITY) && !getJumpManager().isDoubleJumping(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	public void toggleGadget()
	{
		GetGadget().toggleGadgetEnabled();

		for (Player player : UtilServer.getPlayersCollection())
		{
			player.sendMessage(C.cWhiteB + "Gadgets/Mounts are now " + F.elem(GetGadget().isGadgetEnabled() ? C.cGreenB + "Enabled" : C.cRedB + "Disabled"));
		}
	}

	@EventHandler
	public void clearEntityTargets(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Entity entity : _worldData.getWorld().getEntities())
		{
			if (entity instanceof EntityInsentient)
			{
				EntityInsentient entityMonster = (EntityInsentient) entity;

				if (entityMonster.getGoalTarget() != null && entityMonster.getGoalTarget() instanceof EntityPlayer)
				{
					if (((EntityPlayer) entityMonster.getGoalTarget()).playerConnection.isDisconnected())
					{
						entityMonster.setGoalTarget(null, TargetReason.FORGOT_TARGET, false);
					}
				}
			}
		}
	}

	public MineplexWorld getWorldData()
	{
		return _worldData;
	}

	public List<Location> getLookAt()
	{
		return _lookAt;
	}

	public boolean isNearSpawn(Location location)
	{
		return UtilMath.offsetSquared(GetSpawn(), location) < 100;
	}

	@EventHandler
	public void gadgetLocation(GadgetSelectLocationEvent event)
	{
		if (isNearSpawn(event.getLocation()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void gadgetBlock(GadgetBlockEvent event)
	{
		event.getBlocks().removeIf(block -> isNearSpawn(block.getLocation().add(0.5, 0.5, 0.5)));
	}

	@EventHandler
	public void gadgetEntity(GadgetCollideEntityEvent event)
	{
		if (isNearSpawn(event.getEntity().getLocation()))
		{
			event.setCancelled(true);
		}
	}
}
