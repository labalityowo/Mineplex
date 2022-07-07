package mineplex.serverdata.commands;

import java.util.UUID;

public class PlayerJoinCommand extends ServerCommand
{
	private String _uuid;
	private String _name;
	
	public PlayerJoinCommand(UUID uuid, String name)
	{
		_uuid = uuid.toString();
		_name = name;
	}
	
	@Override
	public void run() 
	{
		// Utilitizes a callback functionality to seperate dependencies
	}

	public String getUuid() 
	{
		return _uuid;
	}
	
	public String getName()
	{
		return _name;
	}
}