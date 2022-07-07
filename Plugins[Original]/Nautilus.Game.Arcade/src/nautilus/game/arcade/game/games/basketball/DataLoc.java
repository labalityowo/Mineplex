package nautilus.game.arcade.game.games.basketball;

/**
 * Enum for easy references to data location keys
 */
public enum DataLoc
{
	RED_HOOP("RED_HOOP"),
	BLUE_HOOP("BLUE_HOOP"),
	CENTER_COURT("CENTER_COURT"),
	CORNER_MIN("CORNER_MIN"),
	CORNER_MAX("CORNER_MAX"),
	RED_SCORE_SPAWN("RED_SCORE_SP"),
	RED_UNDER_HOOP("RED_UNDER"),
	BLUE_SCORE_SPAWN("BLUE_SCORE_SP"),
	BLUE_UNDER_HOOP("BLUE_UNDER")
	;
	
	private String _key;
	
	private DataLoc(String key)
	{
		_key = key.toString();
	}
	
	/**
	 * Fetches the Data Location key
	 * @return The Data Key for the point
	 */
	public String getKey()
	{
		return _key;
	}
}
