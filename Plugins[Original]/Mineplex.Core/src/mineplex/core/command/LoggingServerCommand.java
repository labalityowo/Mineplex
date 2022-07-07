package mineplex.core.command;

import mineplex.serverdata.commands.ServerCommand;

/**
 * LoggingServerCommand
 *
 * @author xXVevzZXx
 */
public class LoggingServerCommand extends ServerCommand
{
	private final long _time;
	private final String _username;
	private final String _command;
	private final String _args;
	
	public LoggingServerCommand(long time, String username, String command, String args)
	{
		_time = time;
		_username = username;
		_command = command;
		_args = args;
	}
}