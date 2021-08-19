package mineplex.core.report;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.chatsnap.SnapshotMetadata;
import mineplex.core.chatsnap.SnapshotRepository;
import mineplex.core.common.jsonchat.ChildJsonMessage;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.report.data.Report;
import mineplex.core.report.data.ReportMessage;

/**
 * Displays a message containing up-to-date details of a report to it's handler.
 */
public class ReportHandlerTask extends BukkitRunnable
{
	private final ReportManager _reportManager;
	private final long _reportId;

	public ReportHandlerTask(ReportManager reportManager, long reportId)
	{
		_reportManager = reportManager;
		_reportId = reportId;
	}

	private CompletableFuture<Optional<Report>> getReport()
	{
		return _reportManager.getRepository().getReport(_reportId);
	}

	public void start(JavaPlugin plugin)
	{
		runTaskTimer(plugin, 1L, 20L * 10);

		getReport().thenAccept(reportOptional ->
		{
			if (reportOptional.isPresent())
			{
				Report report = reportOptional.get();
				report.cancelHandlerTask();
				report.setHandlerTask(this);
			}
		});
	}

	@Override
	public void run()
	{
		getReport().thenAccept(reportOptional ->
		{
			if (reportOptional.isPresent())
			{
				Report report = reportOptional.get();
				long reportId = report.getId().orElse((long) -1);

				_reportManager.isActiveReport(report).thenAccept(isActive ->
				{
					if (isActive)
					{
						_reportManager.getRepository().getAccountName(report.getSuspectId())
								.thenAccept(suspectName ->
								{
									String prefix = F.main(ReportManager.getReportPrefix(reportId), "");

									ChildJsonMessage jsonMessage = new JsonMessage("\n")
											.extra(prefix + C.cAqua + "Report Overview")
											.add("\n")
											.add(prefix + C.cAqua + "Suspect - " + C.cGold + suspectName)
											.add("\n")
											.add(prefix + C.cAqua + "Type - " + C.cGold + report.getCategory().getName())
											.add("\n")
											.add(prefix + C.cAqua + "Team - " + C.cGold + report.getAssignedTeam().map(ReportTeam::getName).orElse("None"))
											.add("\n" + prefix + "\n")
											.add(prefix + C.cGold + report.getMessages().size() + C.cAqua + " total reports")
											.add("\n")
											.add(Arrays.stream(getReportReasons(report)).map(s -> prefix + s).collect(Collectors.joining("\n")))
											.add("\n" + prefix + "\n");

									Optional<SnapshotMetadata> snapshotMetadataOptional = report.getSnapshotMetadata();

									if (snapshotMetadataOptional.isPresent())
									{
										SnapshotMetadata snapshotMetadata = snapshotMetadataOptional.get();
										Optional<String> tokenOptional = snapshotMetadata.getToken();

										if (tokenOptional.isPresent())
										{
											String token = tokenOptional.get();

											jsonMessage = jsonMessage
													.add(prefix + C.cAqua + "View chat log")
													.hover(HoverEvent.SHOW_TEXT, C.cGray + "Opens the chat log in your default browser")
													.click(ClickEvent.OPEN_URL, SnapshotRepository.getURL(token))
													.add("\n");
										}
									}

									jsonMessage = jsonMessage
											.add(prefix + C.cAqua + "Close this report")
											.hover(HoverEvent.SHOW_TEXT, C.cGray + "Usage: /reportclose <reason>")
											.click(ClickEvent.SUGGEST_COMMAND, "/reportclose ")
											.add("\n");

									Optional<Integer> handlerIdOptional = report.getHandlerId();

									if (handlerIdOptional.isPresent())
									{
										int handlerId = handlerIdOptional.get();
										JsonMessage finalJsonMessage = jsonMessage;

										_reportManager.getRepository().getAccountUUID(handlerId).thenAccept(handlerUUID ->
										{
											if (handlerUUID != null)
											{
												Player handler = Bukkit.getPlayer(handlerUUID);

												if (handler != null)
												{
													finalJsonMessage.sendToPlayer(handler);
												}
												else
												{
													// handler offline
													cancel();
												}
											}
										});
									}
									else
									{
										// no handler (report perhaps aborted), so cancel task
										cancel();
									}
								});
					}
					else
					{
						// report has been closed, so this task should be cancelled
						cancel();
					}
				});
			}
		});
	}

	private String[] getReportReasons(Report report)
	{
		Collection<ReportMessage> reportMessages = report.getMessages().values();
		String[] output = new String[reportMessages.size()];
		int count = 0;

		for (ReportMessage reportMessage : reportMessages)
		{
			// this is blocking, but that's okay as it will only be called asynchronously
			String reporterName = _reportManager.getRepository().getAccountName(reportMessage.getReporterId()).join();

			// triple backslashes so this translates to valid JSON
			output[count++] = String.format("%4$s(%d%4$s) %s%s%s - \\\"%s%s%4$s\\\"", count, C.cGold, reporterName, C.cGray, C.cPurple, reportMessage.getMessage());
		}

		return output;
	}
}
