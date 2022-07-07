package mineplex.hub.server;

import mineplex.core.game.status.GameInfo;
import mineplex.serverdata.data.MinecraftServer;

public class GameServer
{

	private MinecraftServer _server;
	private final String _prefix;
	private final int _number;
	private GameInfo _info;
	private long _lastUpdate;

	GameServer(MinecraftServer server)
	{
		_server = server;

		// Split the server name to get it's prefix and number ("CW4-5" = "CW4" & 5)
		String[] split = server.getName().split("-");

		if (split.length > 1)
		{
			_prefix = split[0];

			int number;

			try
			{
				number = Integer.parseInt(split[1]);
			}
			catch (NumberFormatException ex)
			{
				number = 0;
			}

			_number = number;
		}
		else
		{
			_prefix = "null";
			_number = 0;
		}
	}

	public void updateStatus(MinecraftServer server, GameInfo info)
	{
		_server = server;
		_info = info;
		_lastUpdate = System.currentTimeMillis();
	}

	public MinecraftServer getServer()
	{
		return _server;
	}

	public String getPrefix()
	{
		return _prefix;
	}

	public int getNumber()
	{
		return _number;
	}

	public GameInfo getInfo()
	{
		return _info;
	}

	public long getLastUpdate()
	{
		return _lastUpdate;
	}

	public boolean isDevServer()
	{
		return _number >= 777;
	}
}
