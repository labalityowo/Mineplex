package mineplex.core.antihack;

import mineplex.serverdata.commands.ServerCommand;

public class MajorViolationCommand extends ServerCommand
{
	private final String _thisServer;
	private final String _playerName;
	private final String _hackType;
	private final int _violations;
	private final String _message;
	private final boolean _strict;

	public MajorViolationCommand(String thisServer, String playerName, String hackType, int violations, String message, boolean strict)
	{
		this._thisServer = thisServer;
		this._playerName = playerName;
		this._hackType = hackType;
		this._violations = violations;
		this._message = message;
		this._strict = strict;
	}

	public String getOriginatingServer()
	{
		return _thisServer;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	public String getHackType()
	{
		return _hackType;
	}

	public int getViolations()
	{
		return _violations;
	}

	public String getMessage()
	{
		return _message;
	}

	public boolean isStrict()
	{
		return _strict;
	}
}
