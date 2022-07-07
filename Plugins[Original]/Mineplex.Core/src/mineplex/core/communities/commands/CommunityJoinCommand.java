package mineplex.core.communities.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.data.Community.PrivacySetting;
import mineplex.core.communities.CommunityManager;
import mineplex.core.communities.data.ICommunity;

public class CommunityJoinCommand extends CommandBase<CommunityManager>
{
	public CommunityJoinCommand(CommunityManager plugin)
	{
		super(plugin, CommunityManager.Perm.COMMUNITY_JOIN_COMMAND, "join");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 1)
		{
			UtilPlayer.message(caller, F.help("/com join <community>", "Joins a community that is open or you have been invited to", ChatColor.DARK_AQUA));
			return;
		}

		ICommunity c = Plugin.getCommunity(args[0]);
		if (c == null)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "That community was not found!"));
			return;
		}

		// edge case someone can try if they really want: open communities with less than 5 members are forgotten here
		if (c.getPrivacySetting() != PrivacySetting.OPEN && !Plugin.Get(caller).Invites.contains(c.getId()))
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "You are have not been invited to " + F.name(c.getName()) + "!"));
			return;
		}

		if (c instanceof Community && ((Community) c).getMembers().containsKey(caller.getUniqueId()))
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "You are already in " + F.name(c.getName()) + "!"));
			return;
		}

		Plugin.handleJoin(caller, c, Plugin.Get(caller).Invites.contains(c.getId()));
	}
}