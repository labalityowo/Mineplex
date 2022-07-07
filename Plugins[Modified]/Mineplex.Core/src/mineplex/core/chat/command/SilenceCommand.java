package mineplex.core.chat.command;

import org.bukkit.entity.Player;

import mineplex.core.chat.Chat;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class SilenceCommand extends CommandBase<Chat>
{
	public SilenceCommand(Chat plugin)
	{
		super(plugin, Chat.Perm.SILENCE_COMMAND, "silence");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		try
		{
			//Toggle
			if (args.length == 0)
			{
				//Disable
				if (Plugin.getChatSilence() != 0)
				{
					Plugin.setChatSilence(0, true);
				}
				//Enable
				else
				{
					Plugin.setChatSilence(-1, true);
				}
			}
			//Timer
			else
			{
				long time = (long) (Double.valueOf(args[0]) * 1000 * 60);
				
				Plugin.setChatSilence(time, true);
			}
		}
		catch (Exception e)
		{
			UtilPlayer.message(caller, F.main("Chat", "Invalid Time Parameter."));
		}
	}
}