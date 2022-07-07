package mineplex.serverdata.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mineplex.serverdata.servers.ConnectionData;
import mineplex.serverdata.servers.ConnectionData.ConnectionType;

public class RedisConfig 
{
	// Failsafe values in case configuration is not provided
	private static final String DEFAULT_IP = "10.3.203.80";
	private static final int DEFAULT_PORT = 6379;
	private static Random random = new Random();	// Utility random
	
	// The connections managed by this configuration
	private List<ConnectionData> _connections;
	 
	/**
	 * Class constructor
	 * @param connections
	 */
	public RedisConfig(List<ConnectionData> connections)
	{
		_connections = connections;
	}
	
	/**
	 * Class constructor
	 * Produces a default-value based RedisConfig.
	 */
	public RedisConfig()
	{
		_connections = new ArrayList<ConnectionData>();
		_connections.add(new ConnectionData(DEFAULT_IP, DEFAULT_PORT, ConnectionType.MASTER, "DefaultConnection"));
	}
	
	/**
	 * {@code writeable} defaults to {@literal true}.
	 * @see #getConnection(boolean)
	 */
	public ConnectionData getConnection()
	{
		return getConnection(true, null);
	}
	
	/**
	 * @param writeable - whether the returned connection reference can receive write-requests.
	 * @return a {@link ConnectionData} referencing a valid redis-connection from this configuration.
	 */
	public ConnectionData getConnection(boolean writeable, String name)
	{
		List<ConnectionData> connections = getConnections(writeable, name);
		
		if (connections.size() > 0)
		{
			int index = random.nextInt(connections.size());
			return connections.get(index);
		}
		
		return null;
	}
	
	public List<ConnectionData> getConnections(boolean writeable, String name)
	{
		List<ConnectionData> connections = new ArrayList<ConnectionData>();
		ConnectionType type = (writeable) ? ConnectionType.MASTER : ConnectionType.SLAVE;
		
		for (ConnectionData connection : _connections)
		{
			if (connection.getType() == type && connection.nameMatches(name))
			{
				connections.add(connection);
			}
		}
		
		return connections;
	}
}
