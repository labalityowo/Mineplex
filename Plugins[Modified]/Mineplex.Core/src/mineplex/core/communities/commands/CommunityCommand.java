package mineplex.core.communities.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.CommunityManager;
import mineplex.core.communities.data.ICommunity;
import mineplex.core.communities.gui.pages.CommunityMembersPage;
import mineplex.core.communities.gui.pages.CommunityOverviewPage;

public class CommunityCommand extends MultiCommandBase<CommunityManager>
{
	public CommunityCommand(CommunityManager plugin)
	{
		super(plugin, CommunityManager.Perm.COMMUNITY_COMMAND, "community", "communities", "com");
		
		AddCommand(new CommunityChatCommand(plugin));
		AddCommand(new CommunityCreateCommand(plugin));
		AddCommand(new CommunityDescriptionCommand(plugin));
		AddCommand(new CommunityDisbandCommand(plugin));
		AddCommand(new CommunityInviteCommand(plugin));
		AddCommand(new CommunityJoinCommand(plugin));
		AddCommand(new CommunityMCSCommand(plugin));
		//AddCommand(new CommunityMenuCommand(plugin));
		AddCommand(new CommunityRenameCommand(plugin));
		AddCommand(new CommunityUnInviteCommand(plugin));
		AddCommand(new CommunityCoLeadCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		if (args.length > 0)
		{
			if (args[0].equalsIgnoreCase("help"))
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Community Commands:"));
				UtilPlayer.message(caller, F.help("/com <community>", "Opens a community's menu", ChatColor.DARK_AQUA));
				UtilPlayer.message(caller, F.help("/com invite <player> <community>", "Invites a player to a community you manage", ChatColor.DARK_AQUA));
				UtilPlayer.message(caller, F.help("/com uninvite <player> <community>", "Revokes a player's invitation to a community you manage", ChatColor.DARK_AQUA));
				UtilPlayer.message(caller, F.help("/com join <community>", "Joins a community that is open or you have been invited to", ChatColor.DARK_AQUA));
				UtilPlayer.message(caller, F.help("/com chat <community>", "Selects which community you chat to", ChatColor.DARK_AQUA));
				UtilPlayer.message(caller, F.help("/com create <name>", "Creates a new community", ChatColor.DARK_AQUA));
				UtilPlayer.message(caller, F.help("/com rename <community> <name>", "Changes the name of a community you own", ChatColor.DARK_AQUA));
				UtilPlayer.message(caller, F.help("/com mcs <community>", "Opens the Mineplex Community Server of a community you manage", ChatColor.DARK_AQUA));
				UtilPlayer.message(caller, F.help("/com description <community> <description>", "Sets the description of a community you manage", ChatColor.DARK_AQUA));
				UtilPlayer.message(caller, F.help("/com disband <community>", "Disbands a community you own", ChatColor.DARK_AQUA));
				UtilPlayer.message(caller, F.help("/com colead <community> <player>", "Promotes a player to Co-Leader", ChatColor.DARK_AQUA));
				return;
			}

			ICommunity community = Plugin.getCommunity(args[0]);
			if (community == null)
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Could not find community " + F.name(args[0]) + "!"));
			}
			else
			{
				Plugin.tempLoadCommunity(community.getId(), real -> new CommunityMembersPage(caller, real));
			}
			return;
		}
		
		new CommunityOverviewPage(caller);
	}
}