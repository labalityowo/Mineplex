package mineplex.core.friend.command;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.friend.FriendManager;
import mineplex.core.friend.FriendManager.Perm;
import mineplex.core.preferences.Preference;

public class AddFriend extends CommandBase<FriendManager>
{

    public static final String COMMAND = "friend";

    public AddFriend(FriendManager plugin)
    {
        super(plugin, Perm.FRIEND_COMMAND, "friends", COMMAND, "f");
    }

    @Override
    public void Execute(final Player caller, final String[] args)
    {
        if (args == null || args.length < 1)
        {
            Plugin.showFriends(caller, false);
        }
        else
        {
            _commandCenter.GetClientManager().checkPlayerName(caller, args[0], result ->
            {
                if (result != null)
                {
                    Plugin.addFriend(caller, result);
                }
            });
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
    {
        return tabCompletePlayerNames(sender, args);
    }
}