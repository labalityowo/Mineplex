package mineplex.core.communities.redis;

import java.util.UUID;

import mineplex.core.communities.CommunityManager;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class CommunityUnInviteHandler implements CommandCallback
{
	private CommunityManager _manager;

	public CommunityUnInviteHandler(CommunityManager manager)
	{
		_manager = manager;
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof CommunityUnInvite)
		{
			CommunityUnInvite update = ((CommunityUnInvite) command);
			Integer id = update.getCommunityId();
			String sender = update.getSender();
			String name = update.getPlayerName();
			UUID uuid = UUID.fromString(update.getPlayerUUID());
			boolean announce = update.shouldAnnounce();
			
			_manager.handleCommunityUninvite(id, sender, name, uuid, announce);
		}
	}
}