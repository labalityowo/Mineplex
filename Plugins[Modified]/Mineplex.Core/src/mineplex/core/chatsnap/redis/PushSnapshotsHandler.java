package mineplex.core.chatsnap.redis;

import java.util.Set;
import mineplex.core.chatsnap.SnapshotMessage;
import mineplex.core.chatsnap.SnapshotManager;
import mineplex.core.report.ReportManager;
import mineplex.core.report.data.Report;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

/**
 * Handles receiving of {@link PushSnapshotsCommand} instances.
 */
public class PushSnapshotsHandler implements CommandCallback
{
	private final ReportManager _reportManager;
	private final SnapshotManager _snapshotManager;

	public PushSnapshotsHandler(ReportManager reportManager, SnapshotManager snapshotManager)
	{
		_reportManager = reportManager;
		_snapshotManager = snapshotManager;
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof PushSnapshotsCommand)
		{
			PushSnapshotsCommand pushCommand = (PushSnapshotsCommand) command;
			int accountId = pushCommand.getAccountId();
			long reportId = pushCommand.getReportId();
			Set<Integer> reporters = pushCommand.getReporters();
			Set<SnapshotMessage> messages = _snapshotManager.getMessagesInvolving(accountId, reporters);

			if (messages.size() > 0)
			{
				_reportManager.getRepository().getReport(reportId).thenAccept(reportOptional ->
				{
					if (reportOptional.isPresent())
					{
						Report report = reportOptional.get();
						_snapshotManager.saveReportSnapshot(report, messages);
					}
				});
			}
		}
	}
}
