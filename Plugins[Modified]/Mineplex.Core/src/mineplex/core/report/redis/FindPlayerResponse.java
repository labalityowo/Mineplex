package mineplex.core.report.redis;

import mineplex.serverdata.commands.ServerCommand;

/**
 * Sent when a {@link FindPlayer} command has been sent and the requested player is connected
 * to this server instance.
 */
public class FindPlayerResponse extends ServerCommand
{
	private long _reportId;
	private String _serverName;

	public FindPlayerResponse(FindPlayer findPlayer, String serverName)
	{
		super(findPlayer.getResponseTarget());
		_reportId = findPlayer.getReportId();
		_serverName = serverName;
	}

	public long getReportId()
	{
		return _reportId;
	}

	public String getServerName()
	{
		return _serverName;
	}
}
