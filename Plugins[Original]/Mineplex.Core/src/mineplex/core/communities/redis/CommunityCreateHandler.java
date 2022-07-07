package mineplex.core.communities.redis;

import java.util.UUID;

import mineplex.core.communities.CommunityManager;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class CommunityCreateHandler implements CommandCallback
{
	private CommunityManager _manager;

	public CommunityCreateHandler(CommunityManager manager)
	{
		_manager = manager;
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof CommunityCreate)
		{
			CommunityCreate update = ((CommunityCreate) command);
			UUID leaderUUID = UUID.fromString(update.getLeaderUUID());
			Integer communityId = update.getCommunityId();
			String communityName = update.getCommunityName();
			Integer leaderId = update.getLeaderId();
			String leaderName = update.getLeaderName();
			
			_manager.handleCommunityCreation(communityId, communityName, leaderId, leaderUUID, leaderName);
		}
	}
}