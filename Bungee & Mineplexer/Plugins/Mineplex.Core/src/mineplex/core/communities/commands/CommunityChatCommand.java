package mineplex.core.communities.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.CommunityManager;

public class CommunityChatCommand extends CommandBase<CommunityManager>
{

	public CommunityChatCommand(CommunityManager plugin)
	{
		super(plugin, CommunityManager.Perm.COMMUNITY_CHAT_COMMAND, "chat");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 1)
		{
			UtilPlayer.message(caller, F.help("/com chat <community>", "Selects which community you chat to", ChatColor.DARK_AQUA));
			return;
		}
		Community c = Plugin.getLoadedCommunity(args[0]);
		if (c == null)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "That community was not found!"));
			return;
		}
		if (!c.getMembers().containsKey(caller.getUniqueId()))
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "You are not in " + F.name(c.getName()) + "!"));
			return;
		}
		UtilPlayer.message(caller, F.main(Plugin.getName(), "You are now chatting to " + F.name(c.getName()) + "! Use " + F.elem("!") + " before your message to use community chat!"));

		Plugin.getCustomDataManager().Get(caller).put(CommunityManager.COMMUNITY_CHAT_KEY, c.getId());
		Plugin.getCustomDataManager().saveData(caller);

		Plugin.Get(caller).setCommunityChattingTo(c);
	}
}