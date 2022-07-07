package mineplex.core.common.util;

import java.util.UUID;

public class PlayerInfo
{
	private String _name;
	private UUID _uuid;

	public PlayerInfo(String name, UUID uuid)
	{
		_name = name;
		_uuid = uuid;
	}

	public String getName()
	{
		return _name;
	}

	public UUID getUUID()
	{
		return _uuid;
	}

}
