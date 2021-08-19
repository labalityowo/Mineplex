package mineplex.core.communities.redis;

import java.util.UUID;

import mineplex.core.communities.CommunityManager;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class CommunityCloseJoinRequestHandler implements CommandCallback
{
	private CommunityManager _manager;

	public CommunityCloseJoinRequestHandler(CommunityManager manager)
	{
		_manager = manager;
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof CommunityCloseJoinRequest)
		{
			CommunityCloseJoinRequest update = ((CommunityCloseJoinRequest) command);
			Integer id = update.getCommunityId();
			String sender = update.getSender();
			UUID uuid = UUID.fromString(update.getPlayerUUID());
			String name = update.getPlayerName();
			Integer accountId = update.getAccountId();
			boolean announce = update.shouldAnnounce();
			
			_manager.handleCommunityCloseJoinRequest(id, sender, name, uuid, accountId, announce);
		}
	}
}