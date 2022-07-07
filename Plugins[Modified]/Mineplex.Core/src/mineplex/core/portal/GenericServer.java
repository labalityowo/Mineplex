package mineplex.core.portal;

/**
 * Groups of servers with no specific id
 */
public enum GenericServer
{
	/**
	 * The Hubs, such as Lobby-1
	 */
	HUB("Lobby"),
	/**
	 * The Clans Hubs, such as ClansHub-1
	 */
	CLANS_HUB("ClansHub"),
	/**
	 * The Beta Hubs, such as BetaHub-1
	 */
	BETA_HUB("BetaHub"),
	;

	private final String _name;

	GenericServer(String name)
	{
		_name = name;
	}

	public String getName()
	{
		return _name;
	}
}