package mineplex.core.report;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.chatsnap.SnapshotManager;
import mineplex.core.chatsnap.SnapshotMetadata;
import mineplex.core.chatsnap.redis.PushSnapshotsCommand;
import mineplex.core.chatsnap.redis.PushSnapshotsHandler;
import mineplex.core.command.CommandCenter;
import mineplex.core.common.jsonchat.ChildJsonMessage;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilCollections;
import mineplex.core.common.util.UtilFuture;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.portal.Portal;
import mineplex.core.punish.Category;
import mineplex.core.punish.Punish;
import mineplex.core.punish.PunishClient;
import mineplex.core.report.data.Report;
import mineplex.core.report.data.ReportMessage;
import mineplex.core.report.data.ReportRepository;
import mineplex.core.report.data.ReportUser;
import mineplex.core.report.data.ReportUserRepository;
import mineplex.core.report.data.metrics.ReportMetricsRepository;
import mineplex.core.report.redis.HandlerNotification;
import mineplex.core.report.redis.ReportersNotification;
import mineplex.serverdata.Region;
import mineplex.serverdata.commands.ServerCommandManager;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides methods for getting, using and manipulating reports.
 */
public class ReportManager
{
	public enum Perm implements Permission
	{
		REPORT_CLOSE_COMMAND,
		REPORT_COMMAND,
		REPORT_HANDLE_COMMAND,
		REPORT_HISTORY_COMMAND,
		REPORT_INFO_COMMAND,
		REPORT_METRICS_COMMAND,
	}
	
	private static final String NAME = "Report";
	private static final int INITIAL_PRIORITY = 15;
	private static final int ABUSE_BAN_THRESHOLD = 3;
	public static final int MAXIMUM_REPORTS = 5;

	private final JavaPlugin _plugin;
	private final SnapshotManager _snapshotManager;
	private final CoreClientManager _clientManager;
	private final IncognitoManager _incognitoManager;
	private final Punish _punish;
	private final Region _region;
	private final String _serverName;
	private final int _serverWeight;

	private final ReportRepository _reportRepository;
	private final ReportUserRepository _userRepository;
	private final ReportMetricsRepository _metricsRepository;

	public ReportManager(JavaPlugin plugin, SnapshotManager snapshotManager, CoreClientManager clientManager,
						 IncognitoManager incognitoManager, Punish punish, Region region, String serverName, int serverWeight)
	{
		_plugin = plugin;
		_snapshotManager = snapshotManager;
		_clientManager = clientManager;
		_incognitoManager = incognitoManager;
		_punish = punish;
		_region = region;
		_serverName = serverName;
		_serverWeight = serverWeight;

		_reportRepository = new ReportRepository(this, region, _plugin.getLogger());
		_userRepository = new ReportUserRepository(plugin);
		_metricsRepository = new ReportMetricsRepository(_plugin.getLogger());

		ServerCommandManager commandManager = ServerCommandManager.getInstance();
		ReportRedisManager notificationCallback = new ReportRedisManager(this, _serverName);
		PushSnapshotsHandler pushHandler = new PushSnapshotsHandler(this, snapshotManager);
		commandManager.registerCommandType("HandlerNotification", HandlerNotification.class, notificationCallback);
		commandManager.registerCommandType("ReportersNotification", ReportersNotification.class, notificationCallback);
		commandManager.registerCommandType("PushSnapshotsCommand", PushSnapshotsCommand.class, pushHandler);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.MOD.setPermission(Perm.REPORT_CLOSE_COMMAND, true, true);
		PermissionGroup.TITAN.setPermission(Perm.REPORT_COMMAND, true, true);
		PermissionGroup.MOD.setPermission(Perm.REPORT_HANDLE_COMMAND, true, true);
		PermissionGroup.MOD.setPermission(Perm.REPORT_HISTORY_COMMAND, true, true);
		PermissionGroup.MOD.setPermission(Perm.REPORT_INFO_COMMAND, true, true);
		PermissionGroup.MOD.setPermission(Perm.REPORT_METRICS_COMMAND, true, true);
	}

	public SnapshotManager getSnapshotManager()
	{
		return _snapshotManager;
	}

	/**
	 * Gets the {@link ReportRepository} this instance is using.
	 *
	 * @return the repository
	 */
	public ReportRepository getRepository()
	{
		return _reportRepository;
	}

	/**
	 * Gets the {@link ReportUserRepository} this instance is using.
	 *
	 * @return the repository
	 */
	public ReportUserRepository getUserRepository()
	{
		return _userRepository;
	}

	/**
	 * Gets the {@link ReportMetricsRepository} this instance is using.
	 *
	 * @return the repository
	 */
	public ReportMetricsRepository getMetricsRepository()
	{
		return _metricsRepository;
	}

	/**
	 * Creates a new report or adds to an existing one.
	 *
	 * @param reporter the player creating the report
	 * @param suspect the player that is being reported
	 * @param category the category of the report
	 * @param message the reason this player reported the other
	 * @return a future which when completed contains a {@link Report} instance of the report just created or added to
	 */
	public CompletableFuture<Report> createReport(Player reporter, Player suspect, ReportCategory category, String message)
	{
		int reporterId = _clientManager.Get(reporter).getAccountId();
		int suspectId = _clientManager.Get(suspect).getAccountId();
		return createReport(reporterId, suspectId, category, message);
	}


	/**
	 * Creates a new report or adds to an existing one.
	 *
	 * @param reporterId the account id of the player creating the report
	 * @param suspectId the account id of the suspect that is being reported
	 * @param category the category of the report
	 * @param message the reason this player reported the other
	 * @return a future which when completed contains a {@link Report} instance of the report just created or added to
	 */
	public CompletableFuture<Report> createReport(int reporterId, int suspectId, ReportCategory category, String message)
	{
		return fetchOrCreateReport(suspectId, category).whenComplete((report, throwable) ->
		{
			if (report != null)
			{
				ReportMessage reportMessage = report.getReportMessage(reporterId);

				if (reportMessage != null)
				{
					reportMessage.setMessage(message);
				}
				else
				{
					reportMessage = new ReportMessage(reporterId, message, _serverName, _serverWeight);
					report.addReportReason(reportMessage);
				}

				// create snapshot id ahead of time
				if (category == ReportCategory.CHAT_ABUSE)
				{
					SnapshotMetadata snapshotMetadata = _snapshotManager.getRepository().createSnapshot(null).join();
					report.setSnapshotMetadata(snapshotMetadata);
				}

				saveReport(report).join();
			}
		});
	}

	private CompletableFuture<Report> fetchOrCreateReport(int suspectId, ReportCategory category)
	{
		return _reportRepository.getOngoingReports(suspectId, category).thenCompose(reportIds ->
		{
			if (reportIds.size() > 0)
			{
				return _reportRepository.getReport(Collections.max(reportIds)).thenApply(reportOptional ->
						reportOptional.orElseGet(() ->
						{
							_plugin.getLogger().log(Level.WARNING, "Report #%d couldn't be fetched, opening new report.");
							return new Report(suspectId, category, _region);
						}));
			}
			else
			{
				return CompletableFuture.completedFuture(new Report(suspectId, category, _region));
			}
		});
	}

	/**
	 * Sets the defined player as the handler of the report.
	 * If this is a chat abuse report, all related chat snapshots are pushed to the database.
	 *
	 * @param report the report to set the player to be handler of
	 * @param reportHandler the player to set as the handler of the report
	 */
	public void handleReport(Report report, Player reportHandler)
	{
		checkNotNull(report);
		checkNotNull(reportHandler);

		long reportId = report.getId().orElseThrow(() -> new IllegalStateException("Report id is not present."));

		Optional<Integer> handlerIdOptional = report.getHandlerId();

		if (handlerIdOptional.isPresent())
		{
			_reportRepository.getAccountName(handlerIdOptional.get())
					.thenAccept(name ->
							reportHandler.sendMessage(
									F.main(
											getReportPrefix(reportId),
											String.format("%s is already handling this report.", name)
									)
							)
					);
		}
		else
		{
			int handlerId = _clientManager.Get(reportHandler).getAccountId();
			report.setHandlerId(handlerId);

			// Wait until this has been pushed to the database, otherwise the message task
			// will not function correctly
			saveReport(report).thenAccept(reportId2 ->
			{
				// Regularly show user details of the report
				new ReportHandlerTask(this, reportId).start(_plugin);
			});

			if (report.getCategory() != ReportCategory.CHAT_ABUSE)
			{
				if (!_incognitoManager.Get(reportHandler).Status)
				{
					_incognitoManager.toggle(reportHandler);
				}

				String lastServer = report.getLatestMessage().getServer();
				if (!_serverName.equals(lastServer))
				{
					Portal.transferPlayer(reportHandler.getName(), lastServer);
				}
			}

			reportHandler.sendMessage(
					F.main(getReportPrefix(reportId), C.cAqua + "You are now handling this report."));
		}
	}

	/**
	 * Closes an open report.
	 *
	 * @param report the report to close
	 * @param reportCloser the player who closing this report or null
	 * @param reportResult the result of the report
	 * @return future which can be used to monitor the status of the task
	 */
	public CompletableFuture<Void> closeReport(Report report, Player reportCloser, ReportResult reportResult)
	{
		checkNotNull(report);
		checkNotNull(reportResult);

		return _reportRepository.getAccountName(report.getSuspectId()).thenAccept(suspectName ->
		{
			if (reportCloser != null)
			{
				int closerId = _clientManager.Get(reportCloser).getAccountId();
				report.setHandlerId(closerId);
			}

			report.setReportResult(reportResult);
			report.cancelHandlerTask();

			CompletableFuture<Long> saveCompletableFuture = saveReport(report);

			saveCompletableFuture.thenAccept(reportId ->
			{
				try
				{
					if (reportResult.getType() == ReportResultType.ABUSIVE)
					{
						// if report was marked abusive, check if each of the reporters
						// should be banned from using the report system
						report.getReporterIds().forEach(reporterId ->
								_reportRepository.getAccountUUID(reporterId).thenAccept(reporterUUID ->
								{
									CoreClient reporterClient = _clientManager.Get(reporterUUID);
									checkAbuseBan(reporterClient, reportCloser, reportId);
								}));
					}

					Bukkit.getScheduler().runTask(_plugin, () ->
					{
						String prefix = getReportPrefix(reportId);
						String reason = reportResult.getReason().orElse("No reason specified.");

						if (reportCloser != null)
						{
							if (reportResult.getType() == ReportResultType.ACCEPTED)
							{
								// TODO: force moderator to choose a punishment (requires new punish gui)
								CommandCenter.Instance.onPlayerCommandPreprocess(
										new PlayerCommandPreprocessEvent(reportCloser,
												String.format("/punish %s Report #%d - %s", suspectName, reportId, reason)));
							}

							// TODO: send these after punishment has been decided (requires new punish gui)
							reportCloser.sendMessage(
									F.main(prefix, "Report marked as: "
											+ C.cGold + reportResult.getType().getName()));
							reportCloser.sendMessage(F.main(prefix, "Reason: " + F.elem(reason)));
						}

						getUUIDs(report.getReporterIds()).thenAccept(ids ->
						{
							ReportResultType resultType = reportResult.getType();
							ChildJsonMessage jsonMessage = new JsonMessage(F.main(
									prefix,
									"Your report against " + F.elem(suspectName) + " was marked as " + F.elem(resultType.getName()) + "."))
									.extra("\n");

							if (resultType == ReportResultType.ABUSIVE)
							{
								jsonMessage = jsonMessage.add(F.main(prefix, C.cRed + "Submitting false reports may result in punishment."))
										.add("\n");
							}

							jsonMessage = jsonMessage.add(F.main(prefix, "Reason: " + F.elem(reason)));
							new ReportersNotification(ids, jsonMessage).publish();
							_reportRepository.clearCache(reportId);
						});
					});
				}
				catch (Throwable throwable)
				{
					_plugin.getLogger().log(Level.SEVERE, "Post-report save failed.", throwable);
				}
			});
		});
	}

	/**
	 * Checks if the account should be banned from using the report system.
	 * If so, a ban will be applied.
	 *
	 * @param reporter the player who should be banned if threshold is met
	 * @param handler the handler of the report
	 */
	private void checkAbuseBan(CoreClient reporter, Player handler, long reportId)
	{
		String clientName = reporter.getName();
		PunishClient punishClient = _punish.GetClient(clientName);
		boolean reportBanned = punishClient.IsReportBanned();

		if (!reportBanned)
		{
			_reportRepository.getResultCount(reporter.getAccountId(), ReportResultType.ABUSIVE).thenAccept(abuseCount ->
			{
				if (abuseCount >= ABUSE_BAN_THRESHOLD)
				{
					_punish.AddPunishment(clientName, Category.ReportAbuse,
							String.format("Automatic report ban due to abuse (report #%d).", reportId),
							handler, 3, false, -1);
				}
			});
		}
	}

	/**
	 * Marks a report as expired.
	 *
	 * This essentially closes the report with the result {@link ReportResultType#EXPIRED}
	 * and no string reason.
	 *
	 * A report should be marked as expired when it has a priority <= 0 and is not
	 * currently being handled.
	 *
	 * This is done to keep the database clean and reduce future query times as expired
	 * reports can easily be ignored.
	 *
	 * @param report the report to mark as expired
	 * @return a future which can be used to monitor the progress of the task
	 */
	public CompletableFuture<Void> expireReport(Report report)
	{
		return closeReport(report, null, new ReportResult(ReportResultType.EXPIRED, null));
	}

	/**
	 * Writes all report data to the database.
	 *
	 * @param report the report to write to the database
	 * @return future which when complete, returns the reports id
	 */
	public CompletableFuture<Long> saveReport(Report report)
	{
		CompletableFuture<Long> future = _reportRepository.updateReport(report);

		if (report.getCategory() == ReportCategory.CHAT_ABUSE)
		{
			future.thenAccept(reportId ->
			{
				PushSnapshotsCommand command = new PushSnapshotsCommand(report.getSuspectId(), reportId, report.getReporterIds());
				command.publish();
			});
		}

		return future;
	}

	/**
	 * Aborts a report.
	 * Cancels handler messaging task, stores handler as having aborted handling of report.
	 *
	 * @param report the report of which to be aborted
	 * @return a future which completes once the database has been updated
	 */
	public CompletableFuture<Void> abortReport(Report report)
	{
		long reportId = report.getId().orElseThrow(() -> new IllegalStateException("Report id must be present."));
		int handlerId = report.getHandlerId().orElseThrow(() -> new IllegalStateException("Handler must be assigned to report."));
		report.cancelHandlerTask();
		report.setHandlerId(null);
		return _reportRepository.setAborted(reportId, handlerId, true);
	}

	/**
	 * Assigns a team to a report.
	 *
	 * @param report the report to assign to a team
	 * @param team the team to assign the report to
	 * @return a future which completes once the database has been updated
	 */
	public CompletableFuture<Void> assignTeam(Report report, ReportTeam team)
	{
		report.setAssignedTeam(team);
		return abortReport(report).thenCompose(voidValue -> saveReport(report)).thenApply(reportId -> null); // convert to Void
	}

	/**
	 * Checks if the player is allowed to create reports (ie not banned).
	 *
	 * @param player the player to check if they can report
	 * @return true if the player can report, false if banned
	 */
	public boolean canReport(Player player)
	{
		return !_punish.GetClient(player.getName()).IsReportBanned();
	}

	/**
	 * Checks if the supplied report id is active.
	 *
	 * @param reportId the id of the report to check if active
	 * @return true if the report is active, false otherwise
	 */
	public CompletableFuture<Boolean> isActiveReport(long reportId)
	{
		return getRepository().getReport(reportId).thenCompose(reportOptional ->
		{
			if (reportOptional.isPresent())
			{
				return isActiveReport(reportOptional.get());
			}
			else
			{
				return CompletableFuture.completedFuture(false);
			}
		});
	}

	/**
	 * Checks if the supplied report is active.
	 *
	 * @param report the report to check if active
	 * @return true if the report is active, false otherwise
	 */
	public CompletableFuture<Boolean> isActiveReport(Report report)
	{
		if (report.getResult().isPresent())
		{
			return CompletableFuture.completedFuture(false);
		}
		else if (report.getHandlerId().isPresent())
		{
			return CompletableFuture.completedFuture(true);
		}
		else
		{
			return calculatePriority(report).thenApply(priority ->
			{
				if (priority < 1)
				{
					// mark the report as expired to keep the database clean
					// and reduce future query time
					expireReport(report);
					return false;
				}
				else
				{
					return true;
				}
			});
		}
	}

	/**
	 * Checks if the supplied player is currently handling a report.
	 *
	 * @param player the player to check if currently handling a report
	 * @return true if the player is currently handling a report, false otherwise
	 */
	public CompletableFuture<Boolean> isHandlingReport(Player player)
	{
		return isHandlingReport(_clientManager.getAccountId(player));
	}

	/**
	 * Checks if the supplied account is currently handling a report.
	 *
	 * @param accountId the account id of which to check if currently handling a report
	 * @return true if the account is currently handling a report, false otherwise
	 */
	public CompletableFuture<Boolean> isHandlingReport(int accountId)
	{
		return _reportRepository.getReportsHandling(accountId)
				.thenApply(reportIds -> reportIds.size() > 0)
				.exceptionally(throwable -> true);
		// ^ if for some reason we cannot fetch the report a user is handling
		// assume the worst and return true
		// this means we do not end up allowing a user to handle multiple reports simultaneously
	}

	/**
	 * Gets the report (if any) that the supplied player is currently handling.
	 *
	 * @param player the player
	 * @return the report
	 */
	public CompletableFuture<Optional<Report>> getReportHandling(Player player)
	{
		return getReportHandling(_clientManager.getAccountId(player));
	}

	/**
	 * Gets the report (if any) that the supplied account is currently handling.
	 *
	 * @param accountId the account
	 * @return the report
	 */
	public CompletableFuture<Optional<Report>> getReportHandling(int accountId)
	{
		CompletableFuture<Optional<Report>> future = _reportRepository.getReportsHandling(accountId).thenApply(reportIds ->
				reportIds.stream().map(_reportRepository::getReport).collect(Collectors.toList())
		).thenCompose(UtilFuture::sequence)
				.thenApply(UtilCollections::unboxPresent)
				.thenApply(reports ->
				{
					int size = reports.size();

					if (size == 0)
					{
						return Optional.empty();
					}
					else if (size == 1)
					{
						return Optional.of(reports.get(0));
					}
					else
					{
						throw new IllegalStateException("Account is handling multiple reports.");
					}
				});

		future.exceptionally(throwable ->
		{
			_plugin.getLogger().log(Level.SEVERE, "Error whilst fetching report being handled by: " + accountId + ".");
			return Optional.empty();
		});

		return future;
	}

	public CompletableFuture<List<Report>> getOpenReports(int reporterId)
	{
		return _reportRepository.getOpenReports(reporterId)
				.thenApply(reportIds ->
						reportIds.stream().map(_reportRepository::getReport).collect(Collectors.toList()))
				.thenCompose(UtilFuture::sequence)
				.thenApply(UtilCollections::unboxPresent)
				.thenCompose(reports -> UtilFuture.filter(reports, this::isActiveReport));
	}

	/**
	 * Calculates the priority of a report.
	 * This takes many parameters into account including:
	 * <ul>
	 *     <li>The reputation of the reporter(s) (for the particular category of the report)</li>
	 *     <li>The weight of each server that a report originated from</li>
	 *     <li>The age of each report</li>
	 * </ul>
	 *
	 * @param report the report to get the reputation of
	 * @return the reputation, the higher the more importance the report should be given
	 */
	public CompletableFuture<Double> calculatePriority(Report report)
	{
		CompletableFuture<Double> future = CompletableFuture.supplyAsync(() ->
		{
			double priority = 0;

			for (Map.Entry<Integer, ReportMessage> entry : report.getMessages().entrySet())
			{
				int accountId = entry.getKey();
				ReportUser user = _userRepository.getUser(accountId).join();
				int categoryReputation = user.getReputation(report.getCategory());

				ReportMessage message = entry.getValue();
				int serverWeight = message.getServerWeight();
				int initialPriority = report.getAssignedTeam().map(ReportTeam::getInitialPriority).orElse(INITIAL_PRIORITY);
				double ageImpact = initialPriority * Math.pow(1 - 0.066666666666667, message.getDurationSinceCreation().toMinutes());

				priority += categoryReputation * serverWeight * ageImpact;
			}

			return priority;
		});

		future.exceptionally(throwable ->
		{
			_plugin.getLogger().log(Level.SEVERE, "Error calculating report priority.", throwable);
			return 1.0;
		});

		return future;
	}

	protected void onPlayerJoin(Player player)
	{
		int playerId = _clientManager.Get(player).getAccountId();

		getReportHandling(playerId).whenComplete((reportOptional, throwable) ->
		{
			if (throwable == null)
			{
				if (reportOptional.isPresent())
				{
					Report report = reportOptional.get();

					if (!report.getHandlerTask().isPresent())
					{
						long reportId = report.getId().orElseThrow(() -> new IllegalStateException("Report id is not present."));
						new ReportHandlerTask(this, reportId).start(_plugin);
					}
				}
			}
			else
			{
				_plugin.getLogger().log(Level.SEVERE, "Error whilst checking for report being handled by " + player.getName(), throwable);
			}
		});

		_reportRepository.getOngoingReports(playerId).whenComplete((reports, throwable) ->
		{
			if (throwable == null)
			{
				for (Report report : reports)
				{
					sendHandlerNotification(report,
							F.main(getReportPrefix(report),
									String.format("%s has joined %s.", player.getName(), F.elem(_serverName))));
				}
			}
			else
			{
				_plugin.getLogger().log(Level.SEVERE, "Error whilst checking for on-going reports against " + player.getName(), throwable);
			}
		});
	}

	protected void onPlayerQuit(Player player)
	{
		int playerId = _clientManager.Get(player).getAccountId();

		getReportHandling(playerId).thenAccept(reportOptional ->
		{
			if (reportOptional.isPresent())
			{
				Report report = reportOptional.get();
				report.cancelHandlerTask();
			}
		});

		_reportRepository.getOngoingReports(playerId)
				.thenAccept(reports ->
						{
							for (Report report : reports)
							{
								sendHandlerNotification(report,
										F.main(getReportPrefix(report),
												String.format("%s has left %s.", player.getName(), F.elem(_serverName))));
							}
						}
				);

		_reportRepository.clearCacheFor(_clientManager.getAccountId(player));
	}

	protected void onSuspectLocated(long reportId, String serverName)
	{
		_reportRepository.getReport(reportId).thenAccept(reportOptional ->
		{
			if (reportOptional.isPresent())
			{
				Report report = reportOptional.get();
				Optional<Integer> handlerIdOptional = report.getHandlerId();

				if (handlerIdOptional.isPresent())
				{
					int handlerId = handlerIdOptional.get();

					_reportRepository.getAccountUUID(handlerId).thenAccept(handlerUUID ->
					{
						Player handler = Bukkit.getPlayer(handlerUUID);

						if (handler != null)
						{
							if (!_incognitoManager.Get(handler).Status)
							{
								setAccountIncognito(handlerId, true).thenAccept(voidValue ->
										Portal.transferPlayer(handler.getName(), serverName));
							}
							else
							{
								Portal.transferPlayer(handler.getName(), serverName);
							}
						}
					});
				}
			}
		});
	}

	/**
	 * Since {@link IncognitoManager#toggle(Player)} doesn't provide any sort of callback when values have been written
	 * to the database, this method works around that by calling {@link mineplex.core.incognito.repository.IncognitoRepository#setStatus(int, boolean)}
	 * directly, wrapping it with a {@link CompletableFuture}.
	 *
	 * @param accountId the id of the account to set incognito status for
	 * @param status the status value to set
	 * @return a future
	 */
	private CompletableFuture<Void> setAccountIncognito(int accountId, boolean status)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			_incognitoManager.getRepository().setStatus(accountId, status);
			return null;
		});
	}

	/**
	 * Send to the handler of a {@link Report}, regardless of whether or not the handler is currently on this server instance.
	 * If there is no handler for a report, it will be sent to all staff instead.
	 *
	 * @param report      the report of which a message should be sent ot it's handler
	 * @param jsonMessage the report notification message to send
	 */
	public void sendHandlerNotification(Report report, JsonMessage jsonMessage)
	{
		if (report.getHandlerId().isPresent())
		{
			HandlerNotification handlerNotification = new HandlerNotification(report, jsonMessage);
			handlerNotification.publish();
		}
	}

	/**
	 * Send to the handler of a {@link Report}, regardless of whether or not the handler is currently on this server instance.
	 * If there is no handler for a report, it will be sent to all staff instead.
	 *
	 * @param report  the report of which a message should be sent ot it's handler
	 * @param message the report notification message to send
	 */
	public void sendHandlerNotification(Report report, String message)
	{
		sendHandlerNotification(report, new JsonMessage(message));
	}

	/**
	 * Maps a collection of account ids to Mojang UUIDs.
	 *
	 * @param accountIds the account ids
	 * @return the UUIDs
	 */
	public CompletableFuture<Set<UUID>> getUUIDs(Collection<Integer> accountIds)
	{
		List<CompletableFuture<UUID>> futures = accountIds.stream()
				.map(_reportRepository::getAccountUUID)
				.collect(Collectors.toList());

		return UtilFuture.sequence(futures, Collectors.toSet());
	}

	private static final UUID IKEIRNEZ_UUID = UUID.fromString("e54554ed-678e-420d-a2b5-09112ce6f244");

	/**
	 * Allows the user to bypass certain restrictions throughout the report feature.
	 * This function will only return true on a test/dev server and with the correct player UUID.
	 *
	 * Test users can bypass the following restrictions:
	 *
	 * <ul>
	 * <li>Being unable to report yourself</li>
	 * <li>Being unable to handle reports in which you are a suspect</li>
	 * <li>Being unable to handle reports in which you are a reporter</li>
	 * </ul>
	 *
	 * @param uuid the uuid to check if is test user
	 * @return returns true if test/dev server and if the uuid is that of a test user, otherwise false
	 */
	public boolean isDevMode(UUID uuid)
	{
		return _serverName.startsWith("IKEIRNEZ-TEST") && uuid.equals(IKEIRNEZ_UUID);
	}

	/* STATIC HELPERS */

	/**
	 * Gets the prefix which should be used for displaying messages related to the supplied report.
	 *
	 * @param report the report to get the prefix for
	 * @return the prefix for the supplied report
	 */
	public static String getReportPrefix(Report report)
	{
		long reportId = report.getId().orElseThrow(() ->
				new IllegalStateException("Report has not yet been assigned an id."));

		return getReportPrefix(reportId);
	}

	/**
	 * Gets the prefix which should be used for displaying messages related to the supplied report.
	 *
	 * @param reportId the report id to get the prefix for
	 * @return the prefix for the supplied report
	 */
	public static String getReportPrefix(long reportId)
	{
		return NAME + " #" + reportId;
	}
	
}
