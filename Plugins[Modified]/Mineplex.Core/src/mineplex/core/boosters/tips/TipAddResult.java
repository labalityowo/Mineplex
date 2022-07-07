package mineplex.core.boosters.tips;

/**
 * @author Shaun Bennett
 */
public enum TipAddResult
{
	ALREADY_TIPPED_BOOSTER("You have already thanked this Amplifier!"),
	INVALID_ACCOUNT_ID("Uh oh, something went wrong. Try relogging"),
	UNKNOWN_ERROR("An error occurred. Try again later"),
	CANNOT_TIP_SELF("You can't thank yourself, silly!"),
	ON_COOLDOWN(null),
	SUCCESS(null);

	private String _friendlyMessage;

	TipAddResult(String friendlyMessage)
	{
		_friendlyMessage = friendlyMessage;
	}

	public String getFriendlyMessage()
	{
		return _friendlyMessage;
	}
}
