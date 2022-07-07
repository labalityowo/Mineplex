package mineplex.core.communities.redis;

import mineplex.core.communities.CommunityManager;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class CommunityChatHandler implements CommandCallback
{
	private CommunityManager _manager;

	public CommunityChatHandler(CommunityManager manager)
	{
		_manager = manager;
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof CommunityChat)
		{
			CommunityChat chat = ((CommunityChat) command);
			_manager.handleCommunityChat(chat.getCommunityId(), chat.getSenderName(), chat.getMessage());
		}
	}
}