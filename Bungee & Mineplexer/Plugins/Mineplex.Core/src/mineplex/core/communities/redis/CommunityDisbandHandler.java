package mineplex.core.communities.redis;

import java.util.UUID;

import mineplex.core.communities.CommunityManager;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class CommunityDisbandHandler implements CommandCallback
{
	private CommunityManager _manager;

	public CommunityDisbandHandler(CommunityManager manager)
	{
		_manager = manager;
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof CommunityDisband)
		{
			CommunityDisband update = ((CommunityDisband) command);
			String senderName = update.getSenderName();
			Integer communityId = update.getCommunityId();
			
			_manager.handleCommunityDisband(communityId, senderName);
		}
	}
}