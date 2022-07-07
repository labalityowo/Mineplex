package mineplex.core.chat.command;

import org.bukkit.entity.Player;

import mineplex.core.chat.Chat;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;

public class BroadcastCommand extends CommandBase<Chat>
{
	public BroadcastCommand(Chat plugin)
	{
		super(plugin, Chat.Perm.BROADCAST_COMMAND, "s");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			UtilPlayer.message(caller, F.main("Broadcast", "What are you broadcasting?"));
			return;
		}

		String announcement = "";

		for (String arg : args)
			announcement += arg + " ";

		if (announcement.length() > 0)
			announcement = announcement.substring(0, announcement.length()-1);

		UtilServer.broadcast(caller.getName(), announcement);
	}
}