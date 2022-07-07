package mineplex.core.message.commands;

import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.message.MessageManager;
import mineplex.serverdata.commands.AnnouncementCommand;

public class AnnounceCommand extends CommandBase<MessageManager>
{
	public AnnounceCommand(MessageManager plugin)
	{
		super(plugin, MessageManager.Perm.ANNOUNCE_COMMAND, "announce");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length <= 1)
		{			
			Plugin.Help(caller);
		}
		else
		{
			Optional<PermissionGroup> group = PermissionGroup.getGroup(args[0]);
			if (!group.isPresent())
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), ChatColor.RED + "" + ChatColor.BOLD + "Invalid rank!"));
				return;
			}

			new AnnouncementCommand(true, group.get().name(), F.combine(args, 1, null, false)).publish();
		}
	}
}