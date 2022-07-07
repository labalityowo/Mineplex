package mineplex.core.communities.redis;

import java.util.UUID;

import mineplex.core.communities.CommunityManager;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class CommunityJoinRequestHandler implements CommandCallback
{
	private CommunityManager _manager;

	public CommunityJoinRequestHandler(CommunityManager manager)
	{
		_manager = manager;
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof CommunityJoinRequest)
		{
			CommunityJoinRequest update = ((CommunityJoinRequest) command);
			Integer id = update.getCommunityId();
			UUID uuid = UUID.fromString(update.getPlayerUUID());
			String name = update.getPlayerName();
			Integer accountId = update.getAccountId();
			
			_manager.handleCommunityJoinRequest(id, name, uuid, accountId);
		}
	}
}