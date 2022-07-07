package mineplex.core.stats.game;

import mineplex.core.common.util.UtilTime;
import mineplex.core.game.GameDisplay;
import mineplex.core.stats.StatsManager;
import mineplex.serverdata.Region;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnTimestamp;
import mineplex.serverdata.database.column.ColumnVarChar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class GameStatisticsRepository extends RepositoryBase
{

	private static final String INSERT_MAP = "INSERT INTO gameMaps (gameType,mapName) VALUES (?,?)";
	private static final String GET_MAP_BY_ID = "SELECT mapId FROM gameMaps WHERE mapName=?";
	private static final String SAVE_GAME = "INSERT INTO gameStatistics (region,gameType,map,startTime,endTime) VALUES (?,?,?,?,?)";
	private static final String SAVE_STAT = "INSERT INTO gamePlayerStatistics VALUES (?,?,?,?);";
	private static final String GET_BY_ID = "SELECT * FROM gameStatistics WHERE gameId=?";
	private static final String SQL_REGEX = "\\?";
	private static final long MAX_RESPONSE_TIME = TimeUnit.SECONDS.toMillis(5);

	private final StatsManager _statsManager;

	public GameStatisticsRepository(StatsManager statsManager)
	{
		super(DBPool.getAccount());

		_statsManager = statsManager;
	}

	public void getMapId(Consumer<Integer> callback, int gameId, String mapName)
	{
		executeQuery(GET_MAP_BY_ID, resultSet ->
		{
			if (resultSet.next())
			{
				callback.accept(resultSet.getInt(1));
			}
			else
			{
				executeInsert(INSERT_MAP, resultSetInsert ->
				{
					if (resultSetInsert.next())
					{
						callback.accept(resultSetInsert.getInt(1));
					}
				},
						new ColumnInt("gameType", gameId),
						new ColumnVarChar("mapName", 32, mapName)
				);
			}
		}, new ColumnVarChar("mapName", 32, mapName));
	}

	public void saveGame(GameStats gameStats)
	{
		executeInsert(SAVE_GAME, resultSet ->
				{
					if (resultSet.next())
					{
						int gameId = resultSet.getInt(1);

						saveGameStats(gameId, gameStats);
					}
				},

				new ColumnVarChar("region", 2, gameStats.getRegion().name()),
				new ColumnInt("gameType", gameStats.getGameType().getGameId()),
				new ColumnInt("map", gameStats.getMapId()),
				new ColumnTimestamp("startTime", new Timestamp(gameStats.getStartTime())),
				new ColumnTimestamp("endTime", new Timestamp(gameStats.getEndTime()))

		);
	}

	private void saveGameStats(int gameId, GameStats gameStats)
	{
		String gameIdString = String.valueOf(gameId);
		Map<Integer, Map<String, Long>> stats = gameStats.getStats();

		StringBuilder builder = new StringBuilder(1000);
		long start = System.currentTimeMillis();
		AtomicInteger sqlAppends = new AtomicInteger();
		AtomicInteger expectedSqlAppends = new AtomicInteger();

		stats.forEach((playerId, statsMap) -> expectedSqlAppends.getAndAdd(statsMap.size()));

		stats.forEach((playerId, statsMap) ->
		{
			String playerIdString = String.valueOf(playerId);

			statsMap.forEach((name, value) ->
			{
				_statsManager.loadStatId(name, statId ->
				{
					String statIdString = String.valueOf(statId);
					String statValueString = String.valueOf(value);

					String sql = SAVE_STAT
							.replaceFirst(SQL_REGEX, gameIdString)
							.replaceFirst(SQL_REGEX, playerIdString)
							.replaceFirst(SQL_REGEX, statIdString)
							.replaceFirst(SQL_REGEX, statValueString);

					builder.append(sql);
					sqlAppends.getAndIncrement();
				});
			});
		});

		while (sqlAppends.get() < expectedSqlAppends.get())
		{
			if (UtilTime.elapsed(start, MAX_RESPONSE_TIME))
			{
				return;
			}

			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		try (
				Connection connection = getConnection()
		)
		{
			PreparedStatement preparedStatement = connection.prepareStatement(builder.toString());

			preparedStatement.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void getGameStats(Consumer<GameStats> callback, int gameId)
	{
		executeQuery(GET_BY_ID, resultSet ->
		{

			if (resultSet.next())
			{
				GameStats stats = new GameStats(gameId, Region.valueOf(resultSet.getString("region")), GameDisplay.getById(gameId));
				callback.accept(stats);
			}

		}, new ColumnInt("gameId", gameId));
	}
}
