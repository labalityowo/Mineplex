package mineplex.core.command;

/**
 * LoggedCommand
 *
 * @author xXVevzZXx
 */
public interface LoggedCommand
{
	default void execute(long time, String username, String command, String args)
	{
		LoggingServerCommand cmd = new LoggingServerCommand(time, username, command, args);
		cmd.publish();
	}
}