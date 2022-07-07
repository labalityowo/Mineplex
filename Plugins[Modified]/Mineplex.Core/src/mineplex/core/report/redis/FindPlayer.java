package mineplex.core.report.redis;

import java.util.UUID;

import mineplex.serverdata.commands.ServerCommand;

/**
 * A command sent to all servers for use when attempting to locate a player.
 *
 * If the server receiving this command does indeed have the requested player connected to their server then it
 * should respond with {@link FindPlayerResponse}.
 */
public class FindPlayer extends ServerCommand
{
	private long _reportId;
	private UUID _id;
	private int _accountId;
	private String _responseTarget;

	public FindPlayer(long reportId, UUID id, int accountId, String responseTarget)
	{
		_reportId = reportId;
		_id = id;
		_accountId = accountId;
		_responseTarget = responseTarget;
	}

	public long getReportId()
	{
		return _reportId;
	}

	public UUID getId()
	{
		return _id;
	}

	public int getAccountId()
	{
		return _accountId;
	}

	public String getResponseTarget()
	{
		return _responseTarget;
	}
}
