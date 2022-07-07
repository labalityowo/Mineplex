package mineplex.core.rankGiveaway.redis;

import mineplex.serverdata.commands.ServerCommand;

public class EternalGiveawayMessage extends ServerCommand
{
	private String _playerName;
	private int _eternalCount;

	public EternalGiveawayMessage(String playerName, int eternalCount)
	{
		_playerName = playerName;
		_eternalCount = eternalCount;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	public int getEternalCount()
	{
		return _eternalCount;
	}

	@Override
	public void run()
	{
		// Handled in Command Callback
	}
}

