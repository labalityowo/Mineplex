package mineplex.core.friend.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.friend.FriendManager;
import mineplex.core.friend.FriendManager.Perm;

public class DeleteFriend extends CommandBase<FriendManager>
{

	public static final String COMMAND = "unfriend";

	public DeleteFriend(FriendManager plugin)
	{
		super(plugin, Perm.FRIEND_COMMAND, COMMAND);
	}

	@Override
	public void Execute(final Player caller, final String[] args)
	{
		if (args == null)
		{
			caller.sendMessage(F.main(Plugin.getName(), "You need to include a player's name."));
		}
		else
		{
			_commandCenter.GetClientManager().checkPlayerName(caller, args[0], result ->
			{
				if (result != null)
				{
					Plugin.removeFriend(caller, result);
				}
			});
		}
	}
}