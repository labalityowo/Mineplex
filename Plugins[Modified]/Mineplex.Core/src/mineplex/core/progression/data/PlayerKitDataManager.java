package mineplex.core.progression.data;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.UUID;

/**
 * Manages all PlayerKit data in memory
 */
public class PlayerKitDataManager
{

	private Map<UUID, PlayerKit> _dataMapAccountUUID;

	public PlayerKitDataManager()
	{
		_dataMapAccountUUID = Maps.newConcurrentMap();
	}

	public PlayerKit get(UUID uuid)
	{
		return _dataMapAccountUUID.get(uuid);
	}

	public void add(PlayerKit playerKit)
	{
		_dataMapAccountUUID.put(playerKit.getUuid(), playerKit);
	}

	public void remove(PlayerKit kit)
	{
		_dataMapAccountUUID.remove(kit.getUuid());
	}

	public Map<UUID, PlayerKit> getAll()
	{
		return _dataMapAccountUUID;
	}
}
