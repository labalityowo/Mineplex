package mineplex.game.clans.clans.moderation.antialt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import mineplex.core.Managers;
import mineplex.core.common.util.EnclosedObject;
import mineplex.core.common.util.UtilServer;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class AltRepository extends RepositoryBase
{
	public static final String ALT_COUNT_KEY = "ALT_ACCOUNTS";
	public static final String WHITELIST_STATUS_KEY = "WHITELISTED_IP";
	public static final String WHITELIST_ADDITIONAL_ACCOUNTS_KEY = "WHITELISTED_ADDITIONAL_ACCOUNTS";
	public static final String BAN_STATUS_KEY = "BANNED_IP";
	
	private static final String CREATE_IP_BAN_TABLE = "CREATE TABLE IF NOT EXISTS clansIpBans (ipAddress VARCHAR(16), admin VARCHAR(40), PRIMARY KEY (ipAddress), INDEX adminIndex (admin));";
	private static final String FETCH_IP_BAN_INFO = "SELECT * FROM clansIpBans WHERE ipAddress=?;";
	private static final String BAN_IP = "INSERT INTO clansIpBans (ipAddress, admin) VALUES (?, ?) ON DUPLICATE KEY UPDATE admin=VALUES(admin);";
	private static final String UNBAN_IP = "DELETE FROM clansIpBans WHERE ipAddress=?;";
	
	private static final String CREATE_IP_WHITELIST_TABLE = "CREATE TABLE IF NOT EXISTS clansIpWhitelists (ipAddress VARCHAR(16), admin VARCHAR(40), additionalAccounts INT, PRIMARY KEY (ipAddress), INDEX adminIndex (admin));";
	private static final String FETCH_IP_WHITELIST_INFO = "SELECT * FROM clansIpWhitelists WHERE ipAddress=?;";
	private static final String WHITELIST_IP = "INSERT INTO clansIpWhitelists (ipAddress, admin, additionalAccounts) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE admin=VALUES(admin), additionalAccounts=VALUES(additionalAccounts);";
	private static final String UNWHITELIST_IP = "DELETE FROM clansIpWhitelists WHERE ipAddress=?;";
	
	private static final String CREATE_IP_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS clansIpHistory (ipAddress VARCHAR(16), accountId INT, serverId INT, PRIMARY KEY (ipAddress, accountId, serverId), INDEX ipIndex (ipAddress), INDEX accountIndex (accountId), INDEX accountServerIndex (accountId, serverId), INDEX ipServerIndex (ipAddress, serverId), FOREIGN KEY (serverId) REFERENCES clanServer(id), FOREIGN KEY (accountId) REFERENCES accounts(id));";
	private static final String FETCH_LOCAL_ACCOUNT_IPS = "SELECT ipAddress FROM clansIpHistory WHERE accountId=? AND serverId=?;";
	private static final String FETCH_ACCOUNT_IPS = "SELECT DISTINCT ipAddress FROM clansIpHistory WHERE accountId=?;";
	private static final String FETCH_IP_ACCOUNTS = "SELECT DISTINCT name FROM accounts WHERE id IN (SELECT accountId FROM clansIpHistory WHERE ipAddress=?);";
	private static final String FETCH_LOCAL_IP_ACCOUNTS = "SELECT DISTINCT name FROM accounts WHERE id IN (SELECT accountId FROM clansIpHistory WHERE ipAddress=? AND serverId=?);";
	private static final String FETCH_LOCAL_IP_ACCOUNT_IDS = "SELECT DISTINCT accountId FROM clansIpHistory WHERE ipAddress=? AND serverId=?;";
	private static final String FETCH_DUPLICATE_ACCOUNTS = "SELECT DISTINCT name FROM accounts WHERE id IN (SELECT DISTINCT accountId FROM clansIpHistory WHERE ipAddress IN (SELECT DISTINCT ipAddress FROM clansIpHistory WHERE accountId=?));";
	private static final String LOGIN_ACCOUNT = "INSERT INTO clansIpHistory (ipAddress, accountId, serverId) VALUES (?, ?, ?);";
	
	public AltRepository()
	{
		super(DBPool.getAccount());
	}
	
	public ChainedDatabaseAction checkIpBanned(String ipAddress)
	{
		return new ChainedDatabaseAction((connection, resultMap) ->
		{
			EnclosedObject<Boolean> recordExists = new EnclosedObject<>(false);
			
			executeQuery(connection, FETCH_IP_BAN_INFO, resultSet ->
			{
				recordExists.Set(resultSet.next());
			}, new ColumnVarChar("ipAddress", ipAddress.length(), ipAddress));
			
			resultMap.put(BAN_STATUS_KEY, recordExists.Get());
		});
	}
	
	public ChainedDatabaseAction checkIpWhitelisted(String ipAddress)
	{
		return new ChainedDatabaseAction((connection, resultMap) ->
		{
			EnclosedObject<Boolean> recordExists = new EnclosedObject<>(false);
			EnclosedObject<Integer> additionalAccounts = new EnclosedObject<>(0);
			
			executeQuery(connection, FETCH_IP_WHITELIST_INFO, resultSet ->
			{
				recordExists.Set(resultSet.next());
				if (recordExists.Get())
				{
					additionalAccounts.Set(resultSet.getInt("additionalAccounts"));
				}
			}, new ColumnVarChar("ipAddress", ipAddress.length(), ipAddress));
			
			resultMap.put(WHITELIST_STATUS_KEY, recordExists.Get());
			resultMap.put(WHITELIST_ADDITIONAL_ACCOUNTS_KEY, additionalAccounts.Get());
		});
	}
	
	public ChainedDatabaseAction login(String ipAddress, int accountId, int serverId)
	{
		return new ChainedDatabaseAction((connection, resultMap) ->
		{
			List<Integer> accounts = new ArrayList<>();
			executeQuery(connection, FETCH_LOCAL_IP_ACCOUNT_IDS, rs ->
			{
				while (rs.next())
				{
					accounts.add(rs.getInt(1));
				}
			}, new ColumnVarChar("ipAddress", ipAddress.length(), ipAddress), new ColumnInt("serverId", serverId));

			if (!accounts.contains(accountId))
			{
				executeInsert(connection, LOGIN_ACCOUNT, null, null, new ColumnVarChar(ipAddress, ipAddress.length(), ipAddress), new ColumnInt("accountId", accountId), new ColumnInt("serverId", serverId));
			}
		});
	}
	
	public ChainedDatabaseAction checkAltAccount(String ipAddress, int serverId, Integer... accountIgnore)
	{
		return new ChainedDatabaseAction((connection, resultMap) ->
		{
			List<Integer> accounts = new ArrayList<>();
			List<Integer> ignore = Arrays.asList(accountIgnore);
			executeQuery(connection, FETCH_LOCAL_IP_ACCOUNT_IDS, rs ->
			{
				while (rs.next())
				{
					if (!ignore.contains(rs.getInt(1)))
					{
						accounts.add(rs.getInt(1));
					}
				}
			}, new ColumnVarChar("ipAddress", ipAddress.length(), ipAddress), new ColumnInt("serverId", serverId));
			
			resultMap.put(ALT_COUNT_KEY, accounts.size());
		});
	}
	
	public void banIp(String ipAddress, String admin, Consumer<Boolean> callback)
	{
		UtilServer.runAsync(() ->
		{
			Consumer<Boolean> passThrough = success ->
			{
				if (callback != null)
				{
					UtilServer.runSync(() -> callback.accept(success));
				}
			};
			executeInsert(BAN_IP, rs -> passThrough.accept(true), () -> passThrough.accept(false), new ColumnVarChar("ipAddress", ipAddress.length(), ipAddress), new ColumnVarChar("admin", admin.length(), admin));
		});
	}
	
	public void unbanIp(String ipAddress, Consumer<IpUnbanResult> callback)
	{
		UtilServer.runAsync(() ->
		{
			Consumer<IpUnbanResult> passThrough = result ->
			{
				if (callback != null)
				{
					UtilServer.runSync(() -> callback.accept(result));
				}
			};
			EnclosedObject<Boolean> errored = new EnclosedObject<>(false);
			
			int rows = executeUpdate(UNBAN_IP, () ->
			{
				errored.Set(true);
				passThrough.accept(IpUnbanResult.ERROR);
			}, new ColumnVarChar("ipAddress", ipAddress.length(), ipAddress));
			
			if (!errored.Get())
			{
				if (rows > 0)
				{
					passThrough.accept(IpUnbanResult.UNBANNED);
				}
				else
				{
					passThrough.accept(IpUnbanResult.NOT_BANNED);
				}
			}
		});
	}
	
	public void whitelistIp(String ipAddress, String admin, int additionalAccounts, Consumer<Boolean> callback)
	{
		UtilServer.runAsync(() ->
		{
			Consumer<Boolean> passThrough = success ->
			{
				if (callback != null)
				{
					UtilServer.runSync(() -> callback.accept(success));
				}
			};
			executeInsert(WHITELIST_IP, rs -> passThrough.accept(true), () -> passThrough.accept(false), new ColumnVarChar("ipAddress", ipAddress.length(), ipAddress), new ColumnVarChar("admin", admin.length(), admin), new ColumnInt("additionalAccounts", additionalAccounts));
		});
	}
	
	public void unwhitelistIp(String ipAddress, Consumer<IpUnwhitelistResult> callback)
	{
		UtilServer.runAsync(() ->
		{
			Consumer<IpUnwhitelistResult> passThrough = result ->
			{
				if (callback != null)
				{
					UtilServer.runSync(() -> callback.accept(result));
				}
			};
			EnclosedObject<Boolean> errored = new EnclosedObject<>(false);
			
			int rows = executeUpdate(UNWHITELIST_IP, () ->
			{
				errored.Set(true);
				passThrough.accept(IpUnwhitelistResult.ERROR);
			}, new ColumnVarChar("ipAddress", ipAddress.length(), ipAddress));
			
			if (!errored.Get())
			{
				if (rows > 0)
				{
					passThrough.accept(IpUnwhitelistResult.UNWHITELISTED);
				}
				else
				{
					passThrough.accept(IpUnwhitelistResult.NOT_WHITELISTED);
				}
			}
		});
	}
	
	public void loadAccounts(String ipAddress, Consumer<List<String>> accountCallback)
	{
		UtilServer.runAsync(() ->
		{
			List<String> accounts = new ArrayList<>();
			executeQuery(FETCH_IP_ACCOUNTS, rs ->
			{
				while (rs.next())
				{
					accounts.add(rs.getString(1));
				}
			}, new ColumnVarChar("ipAddress", ipAddress.length(), ipAddress));
			
			UtilServer.runSync(() ->
			{
				accountCallback.accept(accounts);
			});
		});
	}
	
	public void loadAccountIds(String ipAddress, int serverId, Consumer<List<Integer>> accountCallback)
	{
		UtilServer.runAsync(() ->
		{
			List<Integer> accounts = new ArrayList<>();
			executeQuery(FETCH_LOCAL_IP_ACCOUNT_IDS, rs ->
			{
				while (rs.next())
				{
					accounts.add(rs.getInt(1));
				}
			}, new ColumnVarChar("ipAddress", ipAddress.length(), ipAddress), new ColumnInt("serverId", serverId));
			
			UtilServer.runSync(() ->
			{
				accountCallback.accept(accounts);
			});
		});
	}
	
	public void loadAccounts(String ipAddress, int serverId, Consumer<List<String>> accountCallback)
	{
		UtilServer.runAsync(() ->
		{
			List<String> accounts = new ArrayList<>();
			executeQuery(FETCH_LOCAL_IP_ACCOUNTS, rs ->
			{
				while (rs.next())
				{
					accounts.add(rs.getString(1));
				}
			}, new ColumnVarChar("ipAddress", ipAddress.length(), ipAddress), new ColumnInt("serverId", serverId));
			
			UtilServer.runSync(() ->
			{
				accountCallback.accept(accounts);
			});
		});
	}
	
	public void loadIps(int accountId, Consumer<List<String>> ipCallback)
	{
		UtilServer.runAsync(() ->
		{
			List<String> ips = new ArrayList<>();
			executeQuery(FETCH_ACCOUNT_IPS, rs ->
			{
				while (rs.next())
				{
					ips.add(rs.getString(1));
				}
			}, new ColumnInt("accountId", accountId));
			
			UtilServer.runSync(() ->
			{
				ipCallback.accept(ips);
			});
		});
	}
	
	public void loadIps(int accountId, int serverId, Consumer<List<String>> ipCallback)
	{
		UtilServer.runAsync(() ->
		{
			List<String> ips = new ArrayList<>();
			executeQuery(FETCH_LOCAL_ACCOUNT_IPS, rs ->
			{
				while (rs.next())
				{
					ips.add(rs.getString(1));
				}
			}, new ColumnInt("accountId", accountId), new ColumnInt("serverId", serverId));
			
			UtilServer.runSync(() ->
			{
				ipCallback.accept(ips);
			});
		});
	}
	
	public void loadDuplicateAccounts(int accountId, Consumer<List<String>> nameCallback)
	{
		UtilServer.runAsync(() ->
		{
			List<String> accounts = new ArrayList<>();
			executeQuery(FETCH_DUPLICATE_ACCOUNTS, rs ->
			{
				while (rs.next())
				{
					accounts.add(rs.getString(1));
				}
			}, new ColumnVarChar("accountId", accountId));
			
			UtilServer.runSync(() ->
			{
				nameCallback.accept(accounts);
			});
		});
	}
	
	protected static enum IpUnbanResult
	{
		UNBANNED,
		NOT_BANNED,
		ERROR;
	}
	
	protected static enum IpUnwhitelistResult
	{
		UNWHITELISTED,
		NOT_WHITELISTED,
		ERROR;
	}
	
	protected static class ChainedDatabaseAction
	{
		private final BiConsumer<Connection, Map<String, Object>> _action;
		private final Map<String, Object> _results = new HashMap<>();
		private final Object _resultLock = new Object();
		
		public ChainedDatabaseAction(BiConsumer<Connection, Map<String, Object>> action)
		{
			_action = action;
		}
		
		public ChainedDatabaseAction chain(ChainedDatabaseAction action)
		{
			return new ChainedDatabaseAction(_action.andThen(action._action));
		}
		
		@SuppressWarnings("unchecked")
		public <T> T getResult(String key)
		{
			synchronized (_resultLock)
			{
				Object result = _results.get(key);
				if (result == null)
				{
					return null;
				}
				try
				{
					return (T) result;
				}
				catch (ClassCastException ex)
				{
					ex.printStackTrace();
					return null;
				}
			}
		}
		
		public void execute()
		{
			synchronized (_resultLock)
			{
				try (Connection c = Managers.get(AltManager.class)._repo.getConnection())
				{
					_action.accept(c, _results);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		public void executeAsync(Runnable onComplete)
		{
			UtilServer.runAsync(() ->
			{
				synchronized (_resultLock)
				{
					try (Connection c = Managers.get(AltManager.class)._repo.getConnection())
					{
						_action.accept(c, _results);
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
				if (onComplete == null)
				{
					return;
				}
				UtilServer.runSync(onComplete);
			});
		}
	}
}