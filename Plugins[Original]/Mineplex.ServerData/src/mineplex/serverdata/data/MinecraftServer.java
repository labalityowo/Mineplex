package mineplex.serverdata.data;

public class MinecraftServer 
{
	// The name of this server.
	private String _name;
	public String getName() { return _name; }

	// The ServerGroup that this MinecraftServer belongs to.
	private String _group;
	public String getGroup() { return _group; }
	
	// The current message of the day (MOTD) of the server.
	private String _motd;
	public String getMotd() { return _motd; }
	
	// The number of players currently online.
	private int _playerCount;
	public int getPlayerCount() { return _playerCount; }
	public void incrementPlayerCount(int amount) { this._playerCount += amount; }
	
	// The maximum number of players allowed on the server.
	private int _maxPlayerCount;
	public int getMaxPlayerCount() { return _maxPlayerCount; }
	
	// The ticks per second (TPS) of the server.
	private int _tps;
	public int getTps() { return _tps; }
	
	// The current amount of RAM allocated to the server.
	private int _ram;
	public int getRam() { return _ram; }
	
	// The maximum amount of available RAM that can be allocated to the server.
	private int _maxRam;
	public int getMaxRam() { return _maxRam; }
	
	// The public I.P address used by players to connect to the server.
	private String _publicAddress;
	public String getPublicAddress() { return _publicAddress; }
	
	// The port the server is currently running/listening on.
	private int _port;
	public int getPort() { return _port; }
	
	private int _donorsOnline;
	public int getDonorsOnline() { return _donorsOnline; }
	
	private long _startUpDate;

	private long _currentTime;
	public long getCurrentTime()
	{
		return this._currentTime;
	}
	
	/**
	 * Class constructor
	 * @param name
	 * @param group
	 * @param motd
	 * @param publicAddress
	 * @param port
	 * @param playerCount
	 * @param maxPlayerCount
	 * @param tps
	 * @param ram
	 * @param maxRam
	 */
	public MinecraftServer(String name, String group, String motd, String publicAddress, int port,
							int playerCount, int maxPlayerCount, int tps, int ram, int maxRam, long startUpDate, int donorsOnline)
	{
		_name = name;
		_group = group;
		_motd = motd;
		_playerCount = playerCount;
		_maxPlayerCount = maxPlayerCount;
		_tps = tps;
		_ram = ram;
		_maxRam = maxRam;
		_publicAddress = publicAddress;
		_port = port;
		_donorsOnline = donorsOnline;
		_startUpDate = startUpDate;
		_currentTime = System.currentTimeMillis();
	}
	
	/**
	 * @return true, if {@value _playerCount} equals 0, false otherwise.
	 */
	public boolean isEmpty()
	{
		return _playerCount == 0;
	}
	
	/**
	 * @return the amount of time (in seconds) that this {@link MinecraftServer} has been online for.
	 */
	public double getUptime()
	{
		return (System.currentTimeMillis() / 1000d - _startUpDate);
	}
	
	/**
	 * @return true, if this server is currently joinable by players, false otherwise.
	 */
	public boolean isJoinable()
	{
		if (_motd == null)
		{
			return false;
		}

		// This is super dodgy, this is the only way around monitor not killing game servers with the new MOTD system
		if (_motd.isEmpty() || _motd.contains("VOTING") || _motd.contains("STARTING") || _motd.contains("WAITING") || _motd.contains("ALWAYS_OPEN"))
		{
			if (_playerCount < _maxPlayerCount)
			{
				int availableSlots = _maxPlayerCount - _playerCount;

				return !_motd.isEmpty() || (availableSlots > 20);
			}
		}
		return false;
	}
	
	public void setGroup(String group)
	{
		_group = group;
	}
	public void setName(String name)
	{
		_name = name;
	}
}
