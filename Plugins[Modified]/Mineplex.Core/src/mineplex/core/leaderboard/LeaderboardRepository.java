package mineplex.core.leaderboard;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import mineplex.core.common.util.UtilServer;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;

public class LeaderboardRepository extends RepositoryBase
{
	private static final String CREATE_TOTAL = "CREATE TABLE accountStatsAllTime (accountId INT NOT NULL, statId INT NOT NULL, value BIGINT NOT NULL, PRIMARY KEY (accountId, statId), INDEX valueIndex (value DESC), FOREIGN KEY (accountId) REFERENCES accounts(id), FOREIGN KEY (statId) REFERENCES stats(id));";
	private static final String CREATE_SEASONAL = "CREATE TABLE accountStatsSeasonal (accountId INT NOT NULL, statId INT NOT NULL, seasonId SMALLINT NOT NULL, value BIGINT NOT NULL, PRIMARY KEY (accountId, statId), INDEX valueIndex (value DESC), INDEX seasonIndex (seasonId), FOREIGN KEY (accountId) REFERENCES accounts(id), FOREIGN KEY (statId) REFERENCES stats(id), FOREIGN KEY (seasonId) REFERENCES statSeasons(id));";
	private static final String CREATE_YEARLY = "CREATE TABLE accountStatsYearly (accountId INT NOT NULL, statId INT NOT NULL, date DATE NOT NULL, value BIGINT NOT NULL, PRIMARY KEY (accountId, statId), INDEX valueIndex (value DESC), INDEX dateIndex (date), FOREIGN KEY (accountId) REFERENCES accounts(id), FOREIGN KEY (statId) REFERENCES stats(id));";
	private static final String CREATE_MONTHLY = "CREATE TABLE accountStatsMonthly (accountId INT NOT NULL, statId INT NOT NULL, date DATE NOT NULL, value BIGINT NOT NULL, PRIMARY KEY (accountId, statId), INDEX valueIndex (value DESC), INDEX dateIndex (date), FOREIGN KEY (accountId) REFERENCES accounts(id), FOREIGN KEY (statId) REFERENCES stats(id));";
	private static final String CREATE_WEEKLY = "CREATE TABLE accountStatsWeekly (accountId INT NOT NULL, statId INT NOT NULL, date DATE NOT NULL, value BIGINT NOT NULL, PRIMARY KEY (accountId, statId), INDEX valueIndex (value DESC), INDEX dateIndex (date), FOREIGN KEY (accountId) REFERENCES accounts(id), FOREIGN KEY (statId) REFERENCES stats(id));";
	private static final String CREATE_DAILY = "CREATE TABLE accountStatsDaily (accountId INT NOT NULL, statId INT NOT NULL, date DATE NOT NULL, value BIGINT NOT NULL, PRIMARY KEY (accountId, statId), INDEX valueIndex (value DESC), INDEX dateIndex (date), FOREIGN KEY (accountId) REFERENCES accounts(id), FOREIGN KEY (statId) REFERENCES stats(id));";
	private static final String CREATE_SEASON = "CREATE TABLE statSeasons (id SMALLINT NOT NULL, seasonName VARCHAR(50) NOT NULL, startDate TIMESTAMP NOT NULL DEFAULT '1969-12-31 18:00:01', endDate TIMESTAMP NOT NULL DEFAULT '1969-12-31 18:00:01', PRIMARY KEY (id), UNIQUE INDEX seasonIndex (seasonName), INDEX startIndex (startDate), INDEX endIndex (endDate));";
	
	private static final String FETCH_STAT_ALL = "SELECT a.name, a.uuid, sl.value FROM (SELECT accountId, value FROM accountStatsAllTime WHERE statId=(SELECT id FROM stats WHERE name='%STAT%') ORDER BY value DESC LIMIT %START%,%LIMIT%) AS sl INNER JOIN accounts AS a ON a.id=sl.accountId;";
	
	private static final String FETCH_STAT_YEARLY = "SELECT a.name, a.uuid, sl.value FROM (SELECT accountId, value FROM accountStatsYearly WHERE (date BETWEEN MAKEDATE(YEAR(CURDATE()),1) AND CURDATE()) AND statId=(SELECT id FROM stats WHERE name='%STAT%') ORDER BY value DESC LIMIT %START%,%LIMIT%) AS sl INNER JOIN accounts AS a ON a.id=sl.accountId;";
	private static final String FETCH_STAT_MONTHLY = "SELECT a.name, a.uuid, sl.value FROM (SELECT accountId, value FROM accountStatsMonthly WHERE (date BETWEEN DATE_FORMAT(CURDATE(),'%Y-%m-01') AND CURDATE()) AND statId=(SELECT id FROM stats WHERE name='%STAT%') ORDER BY value DESC LIMIT %START%,%LIMIT%) AS sl INNER JOIN accounts AS a ON a.id=sl.accountId;";
	private static final String FETCH_STAT_WEEKLY = "SELECT a.name, a.uuid, sl.value FROM (SELECT accountId, value FROM accountStatsWeekly WHERE (date BETWEEN SUBDATE(CURDATE(), dayofweek(CURDATE())-1) AND CURDATE()) AND statId=(SELECT id FROM stats WHERE name='%STAT%') ORDER BY value DESC LIMIT %START%,%LIMIT%) AS sl INNER JOIN accounts AS a ON a.id=sl.accountId;";
	private static final String FETCH_STAT_DAILY = "SELECT a.name, a.uuid, sl.value FROM (SELECT accountId, value FROM accountStatsDaily WHERE date = CURDATE() AND statId=(SELECT id FROM stats WHERE name='%STAT%') ORDER BY value DESC LIMIT %START%,%LIMIT%) AS sl INNER JOIN accounts AS a ON a.id=sl.accountId;";
	
	private static final String FETCH_STAT_ALL_SEASON = "SELECT a.name, a.uuid, sl.value FROM (SELECT accountId, value FROM accountStatsSeasonal WHERE statId=(SELECT id FROM stats WHERE name='%STAT%') AND seasonId=(SELECT id FROM statSeasons WHERE now() BETWEEN startDate AND endDate LIMIT 1) ORDER BY value DESC LIMIT %START%,%LIMIT%) AS sl INNER JOIN accounts AS a ON a.id=sl.accountId;";
	
	public LeaderboardRepository()
	{
		super(DBPool.getAccount());
	}
	
	public void loadLeaderboard(Leaderboard board, Consumer<Map<String, Long>> leaderboard)
	{
		UtilServer.runAsync(() ->
		{
			Map<String, Long> names = new LinkedHashMap<>();
			try (
				Connection c = getConnection();
				Statement s = c.createStatement();
				)
			{
				s.execute(board.getType().getStatement(board.getStatIds(), board.getStart(), board.getSize()));
				for (int i = 0; i < board.getStatIds().length; i++)
				{
					try (ResultSet rs = s.getResultSet())
					{
						while (rs.next())
						{
							names.merge(rs.getString("name"), rs.getLong("value"), Long::sum);
						}
						
						if (!s.getMoreResults())
						{
							break;
						}
					}
				}
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
			}
			finally
			{
				UtilServer.runSync(() -> leaderboard.accept(names));
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public void loadLeaderboards(Collection<Leaderboard> boards, Consumer<Map<String, Long>[]> leaderboard)
	{
		UtilServer.runAsync(() ->
		{
			Map<String, Long>[] leaderboards = new Map[boards.size()];
			StringBuilder queryBuilder = new StringBuilder();
			{
				int i = 0;
				for (Leaderboard board : boards)
				{
					queryBuilder.append(board.getType().getStatement(board.getStatIds(), board.getStart(), board.getSize()));
					leaderboards[i] = new LinkedHashMap<>();
					i++;
				}
			}
			
			if (queryBuilder.length() > 0)
			{
				try (
					Connection c = getConnection();
					Statement s = c.createStatement();
					)
				{
					s.execute(queryBuilder.toString());
					int index = 0;
					mainBoardLoop: for (Leaderboard board : boards)
					{
						for (int i = 0; i < board.getStatIds().length; i++)
						{
							try (ResultSet rs = s.getResultSet())
							{
								while (rs.next())
								{
									leaderboards[index].merge(rs.getString("name"), rs.getLong("value"), Long::sum);
								}
								
								if (!s.getMoreResults())
								{
									break mainBoardLoop;
								}
							}
						}
						index++;
					}
				}
				catch (SQLException ex)
				{
					ex.printStackTrace();
				}
				finally
				{
					UtilServer.runSync(() -> leaderboard.accept(leaderboards));
				}
			}
			else
			{
				UtilServer.runSync(() -> leaderboard.accept(leaderboards));
			}
		});
	}
	
	public enum LeaderboardSQLType
	{
		DAILY(FETCH_STAT_DAILY),
		WEEKLY(FETCH_STAT_WEEKLY),
		MONTHLY(FETCH_STAT_MONTHLY),
		YEARLY(FETCH_STAT_YEARLY),
		ALL(FETCH_STAT_ALL),
		ALL_SEASON(FETCH_STAT_ALL_SEASON)
		;
		
		private String _sql;
		
		LeaderboardSQLType(String sql)
		{
			_sql = sql;
		}
		
		public String getStatement(int[] statIds, int start, int limit)
		{
			StringBuilder statementBuilder = new StringBuilder();
			for (int id : statIds)
			{
				statementBuilder.append(_sql.replace("%STAT%", String.valueOf(id)).replace("%START%", String.valueOf(start)).replace("%LIMIT%", String.valueOf(limit)));
			}
			return statementBuilder.toString();
		}
	}
}