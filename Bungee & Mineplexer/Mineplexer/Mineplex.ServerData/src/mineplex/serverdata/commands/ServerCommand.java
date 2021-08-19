package mineplex.serverdata.commands;


import java.util.UUID;

public abstract class ServerCommand
{
	private final UUID _commandId = UUID.randomUUID();
	private final String _fromServer = ServerCommandManager.getInstance().getServerName();

	// The names of servers targetted to receive this ServerCommand.
	private String[] _targetServers;

	public ServerCommand()
	{
		_targetServers = new String[0];
	}

	public ServerCommand(String... targetServers)
	{
		_targetServers = targetServers;
	}
	
	public void setTargetServers(String... targetServers)
	{
	    _targetServers = targetServers;
	}
	
	public String[] getTargetServers()
	{
	    if (_targetServers == null)
	    {
	        _targetServers = new String[0];
	    }
	    
	    return _targetServers;
	}

	public String getFromServer()
	{
		return this._fromServer;
	}
	
	/**
	 * Run the command on it's destination target server.
	 */
	public void run()
	{
		// Not yet implemented in base
	}
	
	/**
	 * @param serverName - the name of the server to be checked for whether they are a target
	 * @return true, if {@code serverName} is one of the {@code targetServers} of this
	 * {@link ServerCommand}, false otherwise.
	 */
	public boolean isTargetServer(String serverName)
	{
		if (getTargetServers().length == 0)
			return true;
		
		for (String targetServer : getTargetServers())
		{
			if (targetServer.equalsIgnoreCase(serverName))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Publish the {@link ServerCommand} across the network to {@code targetServers}.
	 */
	public void publish()
	{
		ServerCommandManager.getInstance().publishCommand(this);
	}

	public boolean wasSentFromThisServer()
	{
		return ServerCommandManager.getInstance().getServerName().equals(_fromServer);
	}

	public boolean isSentToThisServer()
	{
		for (String targetServer : _targetServers)
		{
			if (ServerCommandManager.getInstance().getServerName().equals(targetServer))
			{
				return true;
			}
		}
		return false;
	}

	public UUID getCommandId()
	{
		return _commandId;
	}
}
