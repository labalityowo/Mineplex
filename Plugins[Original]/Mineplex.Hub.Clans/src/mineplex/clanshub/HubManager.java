package mineplex.clanshub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.clanshub.commands.ForcefieldRadius;
import mineplex.clanshub.commands.GadgetToggle;
import mineplex.clanshub.commands.GameModeCommand;
import mineplex.clanshub.profile.gui.GUIProfile;
import mineplex.clanshub.salesannouncements.SalesAnnouncementManager;
import mineplex.core.Managers;
import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.benefit.BenefitManager;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.bonuses.BonusManager;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.botspam.BotSpamManager;
import mineplex.core.chat.Chat;
import mineplex.core.chat.ChatFormat;
import mineplex.core.chat.format.LevelFormatComponent;
import mineplex.core.chat.format.RankFormatComponent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.communities.CommunityManager;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.customdata.CustomDataManager;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseWither;
import mineplex.core.donation.DonationManager;
import mineplex.core.donation.Donor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.GadgetCollideEntityEvent;
import mineplex.core.gadget.gadgets.morph.MorphWither;
import mineplex.core.gadget.gadgets.mount.types.MountDragon;
import mineplex.core.gadget.gadgets.particle.king.CastleManager;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.hologram.HologramManager;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.incognito.events.IncognitoHidePlayerEvent;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.menu.MenuManager;
import mineplex.core.message.PrivateMessageEvent;
import mineplex.core.notifier.NotificationManager;
import mineplex.core.npc.NpcManager;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;
import mineplex.core.personalServer.PersonalServerManager;
import mineplex.core.pet.PetManager;
import mineplex.core.playerCount.PlayerCountManager;
import mineplex.core.poll.PollManager;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.punish.Punish;
import mineplex.core.scoreboard.MineplexScoreboard;
import mineplex.core.scoreboard.ScoreboardManager;
import mineplex.core.scoreboard.TabListSorter;
import mineplex.core.stats.StatsManager;
import mineplex.core.status.ServerStatusManager;
import mineplex.core.task.TaskManager;
import mineplex.core.thank.ThankManager;
import mineplex.core.treasure.TreasureManager;
import mineplex.core.twofactor.TwoFactorAuth;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.youtube.YoutubeManager;
import mineplex.minecraft.game.core.combat.DeathMessageType;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.condition.ConditionManager;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityPlayer;

/**
 * Main manager for clans hub
 */
public class HubManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		GADGET_TOGGLE_COMMAND,
		GAMEMODE_COMMAND,
		AUTO_OP,
		VANISH,
		SPAWN_PM,
		JOIN_FULL,
	}

	private BlockRestore _blockRestore;
	private CoreClientManager _clientManager;
	private ConditionManager _conditionManager;
	private DonationManager _donationManager;
	private DisguiseManager _disguiseManager;
	private PartyManager _partyManager;
	private ForcefieldManager _forcefieldManager;
	private PollManager _pollManager;
	private Portal _portal;
	private StatsManager _statsManager;
	private GadgetManager _gadgetManager;
	private HubVisibilityManager _visibilityManager;
	private PreferencesManager _preferences;
	private InventoryManager _inventoryManager;
	private AchievementManager _achievementManager;
	private TreasureManager _treasureManager;
	private PetManager _petManager;
	private PacketHandler _packetHandler;
	private PlayerCountManager _playerCountManager;
	private CustomDataManager _customDataManager;
	private Punish _punishManager;
	private IncognitoManager _incognito;
	private BonusManager _bonusManager;
	private final TwoFactorAuth _twofactor = Managers.require(TwoFactorAuth.class);

	private Location _spawn;

	private String _serverName = "";
	private boolean _shuttingDown;

	private Map<String, Long> _portalTime = new HashMap<>();

	private Map<String, List<String>> _creativeAdmin = new HashMap<>();

	public HubManager(JavaPlugin plugin, BlockRestore blockRestore, CoreClientManager clientManager, IncognitoManager incognito, DonationManager donationManager, InventoryManager inventoryManager, ConditionManager conditionManager, DisguiseManager disguiseManager, TaskManager taskManager, Portal portal, PartyManager partyManager, PreferencesManager preferences, PetManager petManager, PollManager pollManager, StatsManager statsManager, AchievementManager achievementManager, HologramManager hologramManager, NpcManager npcManager, PacketHandler packetHandler, Punish punish, ServerStatusManager serverStatusManager, CustomDataManager customDataManager, ThankManager thankManager, BoosterManager boosterManager, CastleManager castleManager)
	{
		super("Hub Manager", plugin);

		_incognito = incognito;
		
		_blockRestore = blockRestore;
		_clientManager = clientManager;
		_conditionManager = conditionManager;
		_donationManager = donationManager;
		_disguiseManager = disguiseManager;
		_pollManager = pollManager;

		_portal = portal;

		_spawn = new Location(UtilWorld.getWorld("world"), 0.5, 179, 0.5, -90f, 0f);
		((CraftWorld) _spawn.getWorld()).getHandle().spigotConfig.itemMerge = 0;

		new WorldManager(this);
		_inventoryManager = inventoryManager;
		new BenefitManager(plugin, clientManager, _inventoryManager);

		_gadgetManager = require(GadgetManager.class);
		achievementManager.setGadgetManager(_gadgetManager);

		YoutubeManager youtubeManager = new YoutubeManager(plugin, clientManager, donationManager);
		_bonusManager = new BonusManager(plugin, null, clientManager, donationManager, pollManager , npcManager, hologramManager, statsManager, _inventoryManager, petManager, youtubeManager, _gadgetManager, thankManager, "Carter");
		
		World world = _spawn.getWorld();
		_treasureManager = require(TreasureManager.class);
		_treasureManager.addTreasureLocation(new Location(world, -0.5, 179, -8.5));
		_treasureManager.addTreasureLocation(new Location(world, -0.5, 179, 9.5));
		
		new CosmeticManager(_plugin, clientManager, donationManager, _inventoryManager, _gadgetManager, petManager, _treasureManager, boosterManager, punish);

		new MenuManager(_plugin);
		require(Chat.class).setFormatComponents(
				new LevelFormatComponent(achievementManager),
				new RankFormatComponent(clientManager),
				player ->
				{
					TextComponent component = new TextComponent(player.getName());
					component.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
					return component;
				}
		);

		_petManager = petManager;
		_partyManager = partyManager;
		_preferences = preferences;
		_visibilityManager = new HubVisibilityManager(this);

		_forcefieldManager = new ForcefieldManager(this);
		addCommand(new ForcefieldRadius(_forcefieldManager));

		_statsManager = statsManager;
		_achievementManager = achievementManager;
		_packetHandler = packetHandler;

		new NotificationManager(getPlugin(), clientManager);
		new BotSpamManager(plugin, clientManager, punish);

		((CraftWorld)Bukkit.getWorlds().get(0)).getHandle().pvpMode = true;
		
		//new ValentinesGiftManager(plugin, clientManager, _bonusManager.getRewardManager(), inventoryManager, _gadgetManager, statsManager);

		_playerCountManager = new PlayerCountManager(plugin);

		_customDataManager = Managers.get(CustomDataManager.class);
		
		_punishManager = punish;
	
		_serverName = getPlugin().getConfig().getString("serverstatus.name");
		_serverName = _serverName.substring(0, Math.min(16,  _serverName.length()));
		
		new SalesAnnouncementManager(plugin);
		
		require(PersonalServerManager.class);
		require(CommunityManager.class);
		require(TabListSorter.class);
		ScoreboardManager scoreboardManager = new ScoreboardManager(plugin)
		{
			@Override
			public void setup(MineplexScoreboard scoreboard)
			{
				for (PermissionGroup group : PermissionGroup.values())
				{
					if (!group.canBePrimary())
					{
						continue;
					}
					if (!group.getDisplay(false, false, false, false).isEmpty())
					{
						scoreboard.getHandle().registerNewTeam(group.name()).setPrefix(group.getDisplay(true, true, true, false) + ChatColor.RESET + " ");
					}
					else
					{
						scoreboard.getHandle().registerNewTeam(group.name()).setPrefix("");
					}
				}

				scoreboard.register(HubScoreboardLine.START_EMPTY_SPACER)
						.register(HubScoreboardLine.SERVER_TITLE)
						.register(HubScoreboardLine.SERVER_NAME)
						.register(HubScoreboardLine.SERVER_EMPTY_SPACER)
						.register(HubScoreboardLine.PLAYER_TITLE)
						.register(HubScoreboardLine.PLAYER_COUNT)
						.register(HubScoreboardLine.PLAYER_EMPTY_SPACER)
						.register(HubScoreboardLine.RANK_TITLE)
						.register(HubScoreboardLine.RANK_NAME)
						.register(HubScoreboardLine.RANK_EMPTY_SPACER)
						.register(HubScoreboardLine.WEBSITE_TITLE)
						.register(HubScoreboardLine.WEBSITE_VALUE)
						.recalculate();

				scoreboard.get(HubScoreboardLine.SERVER_TITLE).write(C.cAqua + C.Bold + "Server");
				scoreboard.get(HubScoreboardLine.SERVER_NAME).write(_serverName);
				scoreboard.get(HubScoreboardLine.PLAYER_TITLE).write(C.cYellow + C.Bold + "Players");
				scoreboard.get(HubScoreboardLine.RANK_TITLE).write(C.cGold + C.Bold + "Rank");
				scoreboard.get(HubScoreboardLine.WEBSITE_TITLE).write(C.cRed + C.Bold + "Website");
				scoreboard.get(HubScoreboardLine.WEBSITE_VALUE).write("www.mineplex.com");
			}

			@Override
			public void draw(MineplexScoreboard scoreboard)
			{
				scoreboard.setSidebarName(C.cRed + C.Bold + C.Line + "Mineplex Clans");
				scoreboard.get(HubScoreboardLine.PLAYER_COUNT).write(_playerCountManager.getPlayerCount());

				String rankName = getRankName(GetClients().Get(scoreboard.getOwner()).getPrimaryGroup(), GetDonation().Get(scoreboard.getOwner()));

				PermissionGroup disguisedRank = GetClients().Get(scoreboard.getOwner()).getDisguisedPrimaryGroup();
				String disguisedAs = GetClients().Get(scoreboard.getOwner()).getDisguisedAs();
				if (disguisedRank != null && disguisedAs != null)
				{
					rankName = getRankName(disguisedRank, GetDonation().Get(GetClients().Get(scoreboard.getOwner()).getDisguisedAsUUID())) + " (" + rankName + ")";
				}

				scoreboard.get(HubScoreboardLine.RANK_NAME).write(rankName);
			}

			@Override
			public void handlePlayerJoin(String playerName)
			{
				Player player = Bukkit.getPlayerExact(playerName);

				PermissionGroup group = _clientManager.Get(player).getRealOrDisguisedPrimaryGroup();

				for (MineplexScoreboard scoreboard : getScoreboards().values())
				{
					scoreboard.getHandle().getTeam(group.name()).addEntry(playerName);
				}

				if (get(player) != null)
				{
					for (Player player1 : Bukkit.getOnlinePlayers())
					{
						group = _clientManager.Get(player1).getRealOrDisguisedPrimaryGroup();
						get(player).getHandle().getTeam(group.name()).addEntry(player1.getName());
					}
				}
			}

			@Override
			public void handlePlayerQuit(String playerName)
			{
				Player player = Bukkit.getPlayerExact(playerName);

				PermissionGroup group = _clientManager.Get(player).getRealOrDisguisedPrimaryGroup();

				for (MineplexScoreboard scoreboard : getScoreboards().values())
				{
					scoreboard.getHandle().getTeam(group.name()).removeEntry(playerName);
				}
			}

			private String getRankName(PermissionGroup group, Donor donor)
			{
				String display = group.getDisplay(false, false, false, false);
				if (display.isEmpty())
				{
					if (donor.ownsUnknownSalesPackage("SuperSmashMobs ULTRA") ||
						donor.ownsUnknownSalesPackage("Survival Games ULTRA") ||
						donor.ownsUnknownSalesPackage("Minigames ULTRA") ||
						donor.ownsUnknownSalesPackage("CastleSiege ULTRA") ||
						donor.ownsUnknownSalesPackage("Champions ULTRA"))
					{
							display = "Single Ultra";
					}
					else
					{
						display = "No Rank";
					}
				}

				return display;
			}
		};

		Managers.put(scoreboardManager, ScoreboardManager.class);

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.GADGET_TOGGLE_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.GAMEMODE_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.AUTO_OP, true, true);
		if (UtilServer.isTestServer())
		{
			PermissionGroup.QAM.setPermission(Perm.AUTO_OP, false, true);
		}
		PermissionGroup.CMOD.setPermission(Perm.VANISH, false, true);
		PermissionGroup.ADMIN.setPermission(Perm.VANISH, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.SPAWN_PM, true, true);
		PermissionGroup.ULTRA.setPermission(Perm.JOIN_FULL, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new GadgetToggle(this));
		addCommand(new GameModeCommand(this));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void reflectMotd(ServerListPingEvent event)
	{
		if (_shuttingDown)
		{
			event.setMotd("Restarting soon");
		}
	}

	@EventHandler
	public void redirectStopCommand(PlayerCommandPreprocessEvent event)
	{
		if (event.getPlayer().isOp() && event.getMessage().equalsIgnoreCase("/stop"))
		{
			_shuttingDown = true;

			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
			{
				public void run()
				{
					_portal.sendAllPlayersToGenericServer(GenericServer.HUB, Intent.KICK);

					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
					{
						public void run()
						{
							Bukkit.shutdown();
						}
					}, 40L);
				}
			}, 60L);

			event.setCancelled(true);
		}
	}

	@EventHandler
	public void preventEggSpawn(ItemSpawnEvent event)
	{
		if (event.getEntity() instanceof Egg)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void login(final PlayerLoginEvent event)
	{
        CoreClient client = _clientManager.Get(event.getPlayer());

        // Reserved Slot Check
		if (Bukkit.getOnlinePlayers().size() - Bukkit.getServer().getMaxPlayers() >= 20)
		{
			if (!client.hasPermission(Perm.JOIN_FULL))
			{
				Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () ->
				{
					_portal.sendPlayerToGenericServer(event.getPlayer(), GenericServer.CLANS_HUB, Intent.KICK);
				});

				event.allow();
			}
		}
		else
		{
			event.allow();
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void handleOP(PlayerJoinEvent event)
	{
		if (_clientManager.Get(event.getPlayer()).hasPermission(Perm.AUTO_OP))
		{
			event.getPlayer().setOp(true);
		} else
		{
			event.getPlayer().setOp(false);
		}
	}

	@EventHandler
	public void PlayerRespawn(PlayerRespawnEvent event)
	{
		event.setRespawnLocation(GetSpawn());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void PlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		//Public Message
		event.setJoinMessage(null);

		//Teleport
		player.teleport(GetSpawn());

		//Survival
		player.setGameMode(GameMode.SURVIVAL);

		//Clear Inv
		UtilInv.Clear(player);

		//Health
		player.setHealth(20);
	}

	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event)
	{
		event.setQuitMessage(null);

		event.getPlayer().leaveVehicle();
		event.getPlayer().eject();
		event.getPlayer().setOp(false);

		_portalTime.remove(event.getPlayer().getName());
	}

	@EventHandler
	public void playerPrivateMessage(PrivateMessageEvent event)
	{
		//Dont Let PM Near Spawn!
		if (UtilMath.offset2d(GetSpawn(), event.getSender().getLocation()) == 0 && !_clientManager.Get(event.getSender()).hasPermission(Perm.SPAWN_PM))
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

	@EventHandler
	public void Damage(EntityDamageEvent event)
	{
		if (event.getCause() == DamageCause.VOID)
		{
			if (event.getEntity() instanceof Player)
			{
				event.getEntity().eject();
				event.getEntity().leaveVehicle();
				event.getEntity().teleport(GetSpawn());
			}
			else
			{
				event.getEntity().remove();
			}
		}

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void handleDeath(PlayerDeathEvent event)
	{
		event.setKeepInventory(true);
		event.getDrops().clear();
		event.getEntity().setHealth(20);
		event.getEntity().teleport(GetSpawn());
	}

	@EventHandler
	public void combatDeath(CombatDeathEvent event)
	{
		event.SetBroadcastType(DeathMessageType.None);
	}

	@EventHandler
	public void FoodHealthUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			player.setFoodLevel(20);
			player.setExhaustion(0f);
			player.setSaturation(3f);
		}
	}

	@EventHandler
	public void InventoryCancel(InventoryClickEvent event)
	{
		if (event.getWhoClicked() instanceof Player && ((Player)event.getWhoClicked()).getGameMode() != GameMode.CREATIVE)
			event.setCancelled(true);
	}

	@EventHandler
	public void UpdateDisplay(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Bukkit.getOnlinePlayers().stream().filter(player -> _clientManager.Get(player).getPrimaryGroup() == PermissionGroup.PLAYER).forEach(player ->
		{
			UtilTextBottom.display(C.cGray + "Visit " + F.elem("http://www.mineplex.com/shop") + " for exclusive perks!", player);
		});
	}
	
	/**
	 * Gets the loaded BlockRestore manager
	 * @return The loaded BlockRestore manager
	 */
	public BlockRestore GetBlockRestore()
	{
		return _blockRestore;
	}
	
	/**
	 * Gets the loaded CoreClient manager
	 * @return The loaded CoreClient manager
	 */
	public CoreClientManager GetClients()
	{
		return _clientManager;
	}
	
	/**
	 * Gets the loaded Condition manager
	 * @return The loaded Condition manager
	 */
	public ConditionManager GetCondition()
	{
		return _conditionManager;
	}
	
	/**
	 * Gets the loaded Donation manager
	 * @return The loaded Donation manager
	 */
	public DonationManager GetDonation()
	{
		return _donationManager;
	}
	
	/**
	 * Gets the loaded Disguise manager
	 * @return The loaded Disguise manager
	 */
	public DisguiseManager GetDisguise()
	{
		return _disguiseManager;
	}
	
	/**
	 * Gets the loaded Gadget manager
	 * @return The loaded Gadget manager
	 */
	public GadgetManager GetGadget()
	{
		return _gadgetManager;
	}
	
	/**
	 * Gets the loaded Treasure manager
	 * @return The loaded Treasure manager
	 */
	public TreasureManager GetTreasure()
	{
		return _treasureManager;
	}

	/**
	 * Gets the loaded Preferences manager
	 * @return The loaded Preferences manager
	 */
	public PreferencesManager getPreferences()
	{
		return _preferences;
	}
	
	/**
	 * Gets the lobby's spawn
	 * @return The lobby's spawn
	 */
	public Location GetSpawn()
	{
		return _spawn.clone();
	}
	
	/**
	 * Gets the loaded Pet manager
	 * @return The loaded Pet manager
	 */
	public PetManager getPetManager()
	{
	    return _petManager;
	}
	
	/**
	 * Gets the loaded Bonus manager
	 * @return The loaded Bonus manager
	 */
	public BonusManager getBonusManager()
	{
		return _bonusManager;
	}
	
	/**
	 * Gets the loaded Stats manager
	 * @return The loaded Stats manager
	 */
	public StatsManager GetStats()
	{
		return _statsManager;
	}
	
	/**
	 * Gets the loaded HubVisibility manager
	 * @return The loaded HubVisibility manager
	 */
	public HubVisibilityManager GetVisibility()
	{
		return _visibilityManager;
	}
	
	/**
	 * Gets the loaded CustomData manager
	 * @return The loaded CustomData manager
	 */
	public CustomDataManager getCustomDataManager()
	{
		return _customDataManager;
	}
	
	/**
	 * Gets the loaded Punishment manager
	 * @return The loaded Punishment manager
	 */
	public Punish getPunishments()
	{
		return _punishManager;
	}
	
	/**
	 * Gets the loaded Incognito manager
	 * @return The loaded Incognito manager
	 */
	public IncognitoManager getIncognitoManager()
	{
		return _incognito;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void gadgetCollide(GadgetCollideEntityEvent event)
	{
		if (!event.isCancelled())
		{
			SetPortalDelay(event.getEntity());
		}
	}
	
	/**
	 * Updates a player's portal delay start to be now
	 * @param ent The player to set delay for
	 */
	public void SetPortalDelay(Entity ent)
	{
		if (ent instanceof Player)
		{
			_portalTime.put(((Player)ent).getName(), System.currentTimeMillis());
		}
	}
	
	/**
	 * Checks if a player can portal yet
	 * @param player The player to check
	 * @return Whether a player can portal yet
	 */
	public boolean CanPortal(Player player)
	{
		//Riding
		if (player.getVehicle() != null || player.getPassenger() != null)
			return false;

		//Portal Delay
		if (!_portalTime.containsKey(player.getName()))
			return true;

		return UtilTime.elapsed(_portalTime.get(player.getName()), 5000);
	}

	@EventHandler
	public void ignoreVelocity(PlayerVelocityEvent event)
	{
		if (_clientManager.Get(event.getPlayer()).hasPermission(Preference.IGNORE_VELOCITY) && _preferences.get(event.getPlayer()).isActive(Preference.IGNORE_VELOCITY))
		{
			event.setCancelled(true);
		}
	}
	
	/**
	 * Toggles all gadgets on or off via command
	 * @param caller The player who issued the command
	 */
	public void ToggleGadget(Player caller)
	{
		toggleGadget();
	}
	
	/**
	 * Toggles gadget access on or off in this lobby
	 */
	public void toggleGadget()
	{
		GetGadget().toggleGadgetEnabled();

		for (Player player : UtilServer.getPlayers())
			player.sendMessage(C.cWhite + C.Bold + "Gadgets/Mounts are now " + F.elem(GetGadget().isGadgetEnabled() ? C.cGreen + C.Bold + "Enabled" : C.cRed + C.Bold + "Disabled"));
	}
	
	/**
	 * Sets a player's gamemode via command
	 * @param caller The issuer of the command
	 * @param target The player whose gamemode should be set
	 */
	public void addGameMode(Player caller, Player target)
	{
		if (!_creativeAdmin.containsKey(caller.getName()))
			_creativeAdmin.put(caller.getName(), new ArrayList<String>());

		if (target.getGameMode() == GameMode.CREATIVE)
		{
			_creativeAdmin.get(caller.getName()).add(target.getName());
		}
		else
		{
			_creativeAdmin.get(caller.getName()).remove(target.getName());
		}
	}

	@EventHandler
	public void clearEntityTargets(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Entity entity : Bukkit.getWorlds().get(0).getEntities())
		{
			if (entity instanceof EntityInsentient)
			{
				EntityInsentient entityMonster = (EntityInsentient)entity;
				
				if (entityMonster.getGoalTarget() != null && entityMonster.getGoalTarget() instanceof EntityPlayer)
				{
					if (((EntityPlayer)entityMonster.getGoalTarget()).playerConnection.isDisconnected())
						entityMonster.setGoalTarget(null, TargetReason.FORGOT_TARGET, false);
				}
			}
		}
	}
	
	@EventHandler
	public void clearGameMode(PlayerQuitEvent event)
	{
		List<String> creative = _creativeAdmin.remove(event.getPlayer().getName());

		if (creative == null)
			return;

		for (String name : creative)
		{
			Player player = UtilPlayer.searchExact(name);
			if (player == null)
				continue;

			player.setGameMode(GameMode.SURVIVAL);

			UtilPlayer.message(player, F.main("Game Mode", event.getPlayer().getName() + " left the game. Creative Mode: " + F.tf(false)));
		}
	}
	
	/**
	 * Gets the loaded PacketHandler
	 * @return The loaded PacketHandler
	 */
	public PacketHandler getPacketHandler()
	{
	    return _packetHandler;
	}

	@EventHandler
	public void openProfile(PlayerInteractEvent event)
	{
		if(_twofactor.isAuthenticating(event.getPlayer()) || event.getItem() == null || event.getItem().getType() != Material.SKULL_ITEM)
			return;

		new GUIProfile(getPlugin(), event.getPlayer(), _preferences, _achievementManager).openInventory();;
	}
	
	@EventHandler
	public void trackPortalDelayPlayers(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Iterator<String> playerNameIterator = _portalTime.keySet().iterator(); playerNameIterator.hasNext();)
		{
			String playerName = playerNameIterator.next();
			
			if (UtilTime.elapsed(_portalTime.get(playerName), 5000))
			{
				playerNameIterator.remove();
			}
		}
	}
	
	@EventHandler
	public void showHeader(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}
		
		String text = C.cRed + "Welcome to Mineplex Clans";
		UtilTextTop.display(text, UtilServer.getPlayers());
		
		//Fix Entity Names
		for (Entity pet : _petManager.getPets())
		{
			if (pet instanceof LivingEntity)
			{
				DisguiseBase disguise = _disguiseManager.getActiveDisguise((LivingEntity) pet);

				if (disguise instanceof DisguiseWither)
				{
					((DisguiseWither) disguise).setName(text);
					disguise.resendMetadata();
				}
			}
		}
		
		for (Gadget mount : _gadgetManager.getGadgets(GadgetType.MOUNT))
		{
			if (mount instanceof MountDragon)
			{
				((MountDragon)mount).SetName(text);
			}
		}
		
		for (Gadget gadget : _gadgetManager.getGadgets(GadgetType.MORPH))
		{
			if (gadget instanceof MorphWither)
			{
				((MorphWither)gadget).setWitherData(text, 100);
			}
		}
	}
}
