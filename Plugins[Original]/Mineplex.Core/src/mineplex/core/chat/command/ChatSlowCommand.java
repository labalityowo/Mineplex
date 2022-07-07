package mineplex.core.chat.command;

import org.bukkit.entity.Player;

import mineplex.core.chat.Chat;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class ChatSlowCommand extends CommandBase<Chat>
{
	public ChatSlowCommand(Chat plugin)
	{
		super(plugin, Chat.Perm.SLOW_CHAT_COMMAND, "chatslow");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 1)
		{
			try
			{
				int seconds = Integer.parseInt(args[0]);

				if (seconds < 0)
				{
					UtilPlayer.message(caller, F.main("Chat", "Seconds must be a positive integer"));
					return;
				}

				Plugin.setChatSlow(seconds, true);
				UtilPlayer.message(caller, F.main("Chat", "Set chat slow to " + F.time(seconds + " seconds")));
			}
			catch (Exception e)
			{
				showUsage(caller);
			}
		}
		else
		{
			showUsage(caller);
		}
	}

	private void showUsage(Player caller)
	{
		UtilPlayer.message(caller, F.main("Chat", "Usage: /chatslow <seconds>"));
	}
}