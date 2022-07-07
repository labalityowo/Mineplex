package mineplex.core.communities.redis;

import mineplex.core.communities.CommunityManager;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.PlayerJoinCommand;
import mineplex.serverdata.commands.ServerCommand;

public class PlayerJoinHandler implements CommandCallback 
{
	private CommunityManager _communityManager;
	
	public PlayerJoinHandler(CommunityManager communityManager) 
	{
		_communityManager = communityManager;
	}

	@Override
	public void run(ServerCommand command) 
	{
		if (command instanceof PlayerJoinCommand)
		{
			PlayerJoinCommand joinCommand = (PlayerJoinCommand)command;
			//_communityManager.updateAllMemberData(UUID.fromString(joinCommand.getUuid()), joinCommand.getName());
		}
	}
}