package mineplex.core.report.data.metrics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import mineplex.core.report.ReportResultType;
import mineplex.serverdata.database.DBPool;

/**
 * Handles all fetching of report-related metrics.
 */
public class ReportMetricsRepository
{
	private static final String GET_GLOBAL_SUBMITTED = "SELECT COUNT(DISTINCT reportReasons.reportId) AS submitted FROM reportReasons\n" +
			"  WHERE reportReasons.time BETWEEN NOW() - INTERVAL ? DAY AND NOW();";

	private static final String GET_GLOBAL_RESULT = "SELECT COUNT(reportResults.reportId) AS amount FROM reportResults\n" +
			"  WHERE reportResults.resultId = ?\n" +
			"    AND reportResults.closedTime BETWEEN NOW() - INTERVAL ? DAY AND NOW();";

	private static final String GET_USER_RESULT = "SELECT COUNT(reportResults.reportId) AS amount FROM reportResults\n" +
			"  LEFT JOIN reportHandlers ON reportResults.reportId = reportHandlers.reportId AND reportHandlers.aborted IS FALSE\n" +
			"  WHERE reportHandlers.handlerId = ?\n" +
			"    AND reportResults.resultId = ?\n" +
			"    AND reportResults.closedTime BETWEEN NOW() - INTERVAL ? DAY AND NOW();";

	private final Logger _logger;

	public ReportMetricsRepository(Logger logger)
	{
		_logger = logger;
	}

	public CompletableFuture<ReportGlobalMetrics> getGlobalMetrics(int days)
	{
		CompletableFuture<ReportGlobalMetrics> future = CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				long submitted = getGlobalSubmitted(connection, days);

				try (PreparedStatement preparedStatement = connection.prepareStatement(GET_GLOBAL_RESULT))
				{
					long expired = getGlobalResult(preparedStatement, ReportResultType.EXPIRED, days);
					long accepted = getGlobalResult(preparedStatement, ReportResultType.ACCEPTED, days);
					long denied = getGlobalResult(preparedStatement, ReportResultType.DENIED, days);
					long flagged = getGlobalResult(preparedStatement, ReportResultType.ABUSIVE, days);

					return new ReportGlobalMetrics(submitted, expired, accepted, denied, flagged);
				}
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});

		future.exceptionally(throwable ->
		{
			_logger.log(Level.SEVERE, "Error fetching global metrics.", throwable);
			return null;
		});

		return future;
	}

	private long getGlobalSubmitted(Connection connection, int days) throws SQLException
	{
		PreparedStatement preparedStatement = connection.prepareStatement(GET_GLOBAL_SUBMITTED);
		preparedStatement.setInt(1, days);

		try (ResultSet resultSet = preparedStatement.executeQuery())
		{
			if (resultSet.next())
			{
				return resultSet.getLong("submitted");
			}
			else
			{
				return 0;
			}
		}
	}

	private long getGlobalResult(PreparedStatement preparedStatement, ReportResultType resultType, int days) throws SQLException
	{
		preparedStatement.setShort(1, resultType.getId());
		preparedStatement.setInt(2, days);

		try (ResultSet resultSet = preparedStatement.executeQuery())
		{
			if (resultSet.next())
			{
				return resultSet.getLong("amount");
			}
			else
			{
				return 0;
			}
		}
	}

	public CompletableFuture<ReportMetrics> getUserMetrics(int accountId, int days)
	{
		CompletableFuture<ReportMetrics> future = CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				try (PreparedStatement preparedStatement = connection.prepareStatement(GET_USER_RESULT))
				{
					long accepted = getUserResult(preparedStatement, accountId, ReportResultType.ACCEPTED, days);
					long denied = getUserResult(preparedStatement, accountId, ReportResultType.DENIED, days);
					long flagged = getUserResult(preparedStatement, accountId, ReportResultType.ABUSIVE, days);

					return new ReportMetrics(accepted, denied, flagged);
				}
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});

		future.exceptionally(throwable ->
		{
			_logger.log(Level.SEVERE, "Error fetching user metrics.", throwable);
			return null;
		});

		return future;
	}

	private long getUserResult(PreparedStatement preparedStatement, int accountId, ReportResultType resultType, int days) throws SQLException
	{
		preparedStatement.setInt(1, accountId);
		preparedStatement.setShort(2, resultType.getId());
		preparedStatement.setInt(3, days);

		try (ResultSet resultSet = preparedStatement.executeQuery())
		{
			if (resultSet.next())
			{
				return resultSet.getLong("amount");
			}
			else
			{
				return 0;
			}
		}
	}
}
