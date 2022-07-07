package mineplex.core.report.data.metrics;

/**
 * Extends the standard report metrics class to hold global-scope metrics.
 */
public class ReportGlobalMetrics extends ReportMetrics
{
	private final long _submitted;
	private final long _expired;

	public ReportGlobalMetrics(long submitted, long expired, long accepted, long denied, long flagged)
	{
		super(accepted, denied, flagged);
		_submitted = submitted;
		_expired = expired;
	}

	/**
	 * Gets the amount of reports submitted.
	 *
	 * @return the amount
	 */
	public long getSubmitted()
	{
		return _submitted;
	}

	/**
	 * Gets the amount of reports expired.
	 *
	 * @return the amount
	 */
	public long getExpired()
	{
		return _expired;
	}
}
