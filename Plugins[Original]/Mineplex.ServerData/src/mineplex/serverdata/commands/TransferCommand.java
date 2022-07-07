package mineplex.serverdata.commands;


public class TransferCommand extends ServerCommand
{
	private final String _playerName;
	private final String _targetServer;

	public TransferCommand(String playerName, String targetServer)
	{
		_playerName = playerName;
		_targetServer = targetServer;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	public String getTargetServer()
	{
		return _targetServer;
	}
}
