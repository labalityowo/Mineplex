package mineplex.core.thank;

/**
 * @author Shaun Bennett
 */
public class ClaimThankResult
{
	private final int _claimed;
	private final int _uniqueThanks;

	public ClaimThankResult(int claimed, int uniqueThanks)
	{
		_claimed = claimed;
		_uniqueThanks = uniqueThanks;
	}

	public int getClaimed()
	{
		return _claimed;
	}

	public int getUniqueThanks()
	{
		return _uniqueThanks;
	}
}
