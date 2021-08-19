package mineplex.core.message.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.message.MessageManager;
import mineplex.core.preferences.Preference;

public class MessageCommand extends CommandBase<MessageManager>
{
	public MessageCommand(MessageManager plugin)
	{
		super(plugin, MessageManager.Perm.MESSAGE_COMMAND, "m", "msg", "message", "tell", "t", "w", "whisper", "MSG");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length == 0)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "You didn't specify someone to message!"));
		}
		else
		{
			// Parse Message
			String message;
			if (args.length > 1)
			{
				message = F.combine(args, 1, null, false);
			}
			else
			{
				if (Plugin.getPreferences().get(caller).isActive(Preference.RANDOM_MESSAGES))
				{
					message = Plugin.GetRandomMessage();
				}
				else
				{
					UtilPlayer.message(caller, F.main(Plugin.getName(), "Cat got your tongue?"));
					return;
				}
			}

			Plugin.sendMessage(caller, args[0], message, false, false);
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
	{
		return tabCompletePlayerNames(sender, args);
	}
}