package mineplex.core.message.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.message.MessageManager;
import mineplex.core.preferences.Preference;

public class ResendAdminCommand extends CommandBase<MessageManager>
{
	public ResendAdminCommand(MessageManager plugin)
	{
		super(plugin, MessageManager.Perm.RESEND_ADMIN_COMMAND, "ra");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length == 0)
		{
			UtilPlayer.message(caller, F.help(Plugin.getName(), "/ra [message]", ChatColor.GOLD));
		}
		else
		{
            String lastTo = Plugin.Get(caller).LastAdminTo;

            // Get To
            if (lastTo == null)
            {
                UtilPlayer.message(caller, F.main(Plugin.getName(), "You have not admin messaged anyone recently."));
                return;
            }

            // Parse Message
            String message = "Beep!";
            if (args.length > 0)
            {
                message = F.combine(args, 0, null, false);
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

            Plugin.sendMessage(caller, lastTo, message, true, true);
		}
	}
}