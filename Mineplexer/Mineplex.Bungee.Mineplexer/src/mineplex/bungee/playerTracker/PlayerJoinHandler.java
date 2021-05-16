package mineplex.bungee.playerTracker;

import java.util.UUID;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.PlayerJoinCommand;
import mineplex.serverdata.commands.ServerCommand;

public class PlayerJoinHandler implements CommandCallback 
{
	private PlayerTracker _playerTracker;
	
	public PlayerJoinHandler(PlayerTracker playerTracker) 
	{
		_playerTracker = playerTracker;
	}

	@Override
	public void run(ServerCommand command) 
	{
		if (command instanceof PlayerJoinCommand)
		{
			PlayerJoinCommand joinCommand = (PlayerJoinCommand)command;
			_playerTracker.kickPlayerIfOnline(UUID.fromString(joinCommand.getUuid()));
		}
	}
}
