package mineplex.core.slack;

/**
 * An enumeration of Mineplex Slack teams.
 */
public enum SlackTeam
{
	// Dev team - mineplex.slack.com
	DEVELOPER("Mineplex Dev", "T045RUM7F", "B3165UYN4", "lV5GESG9FDIHG3mQ0LlOGeNV"),

	// QA team - mineplexqa.slack.com
	QA("Mineplex QA", "todo", "todo", "todo"), // TODO: new details
	
	CLANS("Mineplex CM", "T2ADQ1G9L", "B6URH6CM9", "HoF2tRaspjMBaj63luJWnlJz"),
	
	SOCIAL_MEDIA("Mineplex SM", "T44BS70DB", "B6UCH5Y1X", "NciHvU15gbRMZ1PksCjXy3my"),

	;

	private String _title;
	private String _id1;
	private String _id2;
	private String _token;

	SlackTeam(String title, String id1, String id2, String token)
	{
		_title = title;
		_id1 = id1;
		_id2 = id2;
		_token = token;
	}

	/**
	 * Gets the title that will be displayed that the top of each
	 * {@link SlackMessage}.
	 *
	 * @return The title of this team.
	 */
	public String getTitle()
	{
		return _title;
	}

	/**
	 * Gets the first ID of this Slack team.
	 *
	 * @return The individual first ID.
	 */
	public String getId1()
	{
		return _id1;
	}

	/**
	 * Gets the second ID of this Slack team.
	 *
	 * @return The individual second ID.
	 */
	public String getId2()
	{
		return _id2;
	}

	/**
	 * Gets the token key of this Slack team.
	 *
	 * @return The individual and <b>secret</b> token.
	 */
	public String getToken()
	{
		return _token;
	}

	/**
	 * Gets the web hook in the form of a URL.
	 *
	 * @return The URL as a string.
 	 */
	public String getURL()
	{
		return "https://hooks.slack.com/services/" + getId1() + "/" + getId2() + "/" + getToken();
	}
}