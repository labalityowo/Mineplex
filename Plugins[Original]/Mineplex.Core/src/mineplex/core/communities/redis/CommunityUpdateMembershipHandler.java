package mineplex.core.communities.redis;

import java.util.UUID;

import mineplex.core.communities.CommunityManager;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class CommunityUpdateMembershipHandler implements CommandCallback
{
	private CommunityManager _manager;

	public CommunityUpdateMembershipHandler(CommunityManager manager)
	{
		_manager = manager;
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof CommunityUpdateMembership)
		{
			CommunityUpdateMembership update = ((CommunityUpdateMembership) command);
			Integer id = update.getCommunityId();
			UUID uuid = UUID.fromString(update.getPlayerUUID());
			String sender = update.getSender();
			String name = update.getPlayerName();
			Integer accountId = update.getAccountId();
			boolean kick = update.isKick();
			boolean leave = update.isLeave();
			
			_manager.handleCommunityMembershipUpdate(id, sender, name, uuid, accountId, kick, leave);
		}
	}
}