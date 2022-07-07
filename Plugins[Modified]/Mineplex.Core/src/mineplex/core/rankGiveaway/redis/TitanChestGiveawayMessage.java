package mineplex.core.rankGiveaway.redis;

import mineplex.serverdata.commands.ServerCommand;

public class TitanChestGiveawayMessage extends ServerCommand
{
	private String _playerName;
	private String _server;

	public TitanChestGiveawayMessage(String playerName, String server)
	{
		_playerName = playerName;
		_server = server;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	public String getServer()
	{
		return _server;
	}

	@Override
	public void run()
	{
		// Handled in Command Callback
	}
}

