package mineplex.core.communities.redis;

import java.util.UUID;

import mineplex.core.communities.CommunityManager;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class CommunityInviteHandler implements CommandCallback
{
	private CommunityManager _manager;

	public CommunityInviteHandler(CommunityManager manager)
	{
		_manager = manager;
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof CommunityInvite)
		{
			CommunityInvite update = ((CommunityInvite) command);
			Integer id = update.getCommunityId();
			String sender = update.getSender();
			String name = update.getPlayerName();
			UUID uuid = UUID.fromString(update.getPlayerUUID());
			
			_manager.handleCommunityInvite(id, sender, name, uuid);
		}
	}
}