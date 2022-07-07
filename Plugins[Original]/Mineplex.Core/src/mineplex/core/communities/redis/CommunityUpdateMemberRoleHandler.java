package mineplex.core.communities.redis;

import java.util.UUID;

import mineplex.core.communities.CommunityManager;
import mineplex.core.communities.data.CommunityRole;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class CommunityUpdateMemberRoleHandler implements CommandCallback
{
	private CommunityManager _manager;

	public CommunityUpdateMemberRoleHandler(CommunityManager manager)
	{
		_manager = manager;
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof CommunityUpdateMemberRole)
		{
			CommunityUpdateMemberRole update = ((CommunityUpdateMemberRole) command);
			Integer id = update.getCommunityId();
			UUID uuid = UUID.fromString(update.getPlayerUUID());
			CommunityRole role = CommunityRole.parseRole(update.getMemberRole());
			
			_manager.handleCommunityMembershipRoleUpdate(id, update.getSender(), uuid, role);
		}
	}
}