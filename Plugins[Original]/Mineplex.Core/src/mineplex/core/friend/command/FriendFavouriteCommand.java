package mineplex.core.friend.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.friend.FriendManager;
import mineplex.core.friend.FriendManager.Perm;

public class FriendFavouriteCommand extends CommandBase<FriendManager>
{

	public static final String COMMAND = "friendfavorite";

	public FriendFavouriteCommand(FriendManager plugin)
	{
		super(plugin, Perm.FRIEND_COMMAND, COMMAND, "ff");
	}

	@Override
	public void Execute(final Player caller, final String[] args)
	{
		if (args.length == 0)
		{
			return;
		}

		_commandCenter.GetClientManager().checkPlayerName(caller, args[0], result ->
		{
			if (result != null)
			{
				Plugin.toggleFavourite(caller, result, null);
			}
		});
	}
}