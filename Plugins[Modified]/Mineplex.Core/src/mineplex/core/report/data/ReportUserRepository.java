package mineplex.core.report.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import mineplex.core.common.util.UtilFuture;
import mineplex.core.report.ReportCategory;
import mineplex.core.report.ReportResultType;
import mineplex.core.report.ReportTeam;
import mineplex.serverdata.database.DBPool;

/**
 * Handles creating {@link ReportUser} instances from report data stored in the database.
 */
public class ReportUserRepository
{
	private static final String GET_TEAMS = "SELECT teamId FROM reportTeamMemberships WHERE accountId = ?;";

	private static final String INSERT_TEAM = "INSERT IGNORE INTO reportTeamMemberships (accountId, teamId) VALUES (?, ?);";

	private static final String GET_TEAM_MEMBERS = "SELECT accountId FROM reportTeamMemberships WHERE teamId = ?;";

	private static final String GRAB_RESULT_COUNT = "SELECT reports.categoryId, results.resultId, COUNT(*) AS count" +
			"	FROM reports, reportReasons reasons, reportResults results, reportResultTypes resultTypes" +
			"	WHERE results.reportId = reports.id" +
			"		AND reasons.reportId = reports.id" +
			"    	AND resultTypes.id = results.resultId" +
			"	  	AND reasons.reporterId = ?" +
			"	GROUP BY reports.categoryId, results.resultId;";

	private final JavaPlugin _plugin;

	private final Cache<Integer, ReportUser> _cachedUsers = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.weakValues()
			.build();

	public ReportUserRepository(JavaPlugin plugin)
	{
		_plugin = plugin;
	}

	public CompletableFuture<List<Integer>> getTeamMembers(ReportTeam team)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement preparedStatement = connection.prepareStatement(GET_TEAM_MEMBERS);
				preparedStatement.setInt(1, team.getDatabaseId());

				ResultSet resultSet = preparedStatement.executeQuery();
				List<Integer> memberIds = new ArrayList<>();

				while (resultSet.next())
				{
					memberIds.add(resultSet.getInt("accountId"));
				}

				return memberIds;
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});
	}

	public CompletableFuture<List<ReportUser>> getUsers(Collection<Integer> accountIds)
	{
		return UtilFuture.sequence(
				accountIds.stream()
						.map(this::getUser)
						.collect(Collectors.toList())
		).thenApply(users ->
				users.stream().filter(user -> user != null)
						.collect(Collectors.toList())
		);
	}

	public CompletableFuture<ReportUser> getUser(int accountId)
	{
		ReportUser cachedUser = _cachedUsers.getIfPresent(accountId);

		if (cachedUser != null)
		{
			return CompletableFuture.completedFuture(cachedUser);
		}
		else
		{
			return CompletableFuture.supplyAsync(() ->
			{
				ReportUser user = new ReportUser(accountId);

				try (Connection connection = DBPool.getAccount().getConnection())
				{
					loadTeams(connection, user);
					loadStatistics(connection, user);
					_cachedUsers.put(accountId, user);
					return user;
				}
				catch (SQLException e)
				{
					throw new RuntimeException(e);
				}
			}).exceptionally(throwable ->
			{
				_plugin.getLogger().log(Level.SEVERE, "Error fetching ReportUser (id: " + accountId + ").", throwable);
				return null;
			});
		}
	}

	private void loadStatistics(Connection connection, ReportUser user) throws SQLException
	{
		int accountId = user.getAccountId();
		PreparedStatement preparedStatement = connection.prepareStatement(GRAB_RESULT_COUNT);
		preparedStatement.setInt(1, accountId);
		ResultSet resultSet = preparedStatement.executeQuery();

		while (resultSet.next())
		{
			try
			{
				int categoryId = resultSet.getInt("reports.categoryId");
				int resultTypeId = resultSet.getInt("results.resultId");
				int count = resultSet.getInt("count");

				ReportCategory category = ReportCategory.getById(categoryId);
				ReportResultType resultType = ReportResultType.getById(resultTypeId);

				Preconditions.checkNotNull(category, "Invalid category id: " + categoryId);
				Preconditions.checkNotNull(resultType, "Invalid result type id: " + resultType);

				if (resultType.isGlobalStat())
				{
					category = ReportCategory.GLOBAL;
				}

				user.setValue(category, resultType, count);
			}
			catch (Exception ex)
			{
				_plugin.getLogger().log(Level.SEVERE, "Error getting ReportUser (id: " + accountId + ").", ex);
			}
		}
	}

	private void loadTeams(Connection connection, ReportUser user) throws SQLException
	{
		PreparedStatement preparedStatement = connection.prepareStatement(GET_TEAMS);
		preparedStatement.setInt(1, user.getAccountId());
		ResultSet resultSet = preparedStatement.executeQuery();

		while (resultSet.next())
		{
			short teamId = resultSet.getShort("teamId");
			ReportTeam team = ReportTeam.getById(teamId);

			if (team != null)
			{
				user.addTeam(team);
			}
			else
			{
				_plugin.getLogger().log(Level.WARNING, "No definition for team with id: " + teamId);
			}
		}
	}

	public CompletableFuture<Void> updateUser(ReportUser user)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				insertTeams(connection, user);
				return null;
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});
	}

	private void insertTeams(Connection connection, ReportUser user) throws SQLException
	{
		PreparedStatement preparedStatement = connection.prepareStatement(INSERT_TEAM);

		for (ReportTeam team : user.getTeams())
		{
			preparedStatement.setInt(1, user.getAccountId());
			preparedStatement.setShort(2, team.getDatabaseId());
			preparedStatement.addBatch();
		}

		preparedStatement.executeBatch();
	}
}
