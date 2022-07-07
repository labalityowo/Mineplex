package mineplex.core.rankGiveaway.redis;

import mineplex.serverdata.commands.ServerCommand;

public class TitanGiveawayMessage extends ServerCommand
{
	private String _playerName;
	private int _titanCount;

	public TitanGiveawayMessage(String playerName, int titanCount)
	{
		_playerName = playerName;
		_titanCount = titanCount;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	public int getTitanCount()
	{
		return _titanCount;
	}

	@Override
	public void run()
	{
		// Handled in Command Callback
	}
}

