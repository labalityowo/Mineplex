package mineplex.core.antihack.redisnotifications;

import mineplex.serverdata.commands.ServerCommand;

public class GwenBanNotification extends ServerCommand
{
	private final String _serverName;
	private final String _playerName;
	private final String _playerUUID;
	private final String _playerRank;
	private final String _hackType;
	private final String _metadataId;
	private final boolean _extremePrejudice;

	public GwenBanNotification(String serverName, String playerName, String playerUUID, String playerRank, String hackType, String metadataId, boolean extremePrejudice)
	{
		_serverName = serverName;
		_playerName = playerName;
		_playerUUID = playerUUID;
		_playerRank = playerRank;
		_hackType = hackType;
		_metadataId = metadataId;
		_extremePrejudice = extremePrejudice;
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
	
	public String getPlayerRank()
	{
		return _playerRank;
	}

	public String getHackType()
	{
		return _hackType;
	}

	public String getMetadataId()
	{
		return _metadataId;
	}
	
	public boolean isExtremePrejudice()
	{
		return _extremePrejudice;
	}
}