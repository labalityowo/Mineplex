package mineplex.core.report.data;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores data about a single player report
 *
 * One or more of these make-up a {@link Report}.
 */
public class ReportMessage
{
	private int _reporterId;
	private String _message;
	private String _server;
	private int _serverWeight;
	private LocalDateTime _time;

	public ReportMessage(int reporterId, String message, String server, int serverWeight)
	{
		this(reporterId, message, server, serverWeight, LocalDateTime.now());
	}

	public ReportMessage(int reporterId, String message, String server, int serverWeight, LocalDateTime time)
	{
		checkNotNull(message);
		checkNotNull(server);
		checkNotNull(time);

		_reporterId = reporterId;
		_message = message;
		_server = server;
		_serverWeight = serverWeight;
		_time = time;
	}

	public int getReporterId()
	{
		return _reporterId;
	}

	public String getMessage()
	{
		return _message;
	}

	public void setMessage(String message)
	{
		_message = message;
	}

	public String getServer()
	{
		return _server;
	}

	public int getServerWeight()
	{
		return _serverWeight;
	}

	public LocalDateTime getTimeCreated()
	{
		return _time;
	}

	public Duration getDurationSinceCreation()
	{
		return Duration.between(_time, LocalDateTime.now());
	}
}