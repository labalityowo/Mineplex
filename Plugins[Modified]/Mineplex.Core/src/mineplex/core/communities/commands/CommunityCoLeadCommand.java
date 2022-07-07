package mineplex.core.communities.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.communities.CommunityManager;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.data.CommunityMemberData;
import mineplex.core.communities.data.CommunityMemberInfo;
import mineplex.core.communities.data.CommunityRole;

public class CommunityCoLeadCommand extends CommandBase<CommunityManager>
{
	public CommunityCoLeadCommand(CommunityManager plugin)
	{
		super(plugin, CommunityManager.Perm.COMMUNITY_COLEAD_COMMAND, "colead");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 2)
		{
			reply(caller, "Invalid arguments. Try /colead <com> <player>");
			return;
		}

		Community community = Plugin.getLoadedCommunity(args[0]);
		if (community == null)
		{
			reply(caller, "Unknown community: " + args[0]);
			return;
		}

		CommunityMemberInfo member = community.getMembers().get(caller.getUniqueId());
		if (member == null)
		{
			reply(caller, "You are not a member of " + community.getName());
			return;
		}

		if (member.Role != CommunityRole.LEADER)
		{
			reply(caller, "You are not the leader of " + community.getName());
			return;
		}

		for (CommunityMemberInfo info : community.getMembers().values())
		{
			if (info.Name.equalsIgnoreCase(args[1]) || info.UUID.toString().equals(args[1]))
			{
				if (info.Role == CommunityRole.COLEADER)
				{
					reply(caller, info.Name + " is alredy a Co-Leader");
					return;
				}

				Plugin.handleRoleUpdate(caller, community, info, CommunityRole.COLEADER);
				reply(caller, "You have updated " + info.Name + "\'s role to Co-Leader");
				return;
			}
		}

		reply(caller, "Unknown player: " + args[1]);
	}
}
