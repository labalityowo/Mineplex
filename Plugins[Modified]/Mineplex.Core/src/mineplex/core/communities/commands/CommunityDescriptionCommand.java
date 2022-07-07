package mineplex.core.communities.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.chat.Chat;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.CommunityManager;
import mineplex.core.communities.data.CommunityMemberInfo;
import mineplex.core.communities.data.CommunityRole;
import mineplex.core.communities.data.CommunitySetting;

public class CommunityDescriptionCommand extends CommandBase<CommunityManager>
{
	public CommunityDescriptionCommand(CommunityManager plugin)
	{
		super(plugin, CommunityManager.Perm.COMMUNITY_DESCRIPTION_COMMAND, "description");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.help("/com description <community> <description>", "Sets the description of a community you manage", ChatColor.DARK_AQUA));
			return;
		}
		Community c = Plugin.getLoadedCommunity(args[0]);
		String desc = args[1];
		for (int i = 2; i < args.length; i++)
		{
			desc += (" " + args[i]);
		}
		if (c == null)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "That community was not found!"));
			return;
		}
		if (c.getMembers().getOrDefault(caller.getUniqueId(), new CommunityMemberInfo(caller.getName(), caller.getUniqueId(), -1, CommunityRole.MEMBER, -1L)).Role.ordinal() > CommunityRole.COLEADER.ordinal())
		{
			if (!Managers.get(CoreClientManager.class).Get(caller).hasPermission(CommunityManager.Perm.COMMUNITY_DESCRIPTION_STAFF_COMMAND))
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "You are not a co-leader of " + F.name(c.getName()) + "!"));
				return;
			}
		}
		if (desc.length() > 30)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "A community description cannot be longer than 30 characters!"));
			return;
		}
		if (Plugin.NON_ALPHANUMERIC_PATTERN.matcher(desc.replace(" ", "").replace("!", "").replace("?", "").replace(".", "").replace("'", "").replace("\"", "")).find())
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "A community description must be alphanumeric!"));
			return;
		}
		final String description = desc;
		Plugin.runAsync(() ->
		{
			if (Managers.get(Chat.class).filterMessage(caller, description).contains("*"))
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "That description is not allowed!"));
			}
			else
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "You have changed the description of " + F.name(c.getName()) + " to " + F.elem(description) + "!"));
				Plugin.handleSettingUpdate(caller, c, CommunitySetting.DESCRIPTION, description);
			}
		});
	}
}