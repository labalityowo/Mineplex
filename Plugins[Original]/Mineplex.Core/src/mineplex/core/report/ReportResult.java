package mineplex.core.report;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Stores data about the result of a report.
 */
public class ReportResult
{
	private ReportResultType _resultType;
	private String _reason;
	private LocalDateTime _closedTime;

	public ReportResult(ReportResultType resultType, String reason)
	{
		this(resultType, reason, LocalDateTime.now());
	}

	public ReportResult(ReportResultType resultType, String reason, LocalDateTime closedTime)
	{
		_resultType = resultType;
		_reason = reason;
		_closedTime = closedTime;
	}

	public ReportResultType getType()
	{
		return _resultType;
	}

	public Optional<String> getReason()
	{
		return Optional.ofNullable(_reason);
	}

	public LocalDateTime getClosedTime()
	{
		return _closedTime;
	}
}
