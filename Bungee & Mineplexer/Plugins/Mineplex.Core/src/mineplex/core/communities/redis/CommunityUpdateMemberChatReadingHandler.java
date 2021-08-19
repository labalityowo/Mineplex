package mineplex.core.communities.redis;

import java.util.UUID;

import mineplex.core.communities.CommunityManager;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class CommunityUpdateMemberChatReadingHandler implements CommandCallback
{
	private CommunityManager _manager;

	public CommunityUpdateMemberChatReadingHandler(CommunityManager manager)
	{
		_manager = manager;
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof CommunityUpdateMemberChatReading)
		{
			CommunityUpdateMemberChatReading update = ((CommunityUpdateMemberChatReading) command);
			Integer id = update.getCommunityId();
			UUID uuid = UUID.fromString(update.getPlayerUUID());
			boolean reading = update.reading();
			
			_manager.handleToggleReadingCommunityChat(id, uuid, reading);
		}
	}
}