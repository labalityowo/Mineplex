package mineplex.core.game.rejoin;

import mineplex.serverdata.data.Data;

public class GameRejoinData implements Data
{

	private final int _accountId, _gameId;
	private final String _server;

	GameRejoinData(int accountId, int gameId, String server)
	{
		_accountId = accountId;
		_gameId = gameId;
		_server = server;
	}

	public int getGameId()
	{
		return _gameId;
	}

	public String getServer()
	{
		return _server;
	}

	@Override
	public String getDataId()
	{
		return String.valueOf(_accountId);
	}
}
