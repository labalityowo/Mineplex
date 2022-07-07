package mineplex.core.friend.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.friend.FriendManager;
import mineplex.core.friend.FriendManager.Perm;
import mineplex.core.friend.FriendVisibility;
import mineplex.core.recharge.Recharge;

public class FriendVisibilityCommand extends CommandBase<FriendManager>
{

	public static final String COMMAND = "friendvisibility";

	public FriendVisibilityCommand(FriendManager plugin)
	{
		super(plugin, Perm.FRIEND_COMMAND, COMMAND);
	}

	@Override
	public void Execute(final Player caller, final String[] args)
	{
		if (args.length == 0 || !Recharge.Instance.use(caller, COMMAND, 3000, false, false))
		{
			return;
		}

		try
		{
			Plugin.setVisibility(caller, FriendVisibility.valueOf(args[0]));
		}
		catch (IllegalArgumentException ex)
		{
			caller.sendMessage(F.main(Plugin.getName(), "Invalid argument."));
		}
	}
}