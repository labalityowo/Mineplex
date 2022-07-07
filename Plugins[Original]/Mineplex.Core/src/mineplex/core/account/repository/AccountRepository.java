package mineplex.core.account.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.reflect.TypeToken;

import mineplex.cache.player.PlayerCache;
import mineplex.core.account.ILoginProcessor;
import mineplex.core.account.event.GroupAddEvent;
import mineplex.core.account.event.GroupRemoveEvent;
import mineplex.core.account.event.PrimaryGroupUpdateEvent;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.account.repository.token.LoginToken;
import mineplex.core.common.Pair;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UtilServer;
import mineplex.core.database.MinecraftRepository;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class AccountRepository extends MinecraftRepository
{
	private static String CREATE_ACCOUNT_TABLE = "CREATE TABLE IF NOT EXISTS accounts (id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(100), name VARCHAR(40), gems INT, lastLogin LONG, totalPlayTime LONG, PRIMARY KEY (id), UNIQUE INDEX uuidIndex (uuid), UNIQUE INDEX nameIndex (name));";
	private static String ACCOUNT_LOGIN_NEW = "INSERT INTO accounts (uuid, name, lastLogin) values(?, ?, now());";
	
	private static String CREATE_RANKS_TABLE = "CREATE TABLE IF NOT EXISTS accountRanks (id INT NOT NULL AUTO_INCREMENT, accountId INT NOT NULL, rankIdentifier VARCHAR(40), primaryGroup BOOL, PRIMARY KEY(id), INDEX accountIndex (accountId), INDEX rankIndex (rankIdentifier), UNIQUE INDEX additionalIndex (accountId, rankIdentifier, primaryGroup), FOREIGN KEY (accountId) REFERENCES accounts(id));";
	private static String UPDATE_PRIMARY_RANK = "UPDATE accountRanks SET rankIdentifier=? WHERE accountId=? AND primaryGroup=true;";
	private static String ADD_PRIMARY_RANK = "INSERT INTO accountRanks (accountId, rankIdentifier, primaryGroup) VALUES (?, 'NULL', true);";
	private static String ADD_ADDITIONAL_RANK = "INSERT INTO accountRanks (accountId, rankIdentifier, primaryGroup) VALUES (?, ?, false);";
	private static String REMOVE_ADDITIONAL_RANK = "DELETE FROM accountRanks WHERE accountId=? AND rankIdentifier=? AND primaryGroup=false;";
	private static String REMOVE_ADDITIONAL_RANKS = "DELETE FROM accountRanks WHERE accountId=? AND primaryGroup=false;";
	
	private static String SELECT_ACCOUNT_UUID_BY_NAME = "SELECT uuid FROM accounts WHERE name = ? ORDER BY lastLogin DESC;";
	private static String SELECT_ACCOUNT_UUID_BY_ID = "SELECT uuid FROM accounts WHERE id=?;";
	private static String SELECT_ACCOUNT_ID_BY_UUID = "SELECT id FROM accounts WHERE accounts.uuid = ? LIMIT 1";

	private static final String SELECT_LAST_LOGIN_BY_NAME = "SELECT lastLogin FROM accounts WHERE name = ? ORDER BY lastLogin DESC LIMIT 1;";

	public AccountRepository()
	{
		super(DBPool.getAccount());
	}

	public Pair<Integer, Pair<PermissionGroup, Set<PermissionGroup>>> login(final List<ILoginProcessor> loginProcessors, final UUID uuid, final String name) throws SQLException
	{
		// First we try to grab the account id from cache - this saves an extra trip to database
		int accountId = PlayerCache.getInstance().getAccountId(uuid);
		PermissionGroup primaryRank = null;
		Set<PermissionGroup> extraRanks = new HashSet<>();

		System.out.println("LOGIN... IDLE: " + ((BasicDataSource) DBPool.getAccount()).getNumIdle() + " ACTIVE: " + ((BasicDataSource) DBPool.getAccount()).getNumActive());
		try (Connection connection = getConnection(); Statement statement = connection.createStatement())
		{
			if (accountId <= 0)
			{
				// Player was not found in cache, we need to grab the account id from database
				statement.execute("SELECT id FROM accounts WHERE accounts.uuid = '" + uuid + "' LIMIT 1;");
				ResultSet resultSet = statement.getResultSet();

				if (resultSet.next())
				{
					accountId = resultSet.getInt(1);
				}
				else
				{
					// Player doesn't exist in our database, add them to the accounts table
					final List<Integer> tempList = new ArrayList<>(1);

					executeInsert(connection, ACCOUNT_LOGIN_NEW, rs ->
					{
						while (rs.next())
						{
							tempList.add(rs.getInt(1));
						}
					}, () -> {}, new ColumnVarChar("uuid", 100, uuid.toString()), new ColumnVarChar("name", 100, name));

					accountId = tempList.get(0);
				}
			}
			else
			{
				System.out.println(name + " Loaded Account ID From Cache [" + name + " - " + accountId + "]");
			}

			statement.execute("SELECT * FROM accountRanks WHERE accountId=" + accountId + ";");
			try (ResultSet rankSet = statement.getResultSet())
			{
				while (rankSet.next())
				{
					String identifier = rankSet.getString("rankIdentifier");
					PermissionGroup group = identifier.equals("NULL") ? null : PermissionGroup.valueOf(identifier);
					boolean primary = rankSet.getBoolean("primaryGroup");

					if (primary)
					{
						primaryRank = group;
					}
					else
					{
						if (group != null)
						{
							extraRanks.add(group);
						}
					}
				}
			}

			final int finalId = accountId;
			final String uuidString = uuid.toString();

			String loginString = "UPDATE accounts SET name='" + name + "', lastLogin=now() WHERE id = '" + accountId + "';";
			// We can use a parallel stream because they will be in the correct order when we collect
			loginString += loginProcessors.parallelStream().map(processor -> processor.getQuery(finalId, uuidString, name)).collect(Collectors.joining());

			statement.execute(loginString);

			System.out.println("EXECUTE COMPLETE - " + accountId);

			statement.getUpdateCount();
			statement.getMoreResults();

			for (ILoginProcessor loginProcessor : loginProcessors)
			{
				try
				{
					loginProcessor.processLoginResultSet(name, uuid, finalId, statement.getResultSet());
				}
				catch (Throwable t)
				{
					System.out.println("Error: ILoginProcessor raised an exception");
					t.printStackTrace(System.out);
				}
				finally
				{
					statement.getMoreResults();
				}
			}
		}


		return Pair.create(accountId, Pair.create(primaryRank, extraRanks));
	}

	public void getAccountId(UUID uuid, Callback<Integer> callback)
	{
		executeQuery(SELECT_ACCOUNT_ID_BY_UUID, resultSet ->
		{
			int accountId = -1;
			while (resultSet.next()) accountId = resultSet.getInt(1);
			callback.run(accountId);
		}, new ColumnVarChar("uuid", 100, uuid.toString()));
	}

	public String GetClient(String name, UUID uuid, String ipAddress)
	{
		LoginToken token = new LoginToken();
		token.Name = name;
		token.Uuid = uuid.toString();
		token.IpAddress = ipAddress;

		return handleSyncMSSQLCallStream("PlayerAccount/Login", token);
	}

	public String getClientByUUID(UUID uuid)
	{
		return handleSyncMSSQLCallStream("PlayerAccount/GetAccountByUUID", uuid.toString());
	}

	public UUID getClientUUID(String name)
	{
		final List<UUID> uuids = new LinkedList<>();

		executeQuery(SELECT_ACCOUNT_UUID_BY_NAME, resultSet ->
		{
			while (resultSet.next())
			{
				uuids.add(UUID.fromString(resultSet.getString(1)));
			}
		}, new ColumnVarChar("name", 100, name));

		if (uuids.size() > 0)
		{
			return uuids.get(0);
		}
		else
		{
			return null;
		}
	}
	
	public UUID getClientUUID(final int accountId)
	{
		StringBuilder uuidBuilder = new StringBuilder();
		executeQuery(SELECT_ACCOUNT_UUID_BY_ID, resultSet ->
		{
			if (resultSet.next())
			{
				uuidBuilder.append(resultSet.getString("uuid"));
			}
		}, new ColumnInt("id", accountId));
		
		if (uuidBuilder.length() == 0)
		{
			return null;
		}
		else
		{
			return UUID.fromString(uuidBuilder.toString());
		}
	}
	
	public void setPrimaryGroup(final int accountId, final PermissionGroup group, Runnable after)
	{
		UtilServer.runAsync(() ->
		{
			try (Connection c = getConnection())
			{
				try (Statement s = c.createStatement();
					ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM accountRanks WHERE accountId=" + accountId + " AND primaryGroup=true;");
					)
				{
					if (!rs.next() || rs.getInt(1) == 0)
					{
						s.execute(ADD_PRIMARY_RANK.replace("?", String.valueOf(accountId)));
					}
				}
				
				executeUpdate(c, UPDATE_PRIMARY_RANK, () -> {}, new ColumnVarChar("rankIdentifier", 255, group.name()), new ColumnInt("accountId", accountId));
				
				UtilServer.runSync(() ->
				{
					if (after != null)
					{
						after.run();
					}
					UtilServer.CallEvent(new PrimaryGroupUpdateEvent(accountId, group));
				});
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});
	}
	
	public void addAdditionalGroup(final int accountId, final PermissionGroup group, Consumer<Boolean> successCallback)
	{
		UtilServer.runAsync(() ->
		{
			executeInsert(ADD_ADDITIONAL_RANK, rs ->
			{
				if (rs.next())
				{
					UtilServer.runSync(() ->
					{
						if (successCallback != null)
						{
							successCallback.accept(Boolean.TRUE);
						}
						UtilServer.CallEvent(new GroupAddEvent(accountId, group));
					});
				}
				else
				{
					if (successCallback != null)
					{
						UtilServer.runSync(() -> successCallback.accept(Boolean.FALSE));
					}
				}
			}, () ->
			{
				if (successCallback != null)
				{
					UtilServer.runSync(() -> successCallback.accept(Boolean.FALSE));
				}
			}, new ColumnInt("accountId", accountId), new ColumnVarChar("rankIdentifier", 255, group.name()));
		});
	}
	
	public void removeAdditionalGroup(final int accountId, final PermissionGroup group, Consumer<Boolean> successCallback)
	{
		UtilServer.runAsync(() ->
		{
			AtomicBoolean callbackRun = new AtomicBoolean();
			int updated = executeUpdate(REMOVE_ADDITIONAL_RANK, () ->
			{
				callbackRun.set(true);
				if (successCallback != null)
				{
					UtilServer.runSync(() -> successCallback.accept(Boolean.FALSE));
				}
			}, new ColumnInt("accountId", accountId), new ColumnVarChar("rankIdentifier", 255, group.name()));
			
			if (!callbackRun.get())
			{
				if (updated > 0)
				{
					UtilServer.runSync(() ->
					{
						if (successCallback != null)
						{
							successCallback.accept(Boolean.TRUE);
						}
						UtilServer.CallEvent(new GroupRemoveEvent(accountId, group));
					});
				}
				else
				{
					UtilServer.runSync(() -> successCallback.accept(Boolean.FALSE));
				}
			}
		});
	}
	
	public void clearGroups(final int accountId, Consumer<Boolean> successCallback)
	{
		UtilServer.runAsync(() ->
		{
			AtomicBoolean success = new AtomicBoolean(true);
			Set<PermissionGroup> removed = new HashSet<>();
			
			try (Connection c = getConnection())
			{
				try (Statement s = c.createStatement())
				{
					int primaryFound = 0;
					try (ResultSet rs = s.executeQuery("SELECT * FROM accountRanks WHERE accountId=" + accountId + ";"))
					{
						while (rs.next())
						{
							if (!rs.getBoolean("primaryGroup"))
							{
								removed.add(PermissionGroup.valueOf(rs.getString("rankIdentifier")));
							}
							else
							{
								primaryFound++;
							}
						}
					}
					if (primaryFound < 1)
					{
						s.execute(ADD_PRIMARY_RANK.replace("?", String.valueOf(accountId)));
					}
				}

				executeUpdate(c, UPDATE_PRIMARY_RANK, () -> {}, new ColumnVarChar("rankIdentifier", 255, PermissionGroup.PLAYER.name()), new ColumnInt("accountId", accountId));
				executeUpdate(c, REMOVE_ADDITIONAL_RANKS, () -> success.set(false), new ColumnInt("accountId", accountId));
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				success.set(false);
			}
			
			if (successCallback != null || success.get())
			{
				UtilServer.runSync(() ->
				{
					if (successCallback != null)
					{
						successCallback.accept(success.get());
					}
					UtilServer.CallEvent(new PrimaryGroupUpdateEvent(accountId, PermissionGroup.PLAYER));
					for (PermissionGroup group : removed)
					{
						UtilServer.CallEvent(new GroupRemoveEvent(accountId, group));
					}
				});
			}
		});
	}
	
	public void fetchGroups(final int accountId, BiConsumer<PermissionGroup, Set<PermissionGroup>> resultCallback, Runnable onError)
	{
		fetchGroups(accountId, resultCallback, onError, true);
	}
	
	public void fetchGroups(final int accountId, BiConsumer<PermissionGroup, Set<PermissionGroup>> resultCallback, Runnable onError, boolean runAsync)
	{
		Runnable r = () ->
		{
			AtomicBoolean errored = new AtomicBoolean();
			AtomicReference<PermissionGroup> primary = new AtomicReference<>();
			Set<PermissionGroup> additional = new HashSet<>();
			
			executeQuery("SELECT * FROM accountRanks WHERE accountId=?;", rs ->
			{
				while (rs.next())
				{
					if (rs.getBoolean("primaryGroup"))
					{
						if (rs.getString("rankIdentifier").equals("NULL"))
						{
							primary.set(null);
						}
						else
						{
							primary.set(PermissionGroup.valueOf(rs.getString("rankIdentifier")));
						}
					}
					else
					{
						additional.add(PermissionGroup.valueOf(rs.getString("rankIdentifier")));
					}
				}
			}, () ->
			{
				if (onError != null)
				{
					errored.set(true);
					UtilServer.runSync(onError);
				}
			}, new ColumnInt("accountId", accountId));
			
			if (!errored.get())
			{
				if (resultCallback != null)
				{
					resultCallback.accept(primary.get(), additional);
				}
			}
		};
		
		if (runAsync)
		{
			UtilServer.runAsync(r);
		}
		else
		{
			r.run();
		}
	}

	public void matchPlayerName(final Callback<List<String>> callback, final String userName)
	{
		handleMSSQLCall("PlayerAccount/GetMatches", userName, new TypeToken<List<String>>(){}.getType(), callback::run);
	}

	public String getClientByName(String playerName)
	{
		return handleSyncMSSQLCallStream("PlayerAccount/GetAccount", playerName);
	}

	public void loadLastLogin(String name, Consumer<Long> lastLogin)
	{
		UtilServer.runAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection();
				 PreparedStatement statement = connection.prepareStatement(SELECT_LAST_LOGIN_BY_NAME))
			{
				statement.setString(1, name);

				try (ResultSet resultSet = statement.executeQuery())
				{
					if (resultSet.next())
					{
						lastLogin.accept(resultSet.getTimestamp("lastLogin").getTime());
					}
					else
					{
						lastLogin.accept(null);
					}
				}
			}
			catch (SQLException ex)
			{
				lastLogin.accept(null);
			}
		});
	}
}
