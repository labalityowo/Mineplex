package mineplex.serverdata.data;

import mineplex.serverdata.Region;
import mineplex.serverdata.data.Data;

public class BungeeServer implements Data
{
	// The name of this server.
	private String _name;
	public String getName() { return _name; }
	
	// The geographical region of this Bungee Server.
	private Region _region;
	public Region getRegion() { return _region; }
	
	// The number of players currently online.
	private int _playerCount;
	public int getPlayerCount() { return _playerCount; }
	
	// The public I.P address used by players to connect to the server.
	private String _publicAddress;
	public String getPublicAddress() { return _publicAddress; }
	
	// The port the server is currently running/listening on.
	private int _port;
	public int getPort() { return _port; }
	
	// Whether the Bungee server can connect to the internet.
	private boolean _connected;
	public boolean isConnected() { return _connected; }
	
	/**
	 * Class constructor
	 * @param name
	 * @param publicAddress
	 * @param port
	 * @param playerCount
	 * @param connected
	 */
	public BungeeServer(String name, Region region, String publicAddress, int port, int playerCount, boolean connected)
	{
		_name = name;
		_region = region;
		_playerCount = playerCount;
		_publicAddress = publicAddress;
		_port = port;
		_connected = connected;
	}
	
	/**
	 * Unique identifying ID for this Bungee Server.
	 */
	public String getDataId()
	{
		return _name;
	}
}
