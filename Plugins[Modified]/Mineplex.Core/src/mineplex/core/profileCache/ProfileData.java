package mineplex.core.profileCache;

import java.util.UUID;

import mineplex.serverdata.data.Data;

public class ProfileData implements Data
{
	private UUID _uuid;
	private String _playerName;
	private String _propertyMap;

	public ProfileData(UUID uuid, String playerName, String propertyMap)
	{
		_uuid = uuid;
		_playerName = playerName;
		_propertyMap = propertyMap;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	public String getPropertyMap()
	{
		return _propertyMap;
	}

	public UUID getUuid()
	{
		return _uuid;
	}

	@Override
	public String getDataId()
	{
		return _playerName.toLowerCase();
	}
}
