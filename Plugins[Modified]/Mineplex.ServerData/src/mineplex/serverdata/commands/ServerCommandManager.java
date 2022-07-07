package mineplex.serverdata.commands;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import mineplex.serverdata.Utility;
import mineplex.serverdata.servers.ServerManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class ServerCommandManager 
{

	// The singleton instance of ServerCommandManager
	private static ServerCommandManager _instance;
	
	public final String SERVER_COMMANDS_CHANNEL = "commands.server";
	
	private JedisPool _writePool;
	private JedisPool _readPool;
	private Map<String, CommandType> _commandTypes;
	
	private String _localServerName;
	private Gson _gson;
	public void initializeServer(String serverName, Gson gson)
	{
		_localServerName = serverName;
		_gson = gson;
	}

	public boolean isServerInitialized() { return _localServerName != null; }
	public String getServerName()
	{
		return _localServerName;
	}
	
	/**
	 * Private class constructor to prevent non-singleton instances.
	 */
	private ServerCommandManager()
	{
		_writePool = Utility.generatePool(ServerManager.getMasterConnection());	// Publish to master instance
		_readPool = Utility.generatePool(ServerManager.getSlaveConnection());	// Read from slave instance
		
		_commandTypes = new HashMap<>();
		
		initialize();
	}
	
	/**
	 * Initialize the ServerCommandManager by subscribing to the
	 * redis network.
	 */
	private void initialize()
	{
		// Spin up a new thread and subscribe to the Redis pubsub network
		Thread thread = new Thread("Redis Manager")
		{
			public void run()
			{
				try (Jedis jedis = _readPool.getResource())
				{
					jedis.psubscribe(new ServerCommandListener(), SERVER_COMMANDS_CHANNEL + ":*");
				}
			}
		};
		
		thread.start();
	}
	
	/**
	 * Publish a {@link ServerCommand} across the network to all live servers.
	 * @param serverCommand - the {@link ServerCommand} to issue to all servers.
	 */
	public void publishCommand(final ServerCommand serverCommand)
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				String commandType = serverCommand.getClass().getSimpleName();
				String serializedCommand = _gson.toJson(serverCommand);
				
				try(Jedis jedis = _writePool.getResource())
				{
					jedis.publish(SERVER_COMMANDS_CHANNEL + ":" + commandType, serializedCommand);
				}
			}
		}).start();
	}
	
	/**
	 * Handle an incoming (serialized) {@link ServerCommand}.
	 * @param commandType - the type of command being received
	 * @param serializedCommand - the serialized {@link ServerCommand} data.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void handleCommand(final String commandType, String serializedCommand)
	{
		if (!isServerInitialized())
		{
			// TODO: Log un-initialized server receiving command?
			return;
		}
		
		if (_commandTypes.containsKey(commandType))
		{
			Class<? extends ServerCommand> commandClazz = _commandTypes.get(commandType).getCommandType();
			final ServerCommand serverCommand = _gson.fromJson(serializedCommand, commandClazz);

			if (serverCommand.isTargetServer(_localServerName))
			{
				// TODO: Run synchronously?
				CommandCallback callback = _commandTypes.get(commandType).getCallback();
				serverCommand.run(); // Run server command without callback
	
				if (callback != null)
				{
					callback.run(serverCommand); // Run callback
				}
			}
		}
	}
	
	/**
	 * Register a new type of {@link ServerCommand}.
	 * @param commandType - the {@link ServerCommand} type to register.
	 */
	public <T extends ServerCommand> void registerCommandType(String commandName, Class<T> commandType, CommandCallback<T> callback)
	{
		if (_commandTypes.containsKey(commandName))
		{
			// Log overwriting of command type?
		}
		
		CommandType cmdType = new CommandType(commandType, callback);
		_commandTypes.put(commandName, cmdType);
		System.out.println("Registered : " + commandName);
	}

	public <T extends ServerCommand> void registerCommandType(Class<T> commandType, CommandCallback<T> callback)
	{
		registerCommandType(commandType.getSimpleName(), commandType, callback);
	}

	public void registerCommandType(String commandName, Class<? extends ServerCommand> commandType)
	{
		registerCommandType(commandName, commandType, null);
	}
	
	/**
	 * @return the singleton instance of ServerCommandManager
	 */
	public static ServerCommandManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ServerCommandManager();
		}
		
		return _instance;
	}
}