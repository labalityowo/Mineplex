package mineplex.core.communities.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.CommunityManager;
import mineplex.core.communities.data.CommunityMemberInfo;
import mineplex.core.communities.data.CommunityRole;

public class CommunityRenameCommand extends CommandBase<CommunityManager>
{
	public CommunityRenameCommand(CommunityManager plugin)
	{
		super(plugin, CommunityManager.Perm.COMMUNITY_RENAME_COMMAND, "rename");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.help("/com rename <community> <name>", "Changes the name of a community you own", ChatColor.DARK_AQUA));
			return;
		}

		Community c = Plugin.getLoadedCommunity(args[0]);
		String newName = args[1];

		if (c == null)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "That community was not found!"));
			return;
		}

		if (c.getMembers().getOrDefault(caller.getUniqueId(), new CommunityMemberInfo(caller.getName(), caller.getUniqueId(), -1, CommunityRole.MEMBER, -1L)).Role != CommunityRole.LEADER)
		{
			if (!Managers.get(CoreClientManager.class).Get(caller).hasPermission(CommunityManager.Perm.COMMUNITY_RENAME_STAFF_COMMAND))
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "You are not the leader of " + F.name(c.getName()) + "!"));
				return;
			}
		}

		if (!Plugin.isNameValid(newName))
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "A community name cannot be longer than 15 characters and must be alphanumeric!"));
			return;
		}

		if (Plugin.BLOCKED_NAMES.contains(newName.toLowerCase()))
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "That name is not allowed!"));
			return;
		}

		Plugin.runAsync(() ->
		{
			Plugin.communityExists(newName, (exists) ->
			{
				if (exists)
				{
					UtilPlayer.message(caller, F.main(Plugin.getName(), "A community with that name already exists!"));
					return;
				}

				if (!Plugin.isNameAllowed(caller, newName))
				{
					UtilPlayer.message(caller, F.main(Plugin.getName(), "That name is not allowed!"));
				}
				else
				{
					if (!c.getMembers().containsKey(caller.getUniqueId()))
					{
						UtilPlayer.message(caller, F.main(Plugin.getName(), "You have changed the name of " + F.name(c.getName()) + " to " + F.name(newName) + "!"));
					}
					Plugin.handleNameUpdate(caller, c, newName);
				}
			});
		});
	}
}