package mineplex.core.antihack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.xml.bind.DatatypeConverter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.mineplex.anticheat.MineplexAnticheat;
import com.mineplex.anticheat.api.GameEndEvent;
import com.mineplex.anticheat.api.GameStartEvent;
import com.mineplex.anticheat.api.MineplexLink;
import com.mineplex.anticheat.api.PlayerViolationEvent;
import com.mineplex.anticheat.checks.Check;
import com.mineplex.anticheat.checks.CheckManager;
import com.mineplex.anticheat.checks.combat.FastBow;
import com.mineplex.anticheat.checks.combat.KillauraTypeA;
import com.mineplex.anticheat.checks.combat.KillauraTypeB;
import com.mineplex.anticheat.checks.combat.KillauraTypeC;
import com.mineplex.anticheat.checks.combat.KillauraTypeD;
import com.mineplex.anticheat.checks.combat.KillauraTypeE;
import com.mineplex.anticheat.checks.combat.KillauraTypeF;
import com.mineplex.anticheat.checks.move.Glide;
import com.mineplex.anticheat.checks.move.HeadRoll;
import com.mineplex.anticheat.checks.move.Speed;
import com.mineplex.anticheat.checks.move.Timer;
import com.mineplex.anticheat.checks.player.BadPackets;
import com.mineplex.anticheat.checks.player.Scaffold;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.antihack.actions.AntiHackAction;
import mineplex.core.antihack.actions.BanwaveAction;
import mineplex.core.antihack.actions.GEPBanAction;
import mineplex.core.antihack.actions.ImmediateBanAction;
import mineplex.core.antihack.actions.ImmediateKickAction;
import mineplex.core.antihack.actions.NoopAction;
import mineplex.core.antihack.animations.BanwaveAnimationSpin;
import mineplex.core.antihack.banwave.BanWaveInfo;
import mineplex.core.antihack.banwave.BanWaveManager;
import mineplex.core.antihack.commands.AnticheatOffCommand;
import mineplex.core.antihack.commands.AnticheatOnCommand;
import mineplex.core.antihack.commands.DetailedMessagesCommand;
import mineplex.core.antihack.commands.GetVlsCommand;
import mineplex.core.antihack.commands.TestBanCommand;
import mineplex.core.antihack.gep.GwenExtremePrejudice;
import mineplex.core.antihack.guardians.GuardianManager;
import mineplex.core.antihack.logging.AntihackLogger;
import mineplex.core.antihack.redisnotifications.GwenBanNotification;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.punish.Category;
import mineplex.core.punish.Punish;
import mineplex.core.punish.PunishClient;
import mineplex.core.punish.Punishment;
import mineplex.core.punish.PunishmentResponse;
import mineplex.serverdata.commands.ServerCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

@ReflectivelyCreateMiniPlugin
public class AntiHack extends MiniPlugin
{
	public static final Map<Class<? extends Check>, CheckThresholds> CHECKS = ImmutableMap.<Class<? extends Check>, CheckThresholds>builder()
			.put(KillauraTypeA.class, new CheckThresholds("Kill Aura", 25, 45, 60))
			.put(KillauraTypeB.class, new CheckThresholds("High CPS", 25, 40, 60))
			.put(KillauraTypeC.class, new CheckThresholds("Reach", 35, 80, 120))
			.put(KillauraTypeD.class, new CheckThresholds("Kill Aura", 500, 1000, 1500))
			.put(KillauraTypeE.class, new CheckThresholds("Kill Aura", 1000, 2000, 5000))
			.put(KillauraTypeF.class, new CheckThresholds("Kill Aura", 200, 300, 400))
			.put(BadPackets.class, new CheckThresholds("Regen", 500, 1000, 2000))
			.put(Glide.class, new CheckThresholds("Flying", 300, 600, 900))
			.put(Speed.class, new CheckThresholds("Speed", 1000, 2000, 3500))
			.put(HeadRoll.class, new CheckThresholds("Illegal Movement", 0, 0, 1000))
//			.put(Toggle.class, new CheckThresholds("AutoSneak", 100, 200, 300))
			.put(Timer.class, new CheckThresholds("Timer", 1000, 2000, 3000))
			.put(FastBow.class, new CheckThresholds("FastBow", 10, 20, 30))
//			.put(Phase.class, new CheckThresholds("Phase", 500, 1000, 1500))
			.put(Scaffold.class, new CheckThresholds("Scaffold", 80, 120, 170))
			.build();

	public static final Map<Class<? extends Check>, CheckThresholds> STRICT_CHECKS = ImmutableMap.<Class<? extends Check>, CheckThresholds>builder()
			.put(Glide.class, new CheckThresholds("Flying", 300, 600, 900))
			.put(Speed.class, new CheckThresholds("Speed", 600, 1200, 1800))
			.build();

	private static final CheckThresholds NOOP_THRESHOLD = new CheckThresholds("Unknown", Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

	public static final Map<Class<? extends Check>, AntiHackAction> ACTIONS = ImmutableMap.<Class<? extends Check>, AntiHackAction>builder()
			.put(KillauraTypeA.class, new ImmediateBanAction(150))
//			.put(KillauraTypeB.class, new BanwaveAction(80))
			.put(KillauraTypeD.class, new BanwaveAction(1500))
			.put(KillauraTypeF.class, new BanwaveAction(600))
			.put(Glide.class, new ImmediateKickAction(1200))
			.put(Speed.class, new ImmediateKickAction(7500))
			.put(HeadRoll.class, new ImmediateBanAction(2000))
//			.put(Toggle.class, new ImmediateKickAction(500))
			.put(Timer.class, new ImmediateKickAction(3500))
			.put(BadPackets.class, new GEPBanAction(300))
			.put(KillauraTypeC.class, new BanwaveAction(200))
			.put(Scaffold.class, new ImmediateKickAction(230))
			.build();

	public static final Map<Class<? extends Check>, AntiHackAction> STRICT_ACTIONS = ImmutableMap.<Class<? extends Check>, AntiHackAction>builder()
			.put(Glide.class, new ImmediateKickAction(1200))
			.put(Speed.class, new ImmediateKickAction(1400))
			.build();

	private static final AntiHackAction NOOP_ACTION = new NoopAction();

	private static final String NAME = "Chiss";
	private static final String USER_HAS_BEEN_BANNED = F.main("GWEN", "%s has been banned. I am always watching.");
	private static final String USER_HAS_BEEN_BANNED_BANWAVE = USER_HAS_BEEN_BANNED;

	public static final int ID_LENGTH = 5;

	public enum Perm implements Permission
	{
		SEE_GUARDIANS,
		SEE_ALERTS,
		SILENTLY_BANNED,
		ANTICHEAT_TOGGLE_COMMAND,
		DETAILED_MESSAGES_COMMAND,
		GET_VLS_COMMAND,
		TEST_BAN_COMMAND,
	}

	private final Cache<String, Integer> _cooldown = CacheBuilder.newBuilder()
			.concurrencyLevel(1)
			.expireAfterWrite(30, TimeUnit.SECONDS)
			.build();


	private final String _thisServer = UtilServer.getServerName();

	private final CoreClientManager _clientManager = require(CoreClientManager.class);
	private final AntihackLogger _logger = require(AntihackLogger.class);
	private final PreferencesManager _preferences = require(PreferencesManager.class);
	private final Punish _punish = require(Punish.class);

	private final Set<String> _detailedMessages = new HashSet<>();

	private Set<Player> _pendingBan = Collections.synchronizedSet(new HashSet<>());

	private Set<UUID> _banned = Collections.synchronizedSet(new HashSet<>());

	// These are the GWEN checks to ignore when handling PlayerViolationEvent
	private Set<Class<? extends Check>> _ignoredChecks = new HashSet<>();
	private boolean _strict;

	private BanWaveManager _banWaveManager;

	private AntiHack()
	{
		super("AntiHack");

		_detailedMessages.add("Spoobncoobr");

		require(GuardianManager.class);
		_banWaveManager = require(BanWaveManager.class);

		Bukkit.getServicesManager().register(MineplexLink.class, new MineplexLinkImpl(), _plugin, ServicePriority.Normal);

		ServerCommandManager.getInstance().registerCommandType(MajorViolationCommand.class, violation ->
		{
			BaseComponent[] minimal = getMinimalMessage(violation);
			BaseComponent[] detailed = getDetailedMessage(violation);
			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (_detailedMessages.contains(player.getName()))
				{
					player.spigot().sendMessage(detailed);
				}
				else if (_clientManager.Get(player).hasPermission(Perm.SEE_ALERTS) && (violation.getOriginatingServer().equals(_thisServer) || _preferences.get(player).isActive(Preference.GLOBAL_GWEN_REPORTS)))
				{
					player.spigot().sendMessage(minimal);
				}
			}
		});
		
		new GwenExtremePrejudice(UtilServer.getPlugin());
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.PLAYER.setPermission(Perm.SEE_GUARDIANS, true, true);
		PermissionGroup.BUILDER.setPermission(Perm.SEE_GUARDIANS, true, false);
		PermissionGroup.ADMIN.setPermission(Perm.SEE_GUARDIANS, true, true);

		PermissionGroup.CONTENT.setPermission(Perm.SILENTLY_BANNED, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.SILENTLY_BANNED, true, true);

		PermissionGroup.TRAINEE.setPermission(Perm.SEE_ALERTS, true, true);
		
		if (UtilServer.isTestServer())
		{
			PermissionGroup.DEV.setPermission(Perm.ANTICHEAT_TOGGLE_COMMAND, true, true);
			PermissionGroup.DEV.setPermission(Perm.TEST_BAN_COMMAND, true, true);
		}
		PermissionGroup.DEV.setPermission(Perm.DETAILED_MESSAGES_COMMAND, true, true);
		PermissionGroup.DEV.setPermission(Perm.GET_VLS_COMMAND, true, true);
		PermissionGroup.QA.setPermission(Perm.DETAILED_MESSAGES_COMMAND, true, true);
		PermissionGroup.QA.setPermission(Perm.GET_VLS_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		if (UtilServer.isTestServer())
		{
			addCommand(new AnticheatOnCommand(this));
			addCommand(new AnticheatOffCommand(this));
			addCommand(new TestBanCommand(this));
		}
		addCommand(new GetVlsCommand(this));
		addCommand(new DetailedMessagesCommand(this));
	}

	private void runBanAnimation(Player player, Runnable after)
	{
		new BanwaveAnimationSpin().run(player, after);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		UUID uuid = event.getPlayer().getUniqueId();
		runSync(() -> _banned.remove(uuid));
	}

	public void doBan(Player player, Class<? extends Check> cause, boolean gep, int hoursBanned)
	{
		runSync(() ->
		{
			if (_pendingBan.add(player) && !_banned.contains(player.getUniqueId()))
			{
				CoreClient coreClient = _clientManager.Get(player);

				String id = generateId();
				String finalMessage = "[GWEN Cheat Detection]\n\nBan Token:\n" + org.bukkit.ChatColor.GRAY + id;
				JsonObject custom = new JsonObject();
				custom.addProperty("ban-reason", CheckManager.getCheckSimpleName(cause));
				if (gep)
				{
					custom.addProperty("extreme-prejudice", true);
				}

				_logger.saveMetadata(player, id, () ->
				{
					Consumer<Consumer<PunishmentResponse>> doPunish = after ->
					{
						runAsync(() ->
								new GwenBanNotification(_thisServer, player.getName(), player.getUniqueId().toString(), coreClient.getPrimaryGroup().name().toLowerCase(), CheckManager.getCheckSimpleName(cause), id, gep).publish());

						_punish.AddPunishment(coreClient.getName(), Category.Hacking, finalMessage, AntiHack.NAME, 3, true, hoursBanned, true, after);
						if (UtilServer.getGroup().equals("Clans"))
						{
							_punish.getClansPunish().loadClient(coreClient.getUniqueId(), client ->
							{
								_punish.getClansPunish().ban(client, null, AntiHack.NAME, UtilTime.convert(90L, UtilTime.TimeUnit.DAYS, UtilTime.TimeUnit.MILLISECONDS), ChatColor.stripColor(finalMessage).replace("\n", ""), null, ban -> {});
							});
						}
					};

					if (_clientManager.Get(player).hasPermission(Perm.SILENTLY_BANNED))
					{
						doPunish.accept(result ->
						{
							_pendingBan.remove(player);
							_banned.add(player.getUniqueId());
						});
					} else
					{
						runBanAnimation(player, () ->
						{
							doPunish.accept(result ->
							{
								if (result == PunishmentResponse.Punished)
								{
									announceBan(player);
									_banned.add(player.getUniqueId());
									_banWaveManager.flagDone(coreClient);
								}
								_pendingBan.remove(player);
							});
						});
					}
				}, custom);
			}
		});
	}

	public void doBanWave(Player player, BanWaveInfo info)
	{
		runSync(() ->
		{
			CoreClient coreClient = _clientManager.Get(player);

			Consumer<Consumer<PunishmentResponse>> doPunish = after ->
			{
				final int hoursBanned = getHoursBanned(player);
				_punish.AddPunishment(coreClient.getName(), Category.Hacking, info.getMessage(), AntiHack.NAME, 3, true, hoursBanned, true, after);
				String[] serverSplit = info.getServer().split("-");
				if (serverSplit.length > 0 && serverSplit[0].equals("Clans"))
				{
					_punish.getClansPunish().loadClient(coreClient.getUniqueId(), client ->
					{
						_punish.getClansPunish().ban(client, null, AntiHack.NAME, UtilTime.convert(90L, UtilTime.TimeUnit.DAYS, UtilTime.TimeUnit.MILLISECONDS), ChatColor.stripColor(info.getMessage()).replace("\n", ""), null, ban -> {});
					});
				}
			};

			if (_clientManager.Get(player).hasPermission(Perm.SILENTLY_BANNED))
			{
				doPunish.accept(response -> {});
			}
			else
			{
				runBanAnimation(player, () ->
				{
					doPunish.accept(result ->
					{
						if (result == PunishmentResponse.Punished)
						{
							announceBanwave(player);
						}
					});
				});
			}
		});
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void on(PlayerMoveEvent event)
	{
		if (_pendingBan.contains(event.getPlayer()) && UtilMath.offset2d(event.getFrom().getBlock().getLocation(), event.getTo().getBlock().getLocation()) >= 1)
		{
			event.setCancelled(true);
			event.getPlayer().teleport(event.getFrom().getBlock().getLocation().add(0, 1, 0));
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void on(PlayerInteractEvent event)
	{
		if (_pendingBan.contains(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void on(PlayerToggleFlightEvent event)
	{
		if (_pendingBan.contains(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void on(PlayerCommandPreprocessEvent event)
	{
		if (_pendingBan.contains(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void on(EntityDamageEvent event)
	{
		if (_pendingBan.contains(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void on(EntityDamageByEntityEvent event)
	{
		if (_pendingBan.contains(event.getDamager()))
		{
			event.setCancelled(true);
		}
	}

	public int getPunishments(Player player)
	{
		PunishClient punishClient = require(Punish.class).GetClient(player.getName());

		if (punishClient == null)
		{
			return -1;
		}

		int totalPunishments = 0;

		if (punishClient.GetPunishments().containsKey(Category.Hacking))
		{
			for (Punishment punishment : punishClient.GetPunishments().get(Category.Hacking))
			{
				if (punishment.GetAdmin().equalsIgnoreCase(NAME) && punishment.GetReason().contains("[GWEN"))
				{
					totalPunishments++;
				}
			}
		}

		return totalPunishments;
	}

	public int getHoursBanned(Player player)
	{
		switch (getPunishments(player))
		{
			case 0:
				return 5 * 24;
			case 1:
				return 14 * 24;
			default:
				return 30 * 24;
		}
	}

	public void announceBan(Player player)
	{
		Bukkit.getServer().broadcastMessage(String.format(USER_HAS_BEEN_BANNED, player.getName()));
	}

	public void announceBanwave(Player player)
	{
		Bukkit.getServer().broadcastMessage(String.format(USER_HAS_BEEN_BANNED_BANWAVE, player.getName()));
	}

	public boolean toggleDetailedMessage(Player player)
	{
		if (_detailedMessages.add(player.getName()))
		{
			return true;
		}
		else
		{
			_detailedMessages.remove(player.getName());
			return false;
		}
	}

	@EventHandler
	public void onHack(PlayerViolationEvent event)
	{
		if (_ignoredChecks.contains(event.getCheckClass()))
		{
			return;
		}

		CheckThresholds thresholds = CHECKS.getOrDefault(event.getCheckClass(), NOOP_THRESHOLD);
		AntiHackAction action = ACTIONS.getOrDefault(event.getCheckClass(), NOOP_ACTION);
		if (_strict)
		{
			thresholds = STRICT_CHECKS.getOrDefault(event.getCheckClass(), thresholds);
			action = STRICT_ACTIONS.getOrDefault(event.getCheckClass(), action);
		}
		action.handle(event);

		CheckThresholds.Severity severity = thresholds.getSeverity(event.getViolations());

		if (severity == CheckThresholds.Severity.NONE)
		{
			return;
		}

		String key = event.getPlayer().getName() + "." + event.getHackType() + "." + severity.toString();

		Integer pastVl = this._cooldown.getIfPresent(key);
		if (pastVl == null)
		{
			MajorViolationCommand command = new MajorViolationCommand(_thisServer, event.getPlayer().getName(), CheckManager.getCheckSimpleName(event.getCheckClass()), event.getViolations(), event.getMessage(), isStrict());
			ServerCommandManager.getInstance().publishCommand(command);

			_cooldown.put(key, event.getViolations());
		}
	}

	/**
	 * Add a GWEN Anticheat class to the ignored checks.
	 * All violation events for these checks will be ignored
	 *
	 * @param check The class of the check to ignore
	 */
	public void addIgnoredCheck(Class<? extends Check> check)
	{
		_ignoredChecks.add(check);
		JavaPlugin.getPlugin(MineplexAnticheat.class).getCheckManager().disableCheck(check);
	}

	/**
	 * Reset the set of ignored checks. In the case that checks are being ignored for a specific game,
	 * this should be called when the game finishes.
	 */
	public void resetIgnoredChecks()
	{
		CheckManager cm = JavaPlugin.getPlugin(MineplexAnticheat.class).getCheckManager();
		if (!cm.getActiveChecks().isEmpty())
		{
			_ignoredChecks.forEach(cm::enableCheck);
		}
		_ignoredChecks.clear();
	}

	public void enableAnticheat()
	{
		UtilServer.CallEvent(new GameStartEvent());
		_ignoredChecks.forEach(JavaPlugin.getPlugin(MineplexAnticheat.class).getCheckManager()::disableCheck);
	}

	public void disableAnticheat()
	{
		UtilServer.CallEvent(new GameEndEvent());
	}

	private BaseComponent[] getDetailedMessage(MajorViolationCommand violation)
	{
		return new ComponentBuilder("")
				.append("A").color(ChatColor.AQUA).obfuscated(true)
				.append(" GWEN > ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.RED).bold(true)
				.append(violation.getPlayerName(), ComponentBuilder.FormatRetention.NONE).color(ChatColor.GOLD)
				.append(" failed " + violation.getHackType() + " VL" + violation.getViolations() + " in server ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.YELLOW)
				.append(violation.getOriginatingServer(), ComponentBuilder.FormatRetention.NONE).color(ChatColor.YELLOW)
				.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + violation.getOriginatingServer()))
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder("Teleport to " + violation.getOriginatingServer()).create()))
				.append(": " + violation.getMessage() + ".", ComponentBuilder.FormatRetention.NONE).color(ChatColor.YELLOW)
				.create();
	}

	private BaseComponent[] getMinimalMessage(MajorViolationCommand violation)
	{
		Class<? extends Check> checkType = CheckManager.getCheckBySimpleName(violation.getHackType());
		CheckThresholds thresholds = CHECKS.getOrDefault(checkType, NOOP_THRESHOLD);
		if (violation.isStrict())
		{
			thresholds = STRICT_CHECKS.getOrDefault(checkType, thresholds);
		}
		ComponentBuilder componentBuilder = new ComponentBuilder("")
				.append("A").color(ChatColor.AQUA).obfuscated(true)
				.append(" GWEN > ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.RED).bold(true)
				.append(violation.getPlayerName(), ComponentBuilder.FormatRetention.NONE).color(ChatColor.GOLD)
				.append(" suspected of ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.YELLOW);
		thresholds.format(componentBuilder, violation.getViolations());

		if (!violation.getOriginatingServer().equals(_thisServer))
		{
			componentBuilder.append(" in ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.YELLOW)
					.append(violation.getOriginatingServer()).color(ChatColor.AQUA)
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + violation.getOriginatingServer()))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							new ComponentBuilder("Teleport to " + violation.getOriginatingServer()).create()));
		}

		componentBuilder.append(".").color(ChatColor.YELLOW);

		return componentBuilder.create();
	}

	public static String generateId()
	{
		byte[] holder = new byte[ID_LENGTH];
		ThreadLocalRandom.current().nextBytes(holder);
		return DatatypeConverter.printHexBinary(holder);
	}

	public boolean isStrict()
	{
		return _strict;
	}

	public void setStrict(boolean strict)
	{
		_strict = strict;
	}
}