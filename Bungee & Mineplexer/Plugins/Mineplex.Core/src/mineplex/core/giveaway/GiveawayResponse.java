package mineplex.core.giveaway;

import java.util.UUID;

public class GiveawayResponse
{
	private boolean _success;
	private FailReason _failReason;
	private UUID _giveawayId;

	/**
	 * Success Constructor
	 * @param giveawayId
	 */
	protected GiveawayResponse(UUID giveawayId)
	{
		_success = true;
		_failReason = null;
		_giveawayId = giveawayId;
	}

	/**
	 * Faulure constructor
	 * @param failReason
	 */
	protected GiveawayResponse(FailReason failReason)
	{
		_success = false;
		_failReason = failReason;
		_giveawayId = null;
	}

	public boolean isSuccess()
	{
		return _success;
	}

	public UUID getGiveawayId()
	{
		return _giveawayId;
	}

	public FailReason getFailReason()
	{
		return _failReason;
	}

	public static enum FailReason
	{
		INVALID_GIVEAWAY, INVALID_COOLDOWN, CANNOT_GIVEAWAY, INVALID_ACCOUNT_ID, QUERY_FAILED;
	}
}
