package nautilus.game.arcade.game.games.mineware.challenge;

/**
 * The ending condition of a challenge.
 */
public enum ChallengeType
{
	LastStanding("Last Standing"),
	FirstComplete("First Complete");

	private String _name;

	ChallengeType(String name)
	{
		_name = name;
	}

	@Override
	public String toString()
	{
		return _name;
	}
}
