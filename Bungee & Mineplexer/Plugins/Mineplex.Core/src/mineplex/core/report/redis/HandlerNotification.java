package mineplex.core.report.redis;

import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.report.data.Report;
import mineplex.core.report.data.ReportMessage;
import mineplex.serverdata.commands.ServerCommand;

/**
 * A message regarding a report which is sent only to the player handling the report.
 */
public class HandlerNotification extends ServerCommand
{
	private final long _reportId;
	private final int _handlerId;
	private final String _message; // in json format

	public HandlerNotification(Report report, JsonMessage jsonMessage)
	{
		super();
		_reportId = report.getId().orElseThrow(() -> new IllegalStateException("Report has no id set."));
		_handlerId = report.getHandlerId().orElseThrow(() -> new IllegalStateException("Report has no handler."));
		_message = jsonMessage.toString();
	}

	public long getReportId()
	{
		return _reportId;
	}

	public int getHandlerId()
	{
		return _handlerId;
	}

	public String getJson()
	{
		return _message;
	}
}
