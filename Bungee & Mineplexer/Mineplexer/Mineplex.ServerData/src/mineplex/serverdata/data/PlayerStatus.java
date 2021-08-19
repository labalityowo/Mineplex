package mineplex.serverdata.data;

import java.util.UUID;

public class PlayerStatus implements Data
{
	// The uuid of this player.
	private UUID _uuid;
	public UUID getUUID() { return _uuid; }
	
	// The name of this player.
	private String _name;
	public String getName() { return _name; }
	
	// The current server occupied by this player.
	private String _server;
	public String getServer() { return _server; }
	
	/**
	 * Class constructor
	 * @param name
	 * @param server
	 */
	public PlayerStatus(UUID uuid, String name, String server)
	{
		_uuid = uuid;
		_name = name;
		_server = server;
	}
	
	/**
	 * Unique identifying String ID associated with this {@link PlayerStatus}.
	 */
	public String getDataId()
	{
		return _uuid.toString();
	}
}