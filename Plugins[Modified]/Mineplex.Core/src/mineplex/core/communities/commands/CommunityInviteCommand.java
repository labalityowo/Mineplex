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

public class CommunityInviteCommand extends CommandBase<CommunityManager>
{
	public CommunityInviteCommand(CommunityManager plugin)
	{
		super(plugin, CommunityManager.Perm.COMMUNITY_INVITE_COMMAND, "invite");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.help("/com invite <player> <community>", "Invites a player to a community you manage", ChatColor.DARK_AQUA));
			return;
		}
		String player = args[0];
		Community c = Plugin.getLoadedCommunity(args[1]);
		if (c == null)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "That community was not found!"));
			return;
		}
		if (c.getMembers().getOrDefault(caller.getUniqueId(), new CommunityMemberInfo(caller.getName(), caller.getUniqueId(), -1, CommunityRole.MEMBER, -1L)).Role.ordinal() > CommunityRole.COLEADER.ordinal())
		{
			if (!Managers.get(CoreClientManager.class).Get(caller).hasPermission(CommunityManager.Perm.COMMUNITY_INVITE_STAFF_COMMAND))
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "You are not a co-leader of " + F.name(c.getName()) + "!"));
				return;
			}
		}
		for (CommunityMemberInfo info : c.getMembers().values())
		{
			if (info.Name.equalsIgnoreCase(player))
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), F.name(player) + " is already a member of " + F.name(c.getName()) + "!"));
				return;
			}
		}
		Plugin.handleInvite(caller, c, player);
	}
}