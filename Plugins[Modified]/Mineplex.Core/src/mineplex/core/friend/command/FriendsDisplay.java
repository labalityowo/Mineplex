package mineplex.core.friend.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.friend.FriendManager;
import mineplex.core.friend.FriendManager.Perm;

public class FriendsDisplay extends CommandBase<FriendManager>
{

    public static final String COMMAND = "friendsdisplay";

    public FriendsDisplay(FriendManager plugin)
    {
        super(plugin, Perm.FRIEND_COMMAND, COMMAND);
    }

    @Override
    public void Execute(Player caller, final String[] args)
	{
		if (args.length > 0)
		{
			Plugin.showFriendsInUI(caller);
		}
		else
		{
			Plugin.showFriends(caller, true);
		}
	}
}