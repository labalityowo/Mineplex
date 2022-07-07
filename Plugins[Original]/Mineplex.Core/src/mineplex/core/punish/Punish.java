package mineplex.core.punish;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.event.ClientWebResponseEvent;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.Constants;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.punish.Command.HistoryCommand;
import mineplex.core.punish.Command.PunishCommand;
import mineplex.core.punish.Command.RulesCommand;
import mineplex.core.punish.Tokens.PunishClientToken;
import mineplex.core.punish.Tokens.PunishmentToken;
import mineplex.core.punish.clans.ClansBanManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.serverdata.commands.AddPunishCommand;
import mineplex.serverdata.commands.RemovePunishCommand;
import mineplex.serverdata.commands.ServerCommandManager;

public class Punish extends MiniPlugin
{
	public enum Perm implements Permission
	{
		ALERT_PUNISHMENT,
		PUNISHMENT_COMMAND,
		FULL_PUNISHMENT_ACCESS,
		RULES_COMMAND,
		REPORT_BAN_ACCESS,
		BYPASS_REMOVE_CONFIRMATION,
		PUNISHMENT_REAPPLY,
		PUNISHMENT_HISTORY_COMMAND
	}
	
	private Map<String, PunishClient> _punishClients;
	private PunishRepository _repository;
	private CoreClientManager _clientManager;
	private ClansBanManager _clansPunish;
	
	public Punish(JavaPlugin plugin, CoreClientManager clientManager)
	{
		this(plugin, clientManager, false);
	}
	
	public Punish(JavaPlugin plugin, CoreClientManager clientManager, boolean clansServer)
	{
		super("Punish", plugin);
		
        _punishClients = new HashMap<>();
        _clientManager = clientManager;
        _repository = new PunishRepository();
        _clansPunish = new ClansBanManager(plugin, clientManager, clansServer);
        
        ServerCommandManager.getInstance().registerCommandType("PunishCommand", mineplex.serverdata.commands.PunishCommand.class, new PunishmentHandler(this));
        
        generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.TRAINEE.setPermission(Perm.ALERT_PUNISHMENT, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.PUNISHMENT_COMMAND, true, true);
		PermissionGroup.MOD.setPermission(Perm.FULL_PUNISHMENT_ACCESS, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.RULES_COMMAND, true, true);

		PermissionGroup.RC.setPermission(Perm.REPORT_BAN_ACCESS, false, true);
		PermissionGroup.ADMIN.setPermission(Perm.REPORT_BAN_ACCESS, true, true);

		PermissionGroup.QA.setPermission(Perm.PUNISHMENT_REAPPLY, false, true);
		PermissionGroup.RC.setPermission(Perm.PUNISHMENT_REAPPLY, false, true);
		PermissionGroup.ADMIN.setPermission(Perm.PUNISHMENT_REAPPLY, true, true);

		PermissionGroup.MA.setPermission(Perm.BYPASS_REMOVE_CONFIRMATION, true, true);
		PermissionGroup.FN.setPermission(Perm.BYPASS_REMOVE_CONFIRMATION, false, true);
		PermissionGroup.ADMIN.setPermission(Perm.BYPASS_REMOVE_CONFIRMATION, true, true);

		PermissionGroup.PLAYER.setPermission(Perm.PUNISHMENT_HISTORY_COMMAND, true, true);
	}
	
	public ClansBanManager getClansPunish()
	{
		return _clansPunish;
	}
	
	public PunishRepository GetRepository()
	{
		return _repository;
	}
	
	@Override
	public void addCommands()
	{
		addCommand(new PunishCommand(this));
		addCommand(new HistoryCommand(this));
		addCommand(new RulesCommand(this));
	}
	
	@EventHandler
	public void OnClientWebResponse(ClientWebResponseEvent event)
	{
		PunishClientToken token = new Gson().fromJson(event.GetResponse(), PunishClientToken.class);
		LoadClient(token);
	}
	
	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event)
	{
		_punishClients.remove(event.getPlayer().getName().toLowerCase());
	}
	
    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerLogin(AsyncPlayerPreLoginEvent event)
    {
    	if (_punishClients.containsKey(event.getName().toLowerCase()))
		{
    		PunishClient client = GetClient(event.getName());
    		
    		if (client.IsBanned())
    		{
    			Punishment punishment = client.GetPunishment(PunishmentSentence.Ban);
    			String time = UtilTime.convertString(punishment.GetRemaining(), 0, TimeUnit.FIT);
    			
    			if (punishment.GetHours() == -1)
    			{
    				time = "Permanent";
    			}
    			
                String reason = C.cRed + C.Bold + "You are banned for " + time +
                		"\n" + C.cWhite + punishment.GetReason() +
                		"\n" + C.cDGreen + "Unfairly banned? Appeal at " + C.cGreen + "www.mineplex.com/appeals"
                		;
                
                event.disallow(Result.KICK_BANNED, reason);
    		}
		}
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void PunishChatEvent(AsyncPlayerChatEvent event)
    {
        PunishClient client = GetClient(event.getPlayer().getName());
        
        if (client != null && client.IsMuted())
        {
        	event.getPlayer().sendMessage(F.main(getName(), "Shh, you're muted because " + client.GetPunishment(PunishmentSentence.Mute).GetReason() + " by " + client.GetPunishment(PunishmentSentence.Mute).GetAdmin() + " for " + C.cGreen + UtilTime.convertString(client.GetPunishment(PunishmentSentence.Mute).GetRemaining(), 1, TimeUnit.FIT) + "."));
        	event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void PunishSignEvent(SignChangeEvent event)
    {
        PunishClient client = GetClient(event.getPlayer().getName());
        
        if (client != null && client.IsMuted())
        {
        	event.getPlayer().sendMessage(F.main(getName(), "Shh, you're muted because " + client.GetPunishment(PunishmentSentence.Mute).GetReason() + " by " + client.GetPunishment(PunishmentSentence.Mute).GetAdmin() + " for " + C.cGreen + UtilTime.convertString(client.GetPunishment(PunishmentSentence.Mute).GetRemaining(), 1, TimeUnit.FIT) + "."));
        	event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void banRefresh(UpdateEvent event)
    {
    	if (event.getType() != UpdateType.SEC)
    		return;
    	
    	for (Player check : UtilServer.getPlayers())
    	{
    		PunishClient client = GetClient(check.getName());
    		if (client != null && client.IsBanned())
    		{
    			Punishment punishment = client.GetPunishment(PunishmentSentence.Ban);
    			String time = UtilTime.convertString(punishment.GetRemaining(), 0, TimeUnit.FIT);
    			
    			if (punishment.GetHours() == -1)
    			{
    				time = "Permanent";
    			}
    			
                String reason = C.cRed + C.Bold + "You are banned for " + time +
                		"\n" + C.cWhite + punishment.GetReason() +
                		"\n" + C.cDGreen + "Unfairly banned? Appeal at " + C.cGreen + "www.mineplex.com/appeals"
                		;
                
    			check.kickPlayer(reason);
    		}
    	}
    }

	public void Help(Player caller)
	{
		UtilPlayer.message(caller, F.main(_moduleName, "Commands List:"));
		UtilPlayer.message(caller, F.help("/punish", "<player> <reason>", ChatColor.GOLD));
	}

	public void AddPunishment(final String playerName, final Category category, final String reason, final Player caller, final int severity, boolean ban, long duration)
	{
		AddPunishment(playerName, category, reason, caller == null ? null : caller.getName(), severity, ban, duration, false);
	}

	public void AddPunishment(String playerName, final Category category, final String reason, String callerName, final int severity, boolean ban, long duration, final boolean silent)
	{
		AddPunishment(playerName, category, reason, callerName, severity, ban, duration, silent, null);
	}

	public void AddPunishment(String playerName, final Category category, final String reason, String callerName, final int severity, boolean ban, long duration, final boolean silent, Consumer<PunishmentResponse> callback)
	{
		Player player = Bukkit.getPlayerExact(playerName);
		if (player != null)
		{
			CoreClient client = _clientManager.Get(player);
			playerName = client.getName();
		}

		String finalPlayerName = playerName;

		Player caller = callerName == null ? null : Bukkit.getPlayerExact(callerName);
		if (caller != null)
		{
			CoreClient client = _clientManager.Get(caller);
			callerName = client.getName();
		}

		String finalCallerName = callerName;

		if (!_punishClients.containsKey(playerName.toLowerCase()))
		{
			_punishClients.put(playerName.toLowerCase(), new PunishClient());
		}

		final PunishmentSentence sentence;

		if (ban)
		{
			sentence = PunishmentSentence.Ban;
		}
		else
		{
			if (category == Category.ReportAbuse)
			{
				sentence = PunishmentSentence.ReportBan;
			}
			else
			{
				sentence = PunishmentSentence.Mute;
			}
		}

		final long finalDuration = duration;

		_repository.Punish(new Callback<String>()
		{
			public void run(String result)
			{
				PunishmentResponse banResult = PunishmentResponse.valueOf(result);

				if (banResult == PunishmentResponse.AccountDoesNotExist)
				{
					if (caller != null)
						caller.sendMessage(F.main(getName(), "Account with name " + F.elem(finalPlayerName) + " does not exist."));
					else
						System.out.println(F.main(getName(), "Account with name " + F.elem(finalPlayerName) + " does not exist."));
				}
				else if (banResult == PunishmentResponse.InsufficientPrivileges)
				{
					if (caller != null)
						caller.sendMessage(F.main(getName(), "You have insufficient rights to punish " + F.elem(finalPlayerName) + "."));
					else
						System.out.println(F.main(getName(), "You have insufficient rights to punish " + F.elem(finalPlayerName) + "."));
				}
				else if (banResult == PunishmentResponse.Punished)
				{
					runAsync(() ->
					{
						new AddPunishCommand(finalPlayerName, severity, category.name(), sentence.name(), reason, duration, finalCallerName, caller != null ? caller.getUniqueId().toString() : null).publish();
					});
					final String durationString = getDurationString(finalDuration);
					
					if (sentence == PunishmentSentence.Ban)
					{
						if (caller == null)
							System.out.println(F.main(getName(), F.elem(caller == null ? "Mineplex Anti-Cheat" : caller.getName()) + " banned " + F.elem(finalPlayerName) + " because of " + F.elem(reason) + " for " + durationString + "."));
						
						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable()
						{
							public void run()
							{
								String kickReason = C.cRed + C.Bold + "You were banned for " + durationString + " by " + (caller == null ? "Mineplex Anti-Cheat" : caller.getName()) +
										"\n" + C.cWhite + reason +
										"\n" + C.cDGreen + "Unfairly banned? Appeal at " + C.cGreen + "www.mineplex.com/appeals";

								if (player != null)
									player.kickPlayer(kickReason);

								new mineplex.serverdata.commands.PunishCommand(finalPlayerName, true, false, kickReason).publish();
							}
						});

						if (!silent)
						{
							informOfPunish(finalPlayerName, F.main(getName(), caller == null ? "Mineplex Anti-Cheat" : caller.getName() + " banned " + finalPlayerName + " for " + durationString + "."));
						}
					}
					else if (sentence == PunishmentSentence.ReportBan)
					{
						if (caller == null)
							System.out.println(F.main(getName(), F.elem(caller == null ? "Mineplex Anti-Cheat" : caller.getName()) + " report banned " + F.elem(finalPlayerName) + " because of " + F.elem(reason) + " for " +
									durationString + "."));

						if (!silent)
						{
							informOfPunish(finalPlayerName, F.main(getName(), caller == null ? "Mineplex Anti-Cheat" : caller.getName() + " report banned " + finalPlayerName + " for " + durationString + "."));
						}

						//Inform
						if (player != null)
						{
							UtilPlayer.message(player, F.main("Punish", F.elem(C.cGray + C.Bold + "Reason: ") + reason));
							player.playSound(player.getLocation(), Sound.CAT_MEOW, 1f, 1f);
						}

						new mineplex.serverdata.commands.PunishCommand(finalPlayerName, false, finalDuration != 0, F.main("Punish", F.elem(C.cGray + C.Bold + "Report Ban Reason: ") + reason)).publish();

						_repository.LoadPunishClient(finalPlayerName, new Callback<PunishClientToken>()
						{
							public void run(PunishClientToken token)
							{
								LoadClient(token);
							}
						});
					}
					else
					{
						if (caller == null)
							System.out.println(F.main(getName(), F.elem(caller == null ? "Mineplex Anti-Cheat" : caller.getName()) + " muted " + F.elem(finalPlayerName) + " because of " + F.elem(reason) + " for " +
									durationString + "."));
						
						//Warning
						if (finalDuration == 0)
						{
							if (!silent)
							{
								informOfPunish(finalPlayerName, F.main(getName(), caller == null ? "Mineplex Anti-Cheat" : caller.getName() + " issued a friendly warning to " + finalPlayerName + "."));
							}
						}
						else
						{
							if (!silent)
							{
								informOfPunish(finalPlayerName, F.main(getName(), caller == null ? "Mineplex Anti-Cheat" : caller.getName() + " muted " + finalPlayerName + " for " + durationString + "."));
							}
						}
						
						//Inform
						if (player != null)
						{
							UtilPlayer.message(player, F.main("Punish", F.elem(C.cGray + C.Bold + "Reason: ") + reason));
							player.playSound(player.getLocation(), Sound.CAT_MEOW, 1f, 1f);
						}

						new mineplex.serverdata.commands.PunishCommand(finalPlayerName, false, finalDuration != 0, F.main("Punish", F.elem(C.cGray + C.Bold + (finalDuration != 0 ? "Mute" : "Warning") + " Reason: ") + reason)).publish();
						
						_repository.LoadPunishClient(finalPlayerName, new Callback<PunishClientToken>()
						{
							public void run(PunishClientToken token)
							{
								LoadClient(token);
							}
						});
					}
				}
				if (callback != null)
				{
					callback.accept(banResult);
				}
			}

			
		}, playerName, category.toString(), sentence, reason, duration, finalCallerName == null ? "Mineplex Anti-Cheat" : finalCallerName, severity);
	}

	public static String getDurationString(long duration)
	{
		return UtilTime.convertString(duration < 0 ? -1 : duration * 3600000, 1, TimeUnit.FIT);
	}
	
	private void informOfPunish(String punishee, String msg)
	{
		for (Player player : UtilServer.getPlayers())
		{
			if (_clientManager.Get(player).hasPermission(Perm.ALERT_PUNISHMENT) || player.getName().equals(punishee))
			{
				player.sendMessage(msg);
			}
		}
	}
	
	public void LoadClient(PunishClientToken token)
	{
		PunishClient client = new PunishClient();
		
		long timeDifference = System.currentTimeMillis() - token.Time;
		
		for (PunishmentToken punishment : token.Punishments)
		{
			Category category;
			PunishmentSentence punishmentType;

			// catch if category or punishment type no longer exists
			try
			{
				category = Category.valueOf(punishment.Category);
				punishmentType = PunishmentSentence.valueOf(punishment.Sentence);
			}
			catch (IllegalArgumentException e)
			{
				getPlugin().getLogger().log(Level.WARNING, "Skipping loading of punishment id " + punishment.PunishmentId + ", invalid category or punishment type.");
				continue;
			}

			client.AddPunishment(category, new Punishment(punishment.PunishmentId, punishmentType, category, punishment.Reason, punishment.Admin, punishment.Duration, punishment.Severity, punishment.Time + timeDifference, punishment.Active, punishment.Removed, punishment.RemoveAdmin, punishment.RemoveReason));
		}
		
		_punishClients.put(token.Name.toLowerCase(), client);
	}
	
	public PunishClient GetClient(String name)
	{
		synchronized (this)
		{
			return _punishClients.get(name.toLowerCase());
		}
	}

	public void RemovePunishment(Punishment punishment, String target, final Player admin, String reason, Callback<String> callback)
	{
		CoreClient client = _clientManager.Get(admin);
		_repository.RemovePunishment(string ->
		{
			runAsync(() ->
			{
				PunishmentResponse punishResponse = PunishmentResponse.valueOf(string);
				if (punishResponse == PunishmentResponse.PunishmentRemoved)
				{
					ServerCommandManager.getInstance().publishCommand(new RemovePunishCommand(Constants.GSON.fromJson(Constants.GSON.toJson(punishment), JsonObject.class), target, admin.getName(), admin.getUniqueId(), reason));
				}
			});

			callback.run(string);
		}, punishment.GetPunishmentId(), target, reason, client.getName());
	}

	public CoreClientManager GetClients()
	{
		return _clientManager;
	}
	
	public int factorial(int n)
	{
		if (n == 0)
			return 1;
		
		return n * (factorial(n-1)); 
	}
}
