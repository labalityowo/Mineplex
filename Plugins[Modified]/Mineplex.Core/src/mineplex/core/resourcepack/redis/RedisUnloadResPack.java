package mineplex.core.resourcepack.redis;

import mineplex.serverdata.commands.ServerCommand;

public class RedisUnloadResPack extends ServerCommand
{
	private final String _player;

	public RedisUnloadResPack(String player)
	{
		_player = player;
	}

	public String getPlayer()
	{
		return _player;
	}
}