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
import mineplex.core.personalServer.PersonalServerManager;

public class CommunityMCSCommand extends CommandBase<CommunityManager>
{
	public CommunityMCSCommand(CommunityManager plugin)
	{
		super(plugin, CommunityManager.Perm.COMMUNITY_MCS_COMMAND, "mcs");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 1)
		{
			UtilPlayer.message(caller, F.help("/com mcs <community>", "Opens the Mineplex Community Server of a community you manage", ChatColor.DARK_AQUA));
			return;
		}
		Community c = Plugin.getLoadedCommunity(args[0]);
		if (c == null)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "That community was not found!"));
			return;
		}
		if (c.getMembers().getOrDefault(caller.getUniqueId(), new CommunityMemberInfo(caller.getName(), caller.getUniqueId(), -1, CommunityRole.MEMBER, -1L)).Role.ordinal() > CommunityRole.COLEADER.ordinal())
		{
			if (!Managers.get(CoreClientManager.class).Get(caller).hasPermission(CommunityManager.Perm.COMMUNITY_MCS_STAFF_COMMAND))
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "You are not a co-leader of " + F.name(c.getName()) + "!"));
				return;
			}
		}
		
		Managers.get(PersonalServerManager.class).hostCommunityServer(caller, c);
	}
}