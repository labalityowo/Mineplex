package mineplex.core.report.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mysql.jdbc.Statement;
import mineplex.core.chatsnap.SnapshotMetadata;
import mineplex.core.common.util.UtilTime;
import mineplex.core.report.ReportCategory;
import mineplex.core.report.ReportManager;
import mineplex.core.report.ReportResult;
import mineplex.core.report.ReportResultType;
import mineplex.core.report.ReportRole;
import mineplex.core.report.ReportTeam;
import mineplex.serverdata.Region;
import mineplex.serverdata.database.DBPool;
import org.apache.commons.lang3.StringUtils;

/**
 * Handles saving and loading report data to and from the database.
 */
public class ReportRepository
{
	private static final String INSERT_REPORT = "INSERT INTO reports (suspectId, categoryId, snapshotId, assignedTeam, region)\n" +
			"VALUES (?, ?, ?, ?, ?);";

	private static final String UPDATE_REPORT = "UPDATE reports SET snapshotId = ?, assignedTeam = ? WHERE id = ?;";

	private static final String SET_REPORT_MESSAGE = "REPLACE INTO reportReasons (reportId, reporterId, reason, `server`, weight, `time`)" +
			"  VALUES (?, ?, ?, ?, ?, ?);";

	private static final String INSERT_REPORT_HANDLER = "INSERT IGNORE INTO reportHandlers (reportId, handlerId)" +
			"  VALUES (?, ?);";

	private static final String SET_REPORT_RESULT = "REPLACE INTO reportResults (reportId, resultId, reason, closedTime)" +
			"  VALUES (?, ?, ?, ?);";

	private static final String SET_HANDLER_ABORTED = "UPDATE reportHandlers" +
			"  SET aborted = ?" +
			"  WHERE reportId = ?" +
			"    AND handlerId = ?;";

	private static final String GET_REPORT = "SELECT * FROM reports" +
			"  LEFT JOIN reportHandlers ON reports.id = reportHandlers.reportId AND reportHandlers.aborted IS FALSE" +
			"  LEFT JOIN reportResults ON reports.id = reportResults.reportId" +
			"  WHERE reports.id = ?;";

	private static final String GET_REPORT_REASONS = "SELECT * FROM reportReasons" +
			"  WHERE reportId = ?" +
			"  ORDER BY `time` ASC;";

	private static final String GET_UNHANDLED_REPORTS = "SELECT reports.id FROM reports\n" +
			"  LEFT JOIN reportResults ON reports.id = reportResults.reportId\n" +
			"  LEFT JOIN reportHandlers ON reports.id = reportHandlers.reportId\n" +
			"  LEFT JOIN reportReasons ON reports.id = reportReasons.reportId\n" +
			"WHERE reports.categoryId = ?\n" +
			"      AND (reports.region IS NULL OR reports.region = ?)\n" +
			"      AND reportResults.reportId IS NULL\n" +
			"      /* Bypass for testing purposes or check player isn't suspect */\n" +
			"      AND (? IS TRUE OR reports.suspectId != ?)\n" +
			"      /* If team is assigned, make sure user is member of team */\n" +
			"      AND (reports.assignedTeam IS NULL OR reports.assignedTeam IN\n" +
			"                                           (SELECT teamId FROM reportTeamMemberships WHERE accountId = ?))\n" +
			"GROUP BY reports.id\n" +
			"HAVING SUM(IF(reportHandlers.handlerId != ?, 1, 0)) = COUNT(reportHandlers.handlerId)\n" +
			"       /* Check all previous handlers have aborted this report */\n" +
			"       AND SUM(IF(reportHandlers.aborted IS TRUE, 1, 0)) = COUNT(reportHandlers.handlerId)\n" +
			"       /* Bypass for testing purposes or check player isn't a reporter */\n" +
			"       AND (? IS TRUE OR SUM(IF(reportReasons.reporterId != ?, 1, 0)) = COUNT(reportReasons.reporterId));";

	private static final String GET_ONGOING_REPORT = "SELECT reports.id FROM reports\n" +
			"  LEFT JOIN reportResults ON reports.id = reportResults.reportId\n" +
			"WHERE reportResults.reportId IS NULL\n" +
			"      AND reports.suspectId = ?\n" +
			"      AND (reports.region IS NULL OR reports.region = ?);";

	private static final String GET_ONGOING_REPORT_CATEGORY = "SELECT reports.id FROM reports\n" +
			"  LEFT JOIN reportResults ON reports.id = reportResults.reportId\n" +
			"WHERE reportResults.reportId IS NULL\n" +
			"      AND reports.suspectId = ?\n" +
			"      AND reports.categoryId = ?\n" +
			"      AND (reports.region IS NULL OR reports.region = ?);";

	private static final String GET_REPORTS_HANDLING = "SELECT reports.id FROM reports\n" +
			"  LEFT JOIN reportResults ON reports.id = reportResults.reportId\n" +
			"  INNER JOIN reportHandlers ON reports.id = reportHandlers.reportId\n" +
			"WHERE reportResults.reportId IS NULL\n" +
			"      AND reportHandlers.handlerId = ?\n" +
			"      AND reportHandlers.aborted IS FALSE\n" +
			"      AND (reports.region IS NULL OR reports.region = ?);";

	private static final String GET_USER_OPEN_REPORTS = "SELECT reports.id FROM reports\n" +
			"  INNER JOIN reportReasons ON reports.id = reportReasons.reportId\n" +
			"  LEFT JOIN reportResults ON reports.id = reportResults.reportId\n" +
			"WHERE reportResults.reportId IS NULL\n" +
			"  AND reportReasons.reporterId = ?;";

	private static final String GET_USER_RESULT_COUNT = "SELECT COUNT(reports.id) AS resultCount" +
			"  FROM reports, reportReasons, reportResults" +
			"  WHERE reports.id = reportReasons.reportId" +
			"    AND reports.id = reportResults.reportId" +
			"    AND reportReasons.reporterId = ?" +
			"    AND reportResults.resultId = ?;";

	// We order by lastLogin in the below queries to resolve cases whereby two account
	// entries exist with the same name (online and offline mode versions), to get
	// around this, we simply picked the one that logged in most recently

	private static final String GET_ACCOUNT_ID = "SELECT id, `name` FROM accounts\n" +
			"WHERE `name` = ?\n" +
			"ORDER BY lastLogin DESC\n" +
			"LIMIT 1;";

	private static final String GET_ACCOUNT_NAME = "SELECT id, `name` FROM accounts\n" +
			"WHERE id = ?\n" +
			"ORDER BY lastLogin DESC\n" +
			"LIMIT 1;";

	private static final String GET_ACCOUNT_UUID = "SELECT id, uuid FROM accounts" +
			"  WHERE id IN (%s);";

	/** STATISTICS **/

	private static final String STATISTICS_GET_REPORTS_MADE = "SELECT reports.id FROM reports, reportReasons\n" +
			"WHERE reports.id = reportReasons.reportId\n" +
			"      AND reportReasons.reporterId = ?;";

	private static final String STATISTICS_GET_REPORTS_HANDLED = "SELECT reports.id FROM reports, reportHandlers\n" +
			"WHERE reports.id = reportHandlers.reportId\n" +
			"      AND reportHandlers.handlerId = ?\n" +
			"      AND reportHandlers.aborted IS FALSE;";

	private static final String STATISTICS_GET_REPORTS_AGAINST = "SELECT reports.id FROM reports\n" +
			"WHERE reports.suspectId = ?;";

	private final ReportManager _reportManager;
	private final Region _region;
	private final Logger _logger;

	private final Cache<Long, Report> _cachedReports = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build();

	public ReportRepository(ReportManager reportManager, Region region, Logger logger)
	{
		_reportManager = reportManager;
		_region = region;
		_logger = logger;
	}

	/**
	 * Gets the ids of unhandled reports that the supplied user is allowed to handle.
	 *
	 * @param handlerId the id of the account carrying out the search
	 * @param devMode if true, allows various restrictions to be bypassed
	 * @return the ids of unhandled reports the supplied account is allowed to handle
	 */
	public CompletableFuture<List<Long>> getUnhandledReports(int handlerId, ReportCategory category, boolean devMode)
	{
		CompletableFuture<List<Long>> future = CompletableFuture.supplyAsync(() ->
		{
			List<Long> unhandledReports = new ArrayList<>();

			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement preparedStatement = connection.prepareStatement(GET_UNHANDLED_REPORTS);
				preparedStatement.setShort(1, category.getId());
				preparedStatement.setString(2, _region.name());
				preparedStatement.setBoolean(3, devMode);
				preparedStatement.setInt(4, handlerId);
				preparedStatement.setInt(5, handlerId);
				preparedStatement.setInt(6, handlerId);
				preparedStatement.setBoolean(7, devMode);
				preparedStatement.setInt(8, handlerId);
				ResultSet resultSet = preparedStatement.executeQuery();

				while (resultSet.next())
				{
					unhandledReports.add(resultSet.getLong("id"));
				}
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}

			return unhandledReports;
		});

		future.exceptionally(throwable ->
		{
			_logger.log(Level.SEVERE, "Error whilst fetching unhandled reports.", throwable);
			return new ArrayList<>();
		});

		return future;
	}

	/**
	 * Gets a list containing the ids of reports the account is handling
	 * @param handlerId the id of the account
	 * @return a list containing the ids of reports being handled
	 */
	public CompletableFuture<List<Long>> getReportsHandling(int handlerId)
	{
		CompletableFuture<List<Long>> future = CompletableFuture.supplyAsync(() ->
		{
			List<Long> reportsHandling = new ArrayList<>();

			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement preparedStatement = connection.prepareStatement(GET_REPORTS_HANDLING);
				preparedStatement.setInt(1, handlerId);
				preparedStatement.setString(2, _region.name());

				ResultSet resultSet = preparedStatement.executeQuery();

				while (resultSet.next())
				{
					reportsHandling.add(resultSet.getLong("reports.id"));
				}
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}

			return reportsHandling;
		});

		future.exceptionally(throwable -> null);

		return future;
	}

	/**
	 * Get reports by id in bulk.
	 *
	 * @param reportIds the ids of the reports to fetch
	 * @return the requested reports
	 */
	public CompletableFuture<List<Report>> getReports(Collection<Long> reportIds)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				List<Report> reports = new ArrayList<>();
				PreparedStatement preparedStatement = connection.prepareStatement(GET_REPORT);

				for (long reportId : reportIds)
				{
					Report report = _cachedReports.getIfPresent(reportId);

					if (report == null)
					{
						preparedStatement.setLong(1, reportId);
						ResultSet resultSet = preparedStatement.executeQuery();

						report = loadReport(connection, resultSet);

						if (report == null)
						{
							continue;
						}
					}

					reports.add(report);
				}

				return reports;
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Gets a report by id.
	 *
	 * Attempts to fetch the report from local cache, if the report is not present in cache
	 * then the report is fetched from the database.
	 *
	 * @param reportId the id of the report to fetch
	 * @return the report, null if report by that id wasn't found
	 */
	public CompletableFuture<Optional<Report>> getReport(long reportId)
	{
		if (reportId != -1)
		{
			Report cachedReport = _cachedReports.getIfPresent(reportId);

			if (cachedReport != null)
			{
				return CompletableFuture.completedFuture(Optional.of(cachedReport));
			}
			else
			{
				CompletableFuture<Optional<Report>> future = CompletableFuture.supplyAsync(() ->
				{
					try (Connection connection = DBPool.getAccount().getConnection())
					{
						PreparedStatement preparedStatement = connection.prepareStatement(GET_REPORT);
						preparedStatement.setLong(1, reportId);

						ResultSet resultSet = preparedStatement.executeQuery();
						return Optional.ofNullable(loadReport(connection, resultSet));
					}
					catch (SQLException e)
					{
						throw new RuntimeException(e);
					}
				});

				future.exceptionally(throwable ->
				{
					_logger.log(Level.SEVERE, "Error fetching report (id: " + reportId + ").", throwable);
					return null;
				});

				return future;
			}
		}
		else
		{
			return CompletableFuture.completedFuture(null);
		}
	}

	private Report loadReport(Connection connection, ResultSet resultSet) throws SQLException
	{
		if (resultSet.next())
		{
			long reportId = resultSet.getLong("id");
			int suspectId = resultSet.getInt("suspectId");
			ReportCategory reportCategory = ReportCategory.getById(resultSet.getInt("categoryId"));
			String regionName = resultSet.getString("region");
			Region region = !resultSet.wasNull() ? Region.valueOf(regionName) : null;

			Report report = new Report(reportId, suspectId, reportCategory, region);

			int snapshotId = resultSet.getInt("snapshotId");
			if (!resultSet.wasNull())
			{
				SnapshotMetadata snapshotMetadata = _reportManager.getSnapshotManager().getRepository()
						.getSnapshotMetadata(connection, snapshotId).join();

				report.setSnapshotMetadata(snapshotMetadata);
			}

			int handlerId = resultSet.getInt("handlerId");
			if (!resultSet.wasNull())
			{
				report.setHandlerId(handlerId);
			}

			short teamId = resultSet.getShort("assignedTeam");
			if (!resultSet.wasNull())
			{
				ReportTeam team = ReportTeam.getById(teamId);

				if (team != null)
				{
					report.setAssignedTeam(team);
				}
				else
				{
					_logger.log(Level.WARNING, "Unrecognised report team found in database: " + teamId);
				}
			}

			Set<ReportMessage> reportMessages = getReportReasons(connection, reportId);
			reportMessages.forEach(report::addReportReason);

			int resultId = resultSet.getInt("resultId");
			if (!resultSet.wasNull())
			{
				ReportResultType resultType = ReportResultType.getById(resultId);
				String reason = resultSet.getString("reason");
				LocalDateTime closedTime = UtilTime.fromTimestamp(resultSet.getTimestamp("closedTime"));
				report.setReportResult(new ReportResult(resultType, reason, closedTime));
			}

			shouldCacheReport(report).thenAccept(shouldCache ->
			{
				if (shouldCache)
				{
					_cachedReports.put(reportId, report);
				}
			});

			return report;
		}

		return null;
	}

	public CompletableFuture<List<Report>> getOngoingReports(int suspectId)
	{
		CompletableFuture<List<Report>> future = CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				List<Report> reports = new ArrayList<>();
				PreparedStatement preparedStatement = connection.prepareStatement(GET_ONGOING_REPORT);
				preparedStatement.setInt(1, suspectId);
				preparedStatement.setString(2, _region.name());

				ResultSet resultSet = preparedStatement.executeQuery();
				while (resultSet.next())
				{
					try
					{
						int id = resultSet.getInt("id");
						Optional<Report> reportOptional = getReport(id).join();

						if (reportOptional.isPresent())
						{
							Report report = reportOptional.get();

							if (_reportManager.isActiveReport(report).join())
							{
								reports.add(report);
							}
						}
					}
					catch (Exception e)
					{
						_logger.log(Level.SEVERE, "Error whilst getting ongoing reports.", e);
					}
				}

				return reports;
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});

		future.exceptionally(throwable ->
		{
			_logger.log(Level.SEVERE, "Error getting ongoing report for account: " + suspectId + ".", throwable);
			return null;
		});

		return future;
	}

	public CompletableFuture<List<Long>> getOngoingReports(int suspectId, ReportCategory category)
	{
		CompletableFuture<List<Long>> future = CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement preparedStatement = connection.prepareStatement(GET_ONGOING_REPORT_CATEGORY);
				preparedStatement.setInt(1, suspectId);
				preparedStatement.setInt(2, category.getId());
				preparedStatement.setString(3, _region.name());

				ResultSet resultSet = preparedStatement.executeQuery();
				List<Long> reports = new ArrayList<>();
				while (resultSet.next())
				{
					reports.add(resultSet.getLong("id"));
				}

				return reports;
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});

		future.exceptionally(throwable ->
		{
			_logger.log(Level.SEVERE, "Error fetching ongoing report for account: " + suspectId + ", category: " + category + ".", throwable);
			return new ArrayList<>();
		});

		return future;
	}

	public CompletableFuture<List<Long>> getOpenReports(int reporterId)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement preparedStatement = connection.prepareStatement(GET_USER_OPEN_REPORTS);
				preparedStatement.setInt(1, reporterId);

				ResultSet resultSet = preparedStatement.executeQuery();
				List<Long> reports = new ArrayList<>();

				while (resultSet.next())
				{
					reports.add(resultSet.getLong("id"));
				}

				return reports;
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});
	}

	private Set<ReportMessage> getReportReasons(Connection connection, long reportId)
	{
		Set<ReportMessage> reportMessages = new HashSet<>();

		try
		{
			PreparedStatement preparedStatement = connection.prepareStatement(GET_REPORT_REASONS);
			preparedStatement.setLong(1, reportId);

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next())
			{
				int reporterId = resultSet.getInt("reporterId");
				String reason = resultSet.getString("reason");
				String server = resultSet.getString("server");
				int weight = resultSet.getInt("weight");
				LocalDateTime date = UtilTime.fromTimestamp(resultSet.getTimestamp("time"));

				ReportMessage reportMessage = new ReportMessage(reporterId, reason, server, weight, date);
				reportMessages.add(reportMessage);
			}
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}

		return reportMessages;
	}

	public CompletableFuture<Integer> getResultCount(int reporterId, ReportResultType resultType)
	{
		CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement preparedStatement = connection.prepareStatement(GET_USER_RESULT_COUNT);
				preparedStatement.setInt(1, reporterId);
				preparedStatement.setInt(2, resultType.getId());

				ResultSet resultSet = preparedStatement.executeQuery();
				if (resultSet.next())
				{
					return resultSet.getInt("resultCount");
				}
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}

			return null;
		});

		future.exceptionally(throwable ->
		{
			_logger.log(Level.SEVERE, "Error fetching result count for account: " + reporterId + ", type: " + resultType + ".", throwable);
			return 0;
		});

		return future;
	}

	public CompletableFuture<Long> updateReport(Report report)
	{
		CompletableFuture<Long> future = CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				Optional<Long> reportIdOptional = report.getId();
				Optional<Region> regionOptional = report.getRegion();
				Optional<Integer> snapshotIdOptional = report.getSnapshotMetadata().map(SnapshotMetadata::getId);
				Optional<ReportTeam> teamOptional = report.getAssignedTeam();
				long reportId;

				if (reportIdOptional.isPresent())
				{
					reportId = reportIdOptional.get();

					try (PreparedStatement updateReportStatement = connection.prepareStatement(UPDATE_REPORT))
					{
						if (snapshotIdOptional.isPresent())
						{
							updateReportStatement.setInt(1, snapshotIdOptional.get());
						}
						else
						{
							updateReportStatement.setNull(1, Types.INTEGER);
						}

						if (teamOptional.isPresent())
						{
							updateReportStatement.setShort(2, teamOptional.get().getDatabaseId());
						}
						else
						{
							updateReportStatement.setNull(2, Types.TINYINT);
						}

						updateReportStatement.setLong(3, reportId);
						updateReportStatement.execute();
					}
				}
				else
				{
					try (PreparedStatement insertReportStatement = connection.prepareStatement(INSERT_REPORT, Statement.RETURN_GENERATED_KEYS))
					{
						insertReportStatement.setInt(1, report.getSuspectId());
						insertReportStatement.setInt(2, report.getCategory().getId());

						if (snapshotIdOptional.isPresent())
						{
							insertReportStatement.setInt(3, snapshotIdOptional.get());
						}
						else
						{
							insertReportStatement.setNull(3, Types.INTEGER);
						}

						if (teamOptional.isPresent())
						{
							insertReportStatement.setInt(4, teamOptional.get().getDatabaseId());
						}
						else
						{
							insertReportStatement.setNull(4, Types.TINYINT);
						}

						if (regionOptional.isPresent())
						{
							insertReportStatement.setString(5, regionOptional.get().name());
						}
						else
						{
							insertReportStatement.setNull(5, Types.VARCHAR);
						}

						insertReportStatement.executeUpdate();

						ResultSet resultSet = insertReportStatement.getGeneratedKeys();
						if (resultSet.next())
						{
							reportId = resultSet.getLong(1);
							report._reportId = reportId;
						}
						else
						{
							throw new IllegalStateException("Query did not return a report id (we need one).");
						}
					}
				}

				_cachedReports.put(reportId, report); // cache the report

				PreparedStatement setReportMessageStatement = connection.prepareStatement(SET_REPORT_MESSAGE);

				for (Map.Entry<Integer, ReportMessage> entry : report.getMessages().entrySet())
				{
					ReportMessage reportMessage = entry.getValue();
					setReportMessageStatement.setLong(1, reportId); // report id
					setReportMessageStatement.setInt(2, entry.getKey()); // reporter id
					setReportMessageStatement.setString(3, reportMessage.getMessage()); // reason
					setReportMessageStatement.setString(4, reportMessage.getServer()); // server
					setReportMessageStatement.setInt(5, reportMessage.getServerWeight()); // weight
					setReportMessageStatement.setTimestamp(6, UtilTime.toTimestamp(reportMessage.getTimeCreated())); // time
					setReportMessageStatement.addBatch();
				}

				setReportMessageStatement.executeBatch();

				Optional<Integer> handlerIdOptional = report.getHandlerId();
				if (handlerIdOptional.isPresent())
				{
					PreparedStatement setReportHandlerStatement = connection.prepareStatement(INSERT_REPORT_HANDLER);
					setReportHandlerStatement.setLong(1, reportId); // report id
					setReportHandlerStatement.setInt(2, handlerIdOptional.get()); // handler id
					setReportHandlerStatement.execute();
				}

				Optional<ReportResult> reportResultOptional = report.getResult();
				if (reportResultOptional.isPresent())
				{
					PreparedStatement setReportResultStatement = connection.prepareStatement(SET_REPORT_RESULT);
					ReportResult reportResult = reportResultOptional.get();
					setReportResultStatement.setLong(1, reportId); // report id
					setReportResultStatement.setInt(2, reportResult.getType().getId()); // result id
					setReportResultStatement.setString(3, reportResult.getReason().orElse(null)); // reason
					setReportResultStatement.setTimestamp(4, new Timestamp(reportResult.getClosedTime().atZone(UtilTime.CENTRAL_ZONE).toInstant().toEpochMilli())); // closed time
					setReportResultStatement.execute();
				}

				return reportId;
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});

		future.exceptionally(throwable ->
		{
			Optional<Long> idOptional = report.getId();
			_logger.log(Level.SEVERE, "Error updating report" + idOptional.map(id -> "(#" + id + ")").orElse("") + ".", throwable);
			return null;
		});

		return future;
	}

	public CompletableFuture<Void> setAborted(long reportId, int handlerId, boolean aborted)
	{
		CompletableFuture<Void> future = CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement preparedStatement = connection.prepareStatement(SET_HANDLER_ABORTED);
				preparedStatement.setBoolean(1, aborted);
				preparedStatement.setLong(2, reportId);
				preparedStatement.setInt(3, handlerId);
				preparedStatement.execute();
				return null;
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});

		future.exceptionally(throwable ->
		{
			_logger.log(Level.SEVERE, "Error setting handler report as aborted.", throwable);
			return null;
		});

		return future;
	}

	public CompletableFuture<Multimap<ReportRole, Long>> getAccountStatistics(int accountId)
	{
		CompletableFuture<Multimap<ReportRole, Long>> future = CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				Multimap<ReportRole, Long> reportIds = HashMultimap.create();
				reportIds.putAll(ReportRole.REPORTER, getReports(connection, STATISTICS_GET_REPORTS_MADE, accountId));
				reportIds.putAll(ReportRole.HANDLER, getReports(connection, STATISTICS_GET_REPORTS_HANDLED, accountId));
				reportIds.putAll(ReportRole.SUSPECT, getReports(connection, STATISTICS_GET_REPORTS_AGAINST, accountId));
				return reportIds;
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});

		future.exceptionally(throwable ->
		{
			_logger.log(Level.SEVERE, "Error getting account statistics.", throwable);
			return null;
		});

		return future;
	}

	private Set<Long> getReports(Connection connection, String sql, int accountId) throws SQLException
	{
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.setInt(1, accountId);

		ResultSet resultSet = preparedStatement.executeQuery();
		Set<Long> reportIds = new HashSet<>();
		while (resultSet.next())
		{
			reportIds.add(resultSet.getLong("reports.id"));
		}

		return reportIds;
	}

	/**
	 * Disposes of cached reports which are cached as a result of this user.
	 * This function is called when a user leaves the server.
	 *
	 * @param accountId the account id to clean the cached reports of
	 */
	public void clearCacheFor(int accountId)
	{
		Iterator<Report> iterator = _cachedReports.asMap().values().iterator();

		while (iterator.hasNext())
		{
			Report report = iterator.next();
			Optional<Integer> handlerIdOptional = report.getHandlerId();

			CompletableFuture<Boolean> disposeCacheFuture = CompletableFuture.completedFuture(false);

			if (report.getSuspectId() == accountId)
			{
				if (handlerIdOptional.isPresent())
				{
					disposeCacheFuture = checkUserOnline(handlerIdOptional.get());
				}
				else
				{
					// no handler so un-cache this report
					disposeCacheFuture = CompletableFuture.completedFuture(true);
				}
			}
			else if (handlerIdOptional.isPresent() && handlerIdOptional.get() == accountId)
			{
				disposeCacheFuture = checkUserOnline(report.getSuspectId());
			}

			disposeCacheFuture.thenAccept(dispose ->
			{
				if (dispose)
				{
					iterator.remove();
				}
			});
		}
	}

	public void clearCache(long reportId)
	{
		_cachedReports.invalidate(reportId);
	}

	/**
	 * Checks if either the suspect or handler (if any) are online.
	 * If either are online then this will return true, otherwise false.
	 *
	 * @param report the report to check if it should be cached
	 * @return true if this report should be cached, false otherwise
	 */
	private CompletableFuture<Boolean> shouldCacheReport(Report report)
	{
		return checkUserOnline(report.getSuspectId()).thenCompose(online ->
		{
			if (!online)
			{
				Optional<Integer> handlerIdOptional = report.getHandlerId();

				if (handlerIdOptional.isPresent())
				{
					return checkUserOnline(handlerIdOptional.get());
				}
				else
				{
					return CompletableFuture.completedFuture(false);
				}
			}

			return CompletableFuture.completedFuture(true);
		});
	}

	private CompletableFuture<Boolean> checkUserOnline(int accountId)
	{
		return getAccountUUID(accountId).thenApply(Bukkit::getPlayer).thenApply(player -> player != null);
	}

	public CompletableFuture<String> getAccountName(int accountId)
	{
		CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				try (PreparedStatement preparedStatement = connection.prepareStatement(GET_ACCOUNT_NAME))
				{
					preparedStatement.setInt(1, accountId);

					try (ResultSet resultSet = preparedStatement.executeQuery())
					{
						if (resultSet.next())
						{
							return resultSet.getString("name");
						}
					}
				}
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}

			return null;
		});

		future.exceptionally(throwable ->
		{
			_logger.log(Level.SEVERE, "Error whilst fetching name for account: " + accountId + ".");
			return null;
		});

		return future;
	}

	public CompletableFuture<UUID> getAccountUUID(int accountId)
	{
		return getAccountUUIDs(Collections.singletonList(accountId))
				.thenApply(integerUUIDMap -> integerUUIDMap.get(accountId));
	}

	public CompletableFuture<Map<Integer, UUID>> getAccountUUIDs(Collection<Integer> accountIds)
	{
		CompletableFuture<Map<Integer, UUID>> future = CompletableFuture.supplyAsync(() ->
		{
			Map<Integer, UUID> accountUUIDs = new HashMap<>();

			try (Connection connection = DBPool.getAccount().getConnection())
			{
				String query = String.format(GET_ACCOUNT_UUID, StringUtils.join(accountIds, ", "));

				try (PreparedStatement preparedStatement = connection.prepareStatement(query))
				{
					try (ResultSet resultSet = preparedStatement.executeQuery())
					{
						while (resultSet.next())
						{
							int accountId = resultSet.getInt("id");
							UUID accountUUID = UUID.fromString(resultSet.getString("uuid"));
							accountUUIDs.put(accountId, accountUUID);
						}
					}
				}
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}

			return accountUUIDs;
		});

		future.exceptionally(throwable ->
		{
			_logger.log(Level.SEVERE, "Error whilst fetching UUID(s).", throwable);
			return new HashMap<>();
		});

		return future;
	}

	public CompletableFuture<Optional<Integer>> getAccountId(String name)
	{
		CompletableFuture<Optional<Integer>> future = CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				try (PreparedStatement statement = connection.prepareStatement(GET_ACCOUNT_ID))
				{
					statement.setString(1, name);

					try (ResultSet resultSet = statement.executeQuery())
					{
						if (resultSet.next())
						{
							return Optional.of(resultSet.getInt("id"));
						}
						else
						{
							return Optional.empty();
						}
					}
				}
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		});

		future.exceptionally(throwable ->
		{
			_logger.log(Level.SEVERE, "Error whilst fetching id from name: " + name, throwable);
			return Optional.empty();
		});

		return future;
	}
}