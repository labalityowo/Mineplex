package mineplex.serverdata.servers;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mineplex.serverdata.Region;
import mineplex.serverdata.redis.RedisConfig;
import mineplex.serverdata.redis.RedisServerRepository;
import mineplex.serverdata.servers.ConnectionData.ConnectionType;

/**
 * ServerManager handles the creation/management of {@link ServerRepository}s for use.
 * @author Ty
 *
 */
public class ServerManager 
{
	public static final String SERVER_STATUS_LABEL = "ServerStatus";	// Label differentiating ServerStatus related servers
	private static final String DEFAULT_CONFIG = "redis-config.dat";
	
	// Configuration determining connection information
	private static RedisConfig _config;
	
	// The cached repository instances
	private static Map<Region, ServerRepository> repositories = new HashMap<Region, ServerRepository>();
	 
	/**
	 * @param host - the host url used to connect to the database
	 * @param port - the port to connect to the repository
	 * @param region - the geographical region of the {@link ServerRepository}.
	 * @return a newly instanced (or cached) {@link ServerRepository} for the specified {@code region}.
	 */
	private static ServerRepository getServerRepository(ConnectionData writeConn, ConnectionData readConn, Region region)
	{
		if (repositories.containsKey(region)) return repositories.get(region);
		
		ServerRepository repository = new RedisServerRepository(writeConn, readConn, region);
		repositories.put(region, repository);
		return repository;
	}
	
	/**
	 * {@code host} defaults to {@value DEFAULT_REDIS_HOST} and
	 * {@code port} defaults to {@value DEFAULT_REDIS_PORT}.
	 * 
	 * @see #getServerRepository(String, int, Region)
	 */
	public static ServerRepository getServerRepository(Region region)
	{
		return getServerRepository(getConnection(true, SERVER_STATUS_LABEL), getConnection(false, SERVER_STATUS_LABEL), region);
	}
	
	/**
	 * @return the {@link ConnectionData} associated with the master instance connection.
	 */
	public static ConnectionData getMasterConnection()
	{
		return getConnection(true);
	}
	
	/**
	 * Non-Deterministic: Generates random slave instance connection.
	 * @return the {@link ConnectionData} associated with a random slave connection.
	 */
	public static ConnectionData getSlaveConnection()
	{
		return getConnection(false);
	}
	
	public static ConnectionData getConnection(boolean writeable, String name)
	{
		return getDefaultConfig().getConnection(writeable, name);
	}
	
	/**
	 * @param writeable - whether the connection referenced in return can receive write-requests
	 * @return a newly generated {@code ConnectionData} pointing to a valid connection.
	 */
	public static ConnectionData getConnection(boolean writeable)
	{
		return getConnection(writeable, "DefaultConnection");
	}

	/**
	 * @return the default {@link RedisConfig} associated with this manager, providing appropriate connections.
	 */
	public static RedisConfig getDefaultConfig()
	{
		return getConfig(DEFAULT_CONFIG);
	}

	/**
	 * @return the {@link RedisConfig} associated with this manager, providing appropriate connections.
	 */
	public static RedisConfig getConfig(String fileName)
	{
		if (_config == null)
			_config = loadConfig(fileName);

		return _config;
	}

	public static RedisConfig loadConfig(String fileName)
	{
		try
		{
			File configFile = new File(fileName);

			if (configFile.exists())
			{
				List<ConnectionData> connections = new ArrayList<ConnectionData>();
				List<String> lines = Files.readAllLines(configFile.toPath(), Charset.defaultCharset());

				for (String line : lines)
				{
					ConnectionData connection = deserializeConnection(line);
					connections.add(connection);

				}

				return new RedisConfig(connections);
			}
			else
			{
				log(fileName + " not found at " + configFile.toPath().toString());
				return new RedisConfig();
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
			log("---Unable To Parse Redis Configuration File---");
		}

		return null;
	}
	
	/**
	 * @param line - the serialized line representing a valid {@link ConnectionData} object.
	 * @return a deserialized {@link ConnectionData} referenced by the {@code line} passed in.
	 */
	private static ConnectionData deserializeConnection(String line)
	{
		String[] args = line.split(" ");
		
		if (args.length >= 2)
		{
			String ip = args[0];
			int port = Integer.parseInt(args[1]);
			String typeName = (args.length >= 3) ? args[2].toUpperCase() : "MASTER";	// Defaults to MASTER if omitted.
			ConnectionType type = ConnectionType.valueOf(typeName);						
			String name = (args.length >= 4) ? args[3] : "DefaultConnection";			// Defaults to DefaultConnection if omitted.
			
			return new ConnectionData(ip, port, type, name);
		}
		
		return null;
	}
	
	private static void log(String message)
	{
		System.out.println(String.format("[ServerManager] %s", message));
	}
}
