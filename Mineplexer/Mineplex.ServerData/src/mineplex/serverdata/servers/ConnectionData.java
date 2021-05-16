package mineplex.serverdata.servers;

/**
 * ConnectionData stores information relevant for initiating a connection to a repository.
 * @author MrTwiggy
 *
 */
public class ConnectionData 
{
	
	public enum ConnectionType
	{
		MASTER,
		SLAVE;
	}
	
	private ConnectionType _type;	// The type of connection available
	public ConnectionType getType() { return _type; }
	
	private String _name;			// The name associated with this connection
	public String getName() { return _name; }

	private String _host;			// The host URL to connect to repository
	public String getHost() { return _host; }
	
	private int _port;				// The port to connect to repository
	public int getPort() { return _port; }
	
	/**
	 * Constructor
	 * @param host - the host URL defining the repository
	 * @param port - the port used for connection to repository
	 * @param type - the type of connection referenced by this ConnectionData
	 * @param name - the name associated with ConnectionData
	 */
	public ConnectionData(String host, int port, ConnectionType type, String name)
	{
		_host = host;
		_port = port;
		_type = type;
		_name = name;
	}
	
	/**
	 * @param name
	 * @return true, if {@code name} is null or it matches (case-insensitive) the {@code _name} associated
	 * with this ConnectionData, false otherwise.
	 */
	public boolean nameMatches(String name)
	{
		return (name == null || name.equalsIgnoreCase(_name));
	}
}

