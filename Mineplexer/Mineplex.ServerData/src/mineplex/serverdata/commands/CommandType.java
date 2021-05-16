package mineplex.serverdata.commands;

public class CommandType
{
	private Class<? extends ServerCommand> _commandClazz;
	public Class<? extends ServerCommand> getCommandType() { return _commandClazz; }
	
	private CommandCallback<? extends ServerCommand> _commandCallback;
	public CommandCallback<? extends ServerCommand> getCallback() { return _commandCallback; }
	
	public CommandType(Class<? extends ServerCommand> commandClazz, CommandCallback<? extends ServerCommand> commandCallback)
	{
		_commandClazz = commandClazz;
		_commandCallback = commandCallback;
	}
}