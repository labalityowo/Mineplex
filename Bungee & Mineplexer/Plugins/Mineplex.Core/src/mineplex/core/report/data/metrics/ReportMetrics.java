package mineplex.core.report.data.metrics;

/**
 * Holds report-related metrics which can be applied to a user or global scope.
 */
public class ReportMetrics
{
	private final long _accepted;
	private final long _denied;
	private final long _flagged;

	public ReportMetrics(long accepted, long denied, long flagged)
	{
		_accepted = accepted;
		_denied = denied;
		_flagged = flagged;
	}

	/**
	 * Gets the amount of reports accepted.
	 *
	 * @return the amount
	 */
	public long getAccepted()
	{
		return _accepted;
	}

	/**
	 * Gets the amount of reports denied.
	 *
	 * @return the amount
	 */
	public long getDenied()
	{
		return _denied;
	}

	/**
	 * Gets the amount of reports flagged (marked as spam).
	 *
	 * @return the amount
	 */
	public long getFlagged()
	{
		return _flagged;
	}
}
