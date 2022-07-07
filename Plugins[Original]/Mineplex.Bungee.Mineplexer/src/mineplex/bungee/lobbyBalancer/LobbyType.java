package mineplex.bungee.lobbyBalancer;

public enum LobbyType
{
	NORMAL("Lobby", "LOBBY-", "MainMotd"),
	CLANS("ClansHub", "CLANSHUB-", "ClansMotd"),
	BETA("BetaHub","BETAHUB-", "BetaMotd"),
	;
	private final String _connectName; // The name of the server the player is connecting to
	private final String _uppercasePrefix; // The (toUpperCase()) prefix given to servers of this lobby type
	private final String _redisMotdKey;

	LobbyType(String connectName, String uppercasePrefix, String redisMotdKey)
	{
		_connectName = connectName;
		_uppercasePrefix = uppercasePrefix;
		_redisMotdKey = redisMotdKey;
	}

	public String getConnectName()
	{
		return _connectName;
	}

	public String getUppercasePrefix()
	{
		return _uppercasePrefix;
	}

	public String getRedisMotdKey()
	{
		return _redisMotdKey;
	}
}
