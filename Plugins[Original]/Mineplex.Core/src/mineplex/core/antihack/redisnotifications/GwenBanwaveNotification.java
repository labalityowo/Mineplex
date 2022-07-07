package mineplex.core.antihack.redisnotifications;

import mineplex.serverdata.commands.ServerCommand;

public class GwenBanwaveNotification extends ServerCommand
{
	private final String _serverName;
	private final String _playerName;
	private final String _playerUUID;
	private final String _playerRank;
	private final String _hackType;
	private final String _metadataId;
	private final long _timeToBan;

	public GwenBanwaveNotification(String serverName, String playerName, String playerUUID, String playerRank, String hackType, String metadataId, long timeToBan)
	{
		_serverName = serverName;
		_playerName = playerName;
		_playerUUID = playerUUID;
		_playerRank = playerRank;
		_hackType = hackType;
		_metadataId = metadataId;
		_timeToBan = timeToBan;
	}

	public String getServerName()
	{
		return _serverName;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	public String getPlayerUUID()
	{
		return _playerUUID;
	}

	public String getHackType()
	{
		return _hackType;
	}

	public String getMetadataId()
	{
		return _metadataId;
	}

	public long getTimeToBan()
	{
		return _timeToBan;
	}
}
