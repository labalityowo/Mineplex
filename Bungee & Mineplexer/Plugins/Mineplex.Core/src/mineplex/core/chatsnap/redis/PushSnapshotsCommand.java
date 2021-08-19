package mineplex.core.chatsnap.redis;

import java.util.Set;

import mineplex.serverdata.commands.ServerCommand;

/**
 * This command when executed will get all snapshots involving a particular account and push them to the database.
 */
public class PushSnapshotsCommand extends ServerCommand
{
	private final int _accountId;
	private final long _reportId;
	private final Set<Integer> _reporters;

	public PushSnapshotsCommand(int accountId, long reportId, Set<Integer> reporters)
	{
		super();
		_accountId = accountId;
		_reportId = reportId;
		_reporters = reporters;
	}

	public int getAccountId()
	{
		return _accountId;
	}

	public long getReportId()
	{
		return _reportId;
	}

	public Set<Integer> getReporters()
	{
		return _reporters;
	}

	@Override
	public void run()
	{
		// Utilitizes a callback functionality to seperate dependencies
	}
}
