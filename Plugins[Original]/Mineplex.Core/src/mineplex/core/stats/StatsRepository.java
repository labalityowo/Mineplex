package mineplex.core.stats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.mysql.jdbc.exceptions.jdbc4.MySQLDataException;

import mineplex.core.common.util.UtilServer;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class StatsRepository extends RepositoryBase
{
	private static final String SELECT_ACCOUNT_STATS = "SELECT stats.name, accountStatsAllTime.value FROM accountStatsAllTime INNER JOIN stats ON stats.id = accountStatsAllTime.statId WHERE accountStatsAllTime.accountId=?;";
	private static final String SELECT_USER_STATS = "SELECT stats.name, accountStatsAllTime.value FROM accountStatsAllTime INNER JOIN stats ON stats.id = accountStatsAllTime.statId WHERE accountStatsAllTime.accountId=(SELECT id FROM accounts WHERE name=? ORDER BY lastLogin DESC LIMIT 1);";
	
	private static final String INSERT_ACCOUNT_STAT = "INSERT INTO accountStatsAllTime (accountId, statId, value) VALUES (?, ?, ?);";
	private static final String UPDATE_ACCOUNT_STAT = "UPDATE accountStatsAllTime SET value=value + ? WHERE accountId=? AND statId=?;";
	
	private static final String RETRIEVE_STATS = "SELECT id, name FROM stats;";
	private static final String INSERT_STAT = "INSERT INTO stats (name) VALUES (?);";

	public StatsRepository()
	{
		super(DBPool.getAccount());
	}

	/**
	 * Retrieves all the remote registered stats
	 *
	 * @return The list of stats
	 */
	public List<Stat> retrieveStats()
	{
		List<Stat> stats = new ArrayList<>();

		executeQuery(RETRIEVE_STATS, resultSet ->
		{
			while (resultSet.next())
			{
				stats.add(new Stat(resultSet.getInt(1), resultSet.getString(2)));
			}
		});

		return stats;
	}

	/**
	 * Registers a stat with the remote server
	 *
	 * @param name The name of the stat
	 */
	public void registerNewStat(String name, Runnable onComplete)
	{
		try (Connection c = getConnection())
		{
			executeInsert(c, INSERT_STAT, rs -> onComplete.run(), () -> {}, new ColumnVarChar("name", 100, name));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadStats(int accountId, Consumer<Map<String, Long>> callback)
	{
		UtilServer.runAsync(() ->
		{
			Map<String, Long> loaded = new HashMap<>();
			executeQuery(SELECT_ACCOUNT_STATS, resultSet ->
			{
				while (resultSet.next())
				{
					String statName = resultSet.getString(1);
					long value;
					try
					{
						value = resultSet.getLong(2);
					}
					catch (MySQLDataException ex)
					{
						value = 0;
					}
					loaded.put(statName, value);
				}
			}, new ColumnInt("accountId", accountId));
			
			UtilServer.runSync(() -> callback.accept(loaded));
		});
	}

	/**
	 * Gets offline stats for the specified player name. This performs SQL on the current thread
	 */
	public PlayerStats loadOfflinePlayerStats(String playerName)
	{
		PlayerStats playerStats = null;

		Map<String, Long> loaded = new HashMap<>();
		executeQuery(SELECT_USER_STATS, resultSet ->
		{
			while (resultSet.next())
			{
				String statName = resultSet.getString(1);
				long value;
				try
				{
					value = resultSet.getLong(2);
				}
				catch (MySQLDataException ex)
				{
					value = 0;
				}
				loaded.put(statName, value);
			}
		}, new ColumnVarChar("name", playerName.length(), playerName));

		if (!loaded.isEmpty())
		{
			playerStats = new PlayerStats(false);
			loaded.forEach(playerStats::addStat);
		}

		return playerStats;
	}
	
	public void insertStats(Map<Integer, Map<Integer, Long>> stats)
	{
		UtilServer.runAsync(() ->
		{
			try (Connection c = getConnection();
				PreparedStatement updateStat = c.prepareStatement(UPDATE_ACCOUNT_STAT);
				PreparedStatement insertStat = c.prepareStatement(INSERT_ACCOUNT_STAT);
				)
			{
				for (Integer accountId : stats.keySet())
				{
					for (Integer statId : stats.get(accountId).keySet())
					{
						updateStat.setLong(1, stats.get(accountId).get(statId));
						updateStat.setInt(2, accountId);
						updateStat.setInt(3, statId);
						updateStat.addBatch();
					}
				}
				int[] rowsAffected = updateStat.executeBatch();
				int i = 0;
				for (Integer accountId : stats.keySet())
				{
					for (Integer statId : stats.get(accountId).keySet())
					{
						if (rowsAffected[i] < 1)
						{
							insertStat.setInt(1, accountId);
							insertStat.setInt(2, statId);
							insertStat.setLong(3, stats.get(accountId).get(statId));
							insertStat.addBatch();
						}
						i++;
					}
				}
				insertStat.executeBatch();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});
	}
	
	public void insertStats(int accountId, Map<Integer, Long> stats)
	{
		UtilServer.runAsync(() ->
		{
			try (Connection c = getConnection();
				PreparedStatement updateStat = c.prepareStatement(UPDATE_ACCOUNT_STAT);
				PreparedStatement insertStat = c.prepareStatement(INSERT_ACCOUNT_STAT);
				)
			{
				for (Integer statId : stats.keySet())
				{
					updateStat.setLong(1, stats.get(statId));
					updateStat.setInt(2, accountId);
					updateStat.setInt(3, statId);
					updateStat.addBatch();
				}
				int[] rowsAffected = updateStat.executeBatch();
				int i = 0;
				for (Integer statId : stats.keySet())
				{
					if (rowsAffected[i] < 1)
					{
						insertStat.setInt(1, accountId);
						insertStat.setInt(2, statId);
						insertStat.setLong(3, stats.get(statId));
						insertStat.addBatch();
					}
					i++;
				}
				insertStat.executeBatch();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});
	}
}