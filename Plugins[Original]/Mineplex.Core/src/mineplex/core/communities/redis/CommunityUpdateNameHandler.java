package mineplex.core.communities.redis;

import mineplex.core.communities.CommunityManager;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class CommunityUpdateNameHandler implements CommandCallback
{
	private CommunityManager _manager;

	public CommunityUpdateNameHandler(CommunityManager manager)
	{
		_manager = manager;
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof CommunityUpdateName)
		{
			CommunityUpdateName update = ((CommunityUpdateName) command);
			Integer id = update.getCommunityId();
			String sender = update.getSender();
			String name = update.getName();
			
			_manager.handleCommunityNameUpdate(id, sender, name);
		}
	}
}