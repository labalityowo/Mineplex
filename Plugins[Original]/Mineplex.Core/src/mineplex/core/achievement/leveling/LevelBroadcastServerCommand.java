package mineplex.core.achievement.leveling;

import mineplex.serverdata.commands.ServerCommand;

public class LevelBroadcastServerCommand extends ServerCommand
{

	private final String _player;

	LevelBroadcastServerCommand(String player)
	{
		_player = player;
	}

	public String getPlayer()
	{
		return _player;
	}
}
