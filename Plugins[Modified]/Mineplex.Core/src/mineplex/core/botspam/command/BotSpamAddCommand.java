package mineplex.core.botspam.command;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

import mineplex.core.botspam.BotSpamManager;
import mineplex.core.botspam.SpamText;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class BotSpamAddCommand extends CommandBase<BotSpamManager>
{
	public BotSpamAddCommand(BotSpamManager plugin)
	{
		super(plugin, BotSpamManager.Perm.ADD_BOTSPAM_COMMAND, "add");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args != null && args.length >= 1)
		{
			String text = StringUtils.join(args, " ", 0, args.length);

			if (text.length() < 8)
			{
				UtilPlayer.message(caller, F.main("BotSpam", "Spam text must be at least 8 characters"));
				return;
			}

			for (SpamText spamText : Plugin.getSpamTexts())
			{
				if (text.equalsIgnoreCase(spamText.getText()))
				{
					UtilPlayer.message(caller, F.main("BotSpam", "That Spam Text (id " + spamText.getId() + ") already exists. Type " + F.elem("/botspam list") + " to view"));
					return;
				}
			}

			Plugin.addSpamText(caller.getName(), text, () ->
			{
				if (caller.isOnline())
					UtilPlayer.message(caller, F.main("BotSpam", "Added Spam Text: " + F.elem(text)));
			});
		}
		else
		{
			UtilPlayer.message(caller, F.main("BotSpam", "/botspam add <text>"));
		}
	}
}