package mineplex.core.punish.clans;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.punish.clans.command.ClansBanCommand;
import mineplex.core.punish.clans.redis.ClansBanNotification;
import mineplex.serverdata.commands.ServerCommandManager;

public class ClansBanManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		ALERT_PUNISHMENT,
		PUNISHMENT_COMMAND,
	}

	private final CoreClientManager _clientManager;
	private final ClansBanRepository _repository;
	private final boolean _fullOperation;

	public ClansBanManager(JavaPlugin plugin, CoreClientManager clientManager, boolean fullOperation)
	{
		super("Clans Blacklist", plugin);
		
		_clientManager = clientManager;
		
		_repository = new ClansBanRepository(plugin);
		
		_fullOperation = fullOperation;
		
		if (_fullOperation)
		{
			ServerCommandManager.getInstance().registerCommandType(ClansBanNotification.class, notification ->
			{
				runSync(() ->
				{
					if (Bukkit.getPlayer(notification.getTarget()) != null)
					{
						Bukkit.getPlayer(notification.getTarget()).kickPlayer(C.cRedB + "You have been banned from Clans " + notification.getBanTimeFormatted() + ".");
					}
				});
			});
		}
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.ALERT_PUNISHMENT, true, true);
		PermissionGroup.CMOD.setPermission(Perm.ALERT_PUNISHMENT, false, true);
		PermissionGroup.CMA.setPermission(Perm.ALERT_PUNISHMENT, false, true);
		PermissionGroup.QA.setPermission(Perm.ALERT_PUNISHMENT, true, true);

		PermissionGroup.ADMIN.setPermission(Perm.PUNISHMENT_COMMAND, true, true);
		PermissionGroup.CMOD.setPermission(Perm.PUNISHMENT_COMMAND, false, true);
		PermissionGroup.CMA.setPermission(Perm.PUNISHMENT_COMMAND, false, true);
		PermissionGroup.QA.setPermission(Perm.PUNISHMENT_COMMAND, true, true);
	}
	
	@Override
	public void addCommands()
	{
		addCommand(new ClansBanCommand(this));
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}

	public ClansBanRepository getRepository()
	{
		return _repository;
	}
	
	public void loadClient(String name, Consumer<Optional<ClansBanClient>> callback)
	{
		_repository.loadClient(name).thenAccept(client -> runSync(() -> callback.accept(client)));
	}
	
	public void loadClient(UUID uuid, Consumer<ClansBanClient> callback)
	{
		_repository.loadClient(uuid).thenAccept(client -> runSync(() -> callback.accept(client)));
	}
	
	public void ban(ClansBanClient target, String targetName, String admin, long duration, String reason, Player caller, Consumer<Optional<ClansBan>> callback)
	{
		_repository.ban(target._uuid, admin, duration, reason).thenAccept(ban -> runSync(() ->
		{
			if (ban.isPresent())
			{
				target._bans.add(ban.get());
				target.sortBans();
				String banTimeFormatted = target.getBanTimeFormatted();
				
				if (targetName != null)
				{
					for (Player notify : Bukkit.getOnlinePlayers())
					{
						if (_clientManager.Get(notify).hasPermission(Perm.ALERT_PUNISHMENT))
						{
							UtilPlayer.message(notify, F.main(getName(), F.elem(targetName) + " is now banned " + banTimeFormatted + "."));
						}
					}
				}
				if (_fullOperation && Bukkit.getPlayer(target._uuid) != null)
				{
					Bukkit.getPlayer(target._uuid).kickPlayer(C.cRedB + "You have been banned from Clans " + banTimeFormatted + ".");
				}
				new ClansBanNotification(target._uuid, banTimeFormatted).publish();
			}
			else
			{
				if (caller != null && targetName != null)
				{
					UtilPlayer.message(caller, F.main(getName(), C.cRed + "An issue occurred when trying to ban " + F.elem(targetName)));
				}
			}
			if (callback != null)
			{
				callback.accept(ban);
			}
		}));
	}

	public void unban(ClansBanClient target, ClansBan ban, String admin, String reason, Runnable callback)
	{
		if (!target._uuid.equals(ban.getUUID()))
		{
			return;
		}

		_repository.removeBan(ban, admin, reason, () ->
		{
			ban.remove(admin, reason);
			target.sortBans();
			
			if (callback != null)
			{
				callback.run();
			}
		});
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(AsyncPlayerPreLoginEvent event)
	{
		if (!_fullOperation)
		{
			return;
		}
		try
		{
			ClansBanClient client = _repository.loadClient(event.getUniqueId()).get();

			if (client.isBanned())
			{
				String time = UtilTime.convertString(client.getLongestBan().getTimeLeft(), 0, TimeUnit.FIT);

				if (client.getLongestBan().isPermanent())
				{
					time = "Permanent";
				}

				String reason = C.cRedB + "You are banned from Clans for " + time +
						"\n" + C.cWhite + client.getLongestBan().getReason();

				event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, reason);
			}
		}
		catch (Exception ignored) {}
	}
}