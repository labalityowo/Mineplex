package mineplex.core.message.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.message.MessageManager;
import mineplex.core.preferences.Preference;

public class MessageAdminCommand extends CommandBase<MessageManager>
{
    public MessageAdminCommand(MessageManager plugin)
    {
        super(plugin, MessageManager.Perm.MESSAGE_ADMIN_COMMAND, "ma");
    }

    @Override
    public void Execute(Player caller, String[] args)
    {
        if (args == null || args.length == 0)
        {
            UtilPlayer.message(caller, F.help(Plugin.getName(), "/ma <player> [message]", ChatColor.GOLD));
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
            Plugin.sendMessage(caller, args[0], message, false, true);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
    {
        return tabCompletePlayerNames(sender, args);
    }
}