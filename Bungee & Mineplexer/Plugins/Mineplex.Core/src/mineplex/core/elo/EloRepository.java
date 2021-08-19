package mineplex.core.elo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;

import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnLong;

public class EloRepository extends RepositoryBase
{
	private static String INSERT_ELO = "INSERT INTO eloRating (accountId, gameType, elo) VALUES (?, ?, ?);";
	private static String UPDATE_ELO = "UPDATE eloRating SET elo = elo + ? WHERE accountId = ? AND gameType = ?;";
	private static String UPDATE_ELO_ONLY_IF_MATCH = "UPDATE eloRating SET elo = elo + ? WHERE accountId = ? AND gameType = ? AND elo = ?;";
	private static String GRAB_STRIKES = "SELECT strikes FROM rankedBans WHERE accountId = ?;";
	private static String GRAB_STRIKE_EXPIRY = "SELECT strikesExpire FROM rankedBans WHERE accountId = ?;";
	private static String GRAB_BAN_EXPIRY = "SELECT banEnd FROM rankedBans WHERE accountId = ?;";
	private static String UPDATE_BAN = "INSERT INTO rankedBans (accountId, strikes, strikesExpire, banEnd) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE strikes=VALUES(strikes), strikesExpire=VALUES(strikesExpire), banEnd=VALUES(banEnd);";
	private static String DELETE_STRIKES = "UPDATE rankedBans SET strikes = 1 WHERE accountId = ?;";
	private static String GET_NAME_FROM_ID = "SELECT `name` FROM `accounts` WHERE `id`=?;";

	public EloRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
	}

	public boolean saveElo(int accountId, int gameType, int oldElo, int elo) throws SQLException
	{
		List<Boolean> ret = Lists.newArrayList();
		UtilServer.runAsync(() ->
		{
			boolean updateSucceeded = false;

			// If we're increasing in elo we verify the server version matches the database version (prevent d/c and double wins with concurrent matches)
			// Otherwise we always take their elo down if they lose.
			if (elo > oldElo)
			{
				updateSucceeded = executeUpdate(UPDATE_ELO_ONLY_IF_MATCH, new ColumnInt("elo", elo - oldElo), new ColumnInt("accountId", accountId), new ColumnInt("gameType", gameType), new ColumnInt("elo", oldElo)) > 0;
			}
			else
			{
				updateSucceeded = executeUpdate(UPDATE_ELO, new ColumnInt("elo", elo - oldElo), new ColumnInt("accountId", accountId), new ColumnInt("gameType", gameType)) > 0;

				if (!updateSucceeded && executeUpdate(INSERT_ELO, new ColumnInt("accountId", accountId), new ColumnInt("gameType", gameType), new ColumnInt("elo", elo)) > 0)
				{
					updateSucceeded = true;
				}
			}

			ret.add(updateSucceeded);
		});

		if (ret.isEmpty())
		{
			ret.add(false);
		}

		return ret.get(0);
	}

	public EloClientData loadClientInformation(ResultSet resultSet) throws SQLException
	{
		EloClientData clientData = new EloClientData();

		while (resultSet.next())
		{
			clientData.Elos.put(resultSet.getInt(1), resultSet.getInt(2));
		}

		return clientData;
	}

	public void getStrikeExpiry(int accountId, Callback<Long> call)
	{
		UtilServer.runAsync(() -> executeQuery(GRAB_STRIKE_EXPIRY, resultSet ->
		{
			boolean called = false;
			while (resultSet.next())
			{
				called = true;
				call.run(resultSet.getLong(1));
			}

			if (!called)
			{
				call.run(0L);
			}
		}, new ColumnInt("accountId", accountId)));
	}

	public void getBanExpiryAsync(int accountId, Callback<Long> call)
	{
		UtilServer.runAsync(() -> executeQuery(GRAB_BAN_EXPIRY, resultSet ->
		{
			boolean called = false;
			while (resultSet.next())
			{
				called = true;
				call.run(resultSet.getLong(1));
			}

			if (!called)
			{
				call.run(0L);
			}
		}, new ColumnInt("accountId", accountId)));
	}

	public long getBanExpiry(int accountId)
	{
		List<Long> expiry = new ArrayList<Long>();
		executeQuery(GRAB_BAN_EXPIRY, resultSet ->
		{
			while (resultSet.next())
			{
				expiry.add(resultSet.getLong(1));
			}
		}, new ColumnInt("accountId", accountId));

		if (expiry.isEmpty())
		{
			expiry.add(System.currentTimeMillis() - 5000);
		}

		return expiry.get(0);
	}

	public void getStrikes(int accountId, Callback<Integer> call)
	{
		UtilServer.runAsync(() -> executeQuery(GRAB_STRIKES, resultSet ->
		{
			boolean called = false;
			while (resultSet.next())
			{
				called = true;
				call.run(resultSet.getInt(1));
			}

			if (!called)
			{
				call.run(0);
			}
		}, new ColumnInt("accountId", accountId)));
	}

	public void addRankedBan(int accountId)
	{
		getStrikes(accountId, strikes -> {
			int minutes = 1;
			switch (strikes)
			{
			case 0:
				minutes = 1;
				break;
			case 1:
				minutes = 5;
				break;
			case 2:
				minutes = 10;
				break;
			case 3:
				minutes = 20;
				break;
			case 4:
				minutes = 30;
				break;
			case 5:
				minutes = 60;
				break;
			case 6:
				minutes = 120;
				break;
			case 7:
				minutes = 180;
				break;
			case 8:
				minutes = 240;
				break;
			}
			long banEnd = System.currentTimeMillis() + UtilTime.convert(minutes, TimeUnit.MINUTES, TimeUnit.MILLISECONDS);
			long strikesExpire = System.currentTimeMillis() + UtilTime.convert(1, TimeUnit.DAYS, TimeUnit.MILLISECONDS);
			int newStrikes = Math.min(strikes + 1, 8);

			UtilServer.runAsync(() -> executeUpdate(UPDATE_BAN, new ColumnInt("accountId", accountId), new ColumnInt("strikes", newStrikes), new ColumnLong("strikesExpire", strikesExpire), new ColumnLong("banEnd", banEnd)));
		});
	}

	public void resetStrikes(int accountId)
	{
		UtilServer.runAsync(() -> executeUpdate(DELETE_STRIKES, new ColumnInt("accountId", accountId)));
	}

	public void getTopElo(int limit, int gameId, Callback<List<TopEloData>> callback)
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				Connection connection = getConnection();
				PreparedStatement statement = null;
				PreparedStatement nameStatement = null;
				LinkedList<TopEloData> dataList = Lists.newLinkedList();
				try
				{
					String GET_ELO = "SELECT `elo`,`accountId` FROM `eloRating` WHERE `gameType`=? ORDER BY `elo` DESC LIMIT " + limit + ";" ;
					statement = connection.prepareStatement(GET_ELO);
					statement.setInt(1, gameId);
					ResultSet resultSet = statement.executeQuery();
					while (resultSet.next())
					{
						int elo = resultSet.getInt(1);
						TopEloData data;
						nameStatement = connection.prepareStatement(GET_NAME_FROM_ID);
						nameStatement.setInt(1, resultSet.getInt(2));
						ResultSet nameSet = nameStatement.executeQuery();
						if (nameSet.next())
						{
							data = new TopEloData(nameSet.getString("name"), elo);
							dataList.add(data);
						}
					}
					callback.run(dataList);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
				finally
				{
					try
					{
						if (nameStatement != null)
						{
							statement.close();
						}
						if (statement != null)
						{
							statement.close();
						}
						if (connection != null)
						{
							connection.close();
						}
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
			}
		}.runTaskAsynchronously(UtilServer.getPlugin());
	}
}