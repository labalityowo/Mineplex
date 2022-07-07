package mineplex.game.clans.clans.moderation.antialt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.Managers;
import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.punish.clans.ClansBanManager;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.moderation.antialt.AltRepository.ChainedDatabaseAction;
import mineplex.game.clans.clans.moderation.antialt.AltRepository.IpUnbanResult;
import mineplex.game.clans.clans.moderation.antialt.AltRepository.IpUnwhitelistResult;
import mineplex.game.clans.clans.moderation.antialt.IpAPIData.ImmutableIpAPIData;
import mineplex.serverdata.Utility;
import mineplex.serverdata.commands.ServerCommandManager;
import net.md_5.bungee.api.ChatColor;

public class AltManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		BAN_IP,
		UNBAN_IP,
		WHITELIST_IP,
		UNWHITELIST_IP,
		CHECK_IP,
		ACCOUNT_HISTORY,
		IP_HISTORY,
		CHECK_ALTS,
		BYPASS_ALT_CHECK,
	}
	
	private final ClansBanManager _punish = Managers.require(ClansBanManager.class);
	private final CoreClientManager _clientManager = Managers.require(CoreClientManager.class);
	protected final AltRepository _repo;
	private final int _serverId;
	private final Map<String, Pair<ImmutableIpAPIData, Long>> _vpnCache = new HashMap<>();
	
	public AltManager()
	{
		super("Alt Manager");
		
		_serverId = ClansManager.getInstance().getServerId();
		_repo = new AltRepository();
		
		addCommand(new CommandBase<AltManager>(this, Perm.BAN_IP, "banip")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				if (args.length < 1)
				{
					UtilPlayer.message(caller, F.main(getName(), "Usage: /banip <Ip>"));
					return;
				}
				final String ip = args[0];
				_repo.banIp(ip, caller.getName(), success ->
				{
					if (success)
					{
						UtilPlayer.message(caller, F.main(getName(), "Ip successfully banned!"));
						new IpBanNotification(ip).publish();
					}
					else
					{
						UtilPlayer.message(caller, F.main(getName(), "An error occurred while attempting to ban that ip!"));
					}
				});
			}
		});
		addCommand(new CommandBase<AltManager>(this, Perm.UNBAN_IP, "unbanip")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				if (args.length < 1)
				{
					UtilPlayer.message(caller, F.main(getName(), "Usage: /unbanip <Ip>"));
					return;
				}
				final String ip = args[0];
				_repo.unbanIp(ip, result ->
				{
					if (result == IpUnbanResult.UNBANNED)
					{
						UtilPlayer.message(caller, F.main(getName(), "Ip successfully unbanned!"));
					}
					else if (result == IpUnbanResult.NOT_BANNED)
					{
						UtilPlayer.message(caller, F.main(getName(), "That ip was not banned!"));
					}
					else
					{
						UtilPlayer.message(caller, F.main(getName(), "An error occurred while attempting to unban that ip!"));
					}
				});
			}
		});
		addCommand(new CommandBase<AltManager>(this, Perm.WHITELIST_IP, "whitelistip")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				if (args.length < 2)
				{
					UtilPlayer.message(caller, F.main(getName(), "Usage: /whitelistip <Ip> <Additional Accounts>"));
					return;
				}
				final String ip = args[0];
				final int additionalAccounts;
				try
				{
					additionalAccounts = Integer.parseInt(args[1]);
				}
				catch (NumberFormatException ex)
				{
					UtilPlayer.message(caller, F.main(getName(), "That is not a valid number of additional accounts!"));
					return;
				}
				_repo.whitelistIp(ip, caller.getName(), additionalAccounts, success ->
				{
					if (success)
					{
						UtilPlayer.message(caller, F.main(getName(), "Ip successfully whitelisted!"));
					}
					else
					{
						UtilPlayer.message(caller, F.main(getName(), "An error occurred while attempting to whitelist that ip!"));
					}
				});
			}
		});
		addCommand(new CommandBase<AltManager>(this, Perm.UNWHITELIST_IP, "unwhitelistip")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				if (args.length < 1)
				{
					UtilPlayer.message(caller, F.main(getName(), "Usage: /unwhitelistip <Ip>"));
					return;
				}
				final String ip = args[0];
				_repo.unwhitelistIp(ip, result ->
				{
					if (result == IpUnwhitelistResult.UNWHITELISTED)
					{
						UtilPlayer.message(caller, F.main(getName(), "Ip successfully unwhitelisted!"));
					}
					else if (result == IpUnwhitelistResult.NOT_WHITELISTED)
					{
						UtilPlayer.message(caller, F.main(getName(), "That ip was not whitelisted!"));
					}
					else
					{
						UtilPlayer.message(caller, F.main(getName(), "An error occurred while attempting to unwhitelist that ip!"));
					}
				});
			}
		});
		addCommand(new CommandBase<AltManager>(this, Perm.CHECK_IP, "checkip")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				if (args.length < 1)
				{
					UtilPlayer.message(caller, F.main(getName(), "Usage: /checkip <Ip>"));
					return;
				}
				final String ip = args[0];
				runAsync(() ->
				{
					Triple<Boolean, Boolean, Integer> status = checkIpStatus(ip);
					final boolean banned = status.getLeft();
					final boolean whitelisted = status.getMiddle();
					final int additionalAccounts = status.getRight();
					runSync(() ->
					{
						if (banned)
						{
							UtilPlayer.message(caller, F.main(getName(), "That ip is banned!"));
						}
						else
						{
							UtilPlayer.message(caller, F.main(getName(), "That ip is not banned!"));
						}
						if (whitelisted)
						{
							UtilPlayer.message(caller, F.main(getName(), "That ip is whitelisted with " + F.elem(String.valueOf(additionalAccounts)) + " additional accounts!"));
						}
						else
						{
							UtilPlayer.message(caller, F.main(getName(), "That ip is not whitelisted!"));
						}
					});
				});
			}
		});
		addCommand(new CommandBase<AltManager>(this, Perm.ACCOUNT_HISTORY, "accounthistory")
		{
			@SuppressWarnings("deprecation")
			@Override
			public void Execute(Player caller, String[] args)
			{
				if (args.length < 2)
				{
					UtilPlayer.message(caller, F.main(getName(), "Usage: /accounthistory <Ip> <Local>"));
					return;
				}
				final String ip = args[0];
				if (Boolean.valueOf(args[1]))
				{
					_repo.loadAccounts(ip, _serverId, accounts ->
					{
						UtilPlayer.message(caller, F.main(getName(), "Accounts logged in under that ip on this server:"));
						for (String name : accounts)
						{
							Player player = Bukkit.getPlayer(name);
							ChatColor cc = ChatColor.GRAY;
							if (player != null)
							{
								cc = ChatColor.GREEN;
							}
							UtilPlayer.message(caller, C.cBlue + "- " + cc + name);
						}
					});
				}
				else
				{
					_repo.loadAccounts(ip, accounts ->
					{
						UtilPlayer.message(caller, F.main(getName(), "Accounts logged in under that ip:"));
						for (String name : accounts)
						{
							OfflinePlayer op = Bukkit.getOfflinePlayer(name);
							ChatColor cc = ChatColor.GRAY;
							if (op.hasPlayedBefore())
							{
								cc = ChatColor.RED;
							}
							if (op.isOnline())
							{
								cc = ChatColor.GREEN;
							}
							UtilPlayer.message(caller, C.cBlue + "- " + cc + name);
						}
					});
				}
			}
		});
		addCommand(new CommandBase<AltManager>(this, Perm.IP_HISTORY, "iphistory")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				if (args.length < 2)
				{
					UtilPlayer.message(caller, F.main(getName(), "Usage: /iphistory <Name> <Local>"));
					return;
				}
				final String name = args[0];
				final boolean local = Boolean.valueOf(args[1]);
				_clientManager.loadClientByName(name, client ->
				{
					if (client == null)
					{
						UtilPlayer.message(caller, F.main(getName(), "That player was not found!"));
					}
					else
					{
						final int accountId = client.getAccountId();
						if (local)
						{
							_repo.loadIps(accountId, _serverId, ips ->
							{
								UtilPlayer.message(caller, F.main(getName(), "Ips used by " + F.elem(client.getName()) + " on this server:"));
								for (String ip : ips)
								{
									UtilPlayer.message(caller, C.cBlue + "- " + C.cGray + ip);
								}
							});
						}
						else
						{
							_repo.loadIps(accountId, ips ->
							{
								UtilPlayer.message(caller, F.main(getName(), "Ips used by " + F.elem(client.getName()) + ":"));
								for (String ip : ips)
								{
									UtilPlayer.message(caller, C.cBlue + "- " + C.cGray + ip);
								}
							});
						}
					}
				});
			}
		});
		addCommand(new CommandBase<AltManager>(this, Perm.CHECK_ALTS, "alts")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				if (args.length < 1)
				{
					UtilPlayer.message(caller, F.main(getName(), "Usage: /alts <Name>"));
					return;
				}
				final String name = args[0];
				_clientManager.loadClientByName(name, client ->
				{
					if (client == null)
					{
						UtilPlayer.message(caller, F.main(getName(), "That player was not found!"));
					}
					else
					{
						final int accountId = client.getAccountId();
						_repo.loadDuplicateAccounts(accountId, accounts ->
						{
							UtilPlayer.message(caller, F.main(getName(), "Accounts sharing an ip with " + F.elem(client.getName()) + ":"));
							for (String account : accounts)
							{
								if (account.equals(client.getName()))
								{
									continue;
								}
								UtilPlayer.message(caller, C.cBlue + "- " + C.cGray + account);
							}
						});
					}
				});
			}
		});
		ServerCommandManager.getInstance().registerCommandType(IpBanNotification.class, notification ->
		{
			runSync(() ->
			{
				Bukkit.getOnlinePlayers().forEach(player ->
				{
					if (player.getAddress().getAddress().toString().substring(1).equals(notification.getIp()))
					{
						player.kickPlayer(C.cRedB + "Your IP has been suspended from Mineplex Clans.\n" + C.cGold + "Visit http://www.mineplex.com/appeals to appeal this ban.");
					}
				});
			});
		});
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.BAN_IP, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.UNBAN_IP, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.WHITELIST_IP, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.UNWHITELIST_IP, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.CHECK_IP, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.ACCOUNT_HISTORY, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.IP_HISTORY, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.CHECK_ALTS, true, true);
		PermissionGroup.CMOD.setPermission(Perm.CHECK_ALTS, false, true);
	}
	
	private synchronized boolean checkForVPN(String ipAddress)
	{
		_vpnCache.entrySet().removeIf(entry -> UtilTime.elapsed(entry.getValue().getRight(), 120000)); //expire cache
		Pair<ImmutableIpAPIData, Long> cacheValue = _vpnCache.computeIfAbsent(ipAddress, ip ->
		{
			try
			{
				StringBuilder response = new StringBuilder();
				URLConnection connection = new URL("http://api.vpnblocker.net/v2/json/" + ipAddress + "/1YimOXUxTgh34kNRZYF31y5YEw8Phs").openConnection();
				connection.setConnectTimeout(10000);
				connection.setRequestProperty("User-Agent", "Mineplex Clans");
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())))
				{
					reader.lines().forEachOrdered(response::append);
				}
				if (response.length() == 0)
				{
					return Pair.create(new IpAPIData().makeImmutable(), System.currentTimeMillis());
				}
				IpAPIData data = Utility.deserialize(response.toString(), IpAPIData.class);
				if (data == null)
				{
					return Pair.create(new IpAPIData().makeImmutable(), System.currentTimeMillis());
				}
				return Pair.create(data.makeImmutable(), System.currentTimeMillis());
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return Pair.create(new IpAPIData().makeImmutable(), System.currentTimeMillis());
			}
		});
		if (cacheValue.getLeft().host_ip)
		{
			return true;
		}
		return false;
	}
	
	private Triple<Boolean, Boolean, Integer> checkIpStatus(String ipAddress)
	{
		ChainedDatabaseAction action = _repo.checkIpBanned(ipAddress).chain(_repo.checkIpWhitelisted(ipAddress));
		action.execute();
		return Triple.of(action.getResult(AltRepository.BAN_STATUS_KEY), action.getResult(AltRepository.WHITELIST_STATUS_KEY), action.getResult(AltRepository.WHITELIST_ADDITIONAL_ACCOUNTS_KEY));
	}
	
	private void storeLogin(int accountId, String ipAddress)
	{
		_repo.login(ipAddress, accountId, _serverId).executeAsync(null);
	}
	
	private void checkAltAndStore(int accountId, String ipAddress, Consumer<Boolean> callback)
	{
		ChainedDatabaseAction action = _repo.checkAltAccount(ipAddress, _serverId, accountId).chain(_repo.checkIpWhitelisted(ipAddress)).chain(_repo.login(ipAddress, accountId, _serverId));
		action.executeAsync(() ->
		{
			int alts = action.getResult(AltRepository.ALT_COUNT_KEY);
			boolean isWhitelisted = action.getResult(AltRepository.WHITELIST_STATUS_KEY);
			int additionalAccounts = action.getResult(AltRepository.WHITELIST_ADDITIONAL_ACCOUNTS_KEY);
			callback.accept(alts > additionalAccounts && !isWhitelisted);
		});
	}
	
//	@EventHandler(priority = EventPriority.HIGH)
	public void onLogin(AsyncPlayerPreLoginEvent event)
	{
		if (event.getLoginResult() != Result.ALLOWED)
		{
			return;
		}
		
		final String ipAddress = event.getAddress().toString().substring(1);
		Triple<Boolean, Boolean, Integer> status = checkIpStatus(ipAddress);
		if (!status.getMiddle() && checkForVPN(ipAddress))
		{
			event.disallow(Result.KICK_BANNED, C.cRedB + "VPN/Proxy usage is not permitted on Mineplex Clans");
			return;
		}
		if (status.getLeft())
		{
			event.disallow(Result.KICK_BANNED, C.cRedB + "Your IP has been suspended from Mineplex Clans.\n" + C.cGold + "Visit http://www.mineplex.com/appeals to appeal this ban.");
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		/*if (_clientManager.Get(player).hasPermission(Perm.BYPASS_ALT_CHECK))
		{
			return;
		}*/
		final int accountId = _clientManager.getAccountId(player);
		final String ipAddress = player.getAddress().getAddress().toString().substring(1);
		
		/*checkAltAndStore(accountId, ipAddress, alt ->
		{
			if (alt)
			{
				player.kickPlayer(C.cRed + "You have been disconnected for Unauthorized Alting!");
			}
		});*/
		storeLogin(accountId, ipAddress);
	}
}