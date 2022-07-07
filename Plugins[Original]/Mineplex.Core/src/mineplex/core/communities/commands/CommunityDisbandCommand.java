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

public class CommunityDisbandCommand extends CommandBase<CommunityManager>
{
	public CommunityDisbandCommand(CommunityManager plugin)
	{
		super(plugin, CommunityManager.Perm.COMMUNITY_DISBAND_COMMAND, "disband");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 1)
		{
			UtilPlayer.message(caller, F.help("/com disband <community>", "Disbands a community you own", ChatColor.DARK_AQUA));
			return;
		}
		Community c = Plugin.getLoadedCommunity(args[0]);
		if (c == null)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "That community was not found!"));
			return;
		}
		if (c.getMembers().getOrDefault(caller.getUniqueId(), new CommunityMemberInfo(caller.getName(), caller.getUniqueId(), -1, CommunityRole.MEMBER, -1L)).Role != CommunityRole.LEADER)
		{
			if (!Managers.get(CoreClientManager.class).Get(caller).hasPermission(CommunityManager.Perm.COMMUNITY_DISBAND_STAFF_COMMAND))
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "You are not the leader of " + F.name(c.getName()) + "!"));
				return;
			}
		}
		if (!c.getMembers().containsKey(caller.getUniqueId()))
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "You have disbanded community " + F.name(c.getName()) + "!"));
		}
		Plugin.handleDisband(caller, c);
	}
}