package mineplex.core.bonuses.commands;

import org.bukkit.entity.Player;

import mineplex.core.bonuses.BonusManager;
import mineplex.core.command.CommandBase;
import mineplex.core.command.CommandCenter;
import mineplex.core.command.ICommand;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilPlayer;

/**
 * Allows players to run rank-specific commands
 * Found no better place to create it
 */
public class AllowCommand extends CommandBase<BonusManager>
{
	private BonusManager _plugin;

	public AllowCommand(BonusManager plugin)
	{
		super(plugin, BonusManager.Perm.ALLOW_COMMAND, "allowCommand", "allowCmd");
		_plugin = plugin;
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 2 || args.length > 3)
		{
			UtilPlayer.message(caller, F.main("Allow Command", "Usage: /allowCmd <player> <command> [disallow]"));
			return;
		}
		NautHashMap<String, ICommand> commands = CommandCenter.getCommands();
		if (!commands.containsKey(args[1].toLowerCase()))
		{
			UtilPlayer.message(caller, F.main("Allow Command", "Command not found!"));
			return;
		}
		ICommand iCommand = commands.get(args[1]);
		if (!_plugin.getClientManager().Get(caller).hasPermission(iCommand.getPermission()))
		{
			UtilPlayer.message(caller, F.main("Allow Command", "You're not allowed to use that command!"));
			return;
		}
		boolean disallow = false;
		if (args.length == 3)
			disallow = Boolean.parseBoolean(args[2]);
		Player receiver = UtilPlayer.searchExact(args[0]);
		if (receiver == null)
		{
			UtilPlayer.message(caller, F.main("Allow Command", "Could not find player " + F.name(args[0]) + "!"));
			return;
		}
		if (receiver.getUniqueId().equals(caller.getUniqueId()))
		{
			UtilPlayer.message(caller, F.main("Allow Command", "You can't use that for yourself!"));
			return;
		}
		if (disallow)
		{
			boolean canDisallow = UtilPlayer.disallowCommand(receiver, args[1].toLowerCase());
			if (!canDisallow)
			{
				UtilPlayer.message(caller, F.main("Allow Command", "That command was not allowed for the player " + F.name(receiver.getName()) + "!"));
				return;
			}
			UtilPlayer.message(caller, F.main("Allow Command", "You disallowed the player " + F.name(receiver.getName()) + " to use the command " + F.elem(args[1]) + "!"));
			UtilPlayer.message(receiver, F.main("Allow Command", "The player " + F.name(caller.getName()) + " disallowed you to use the command " + F.elem(args[1]) + "!"));
			return;
		}
		UtilPlayer.allowCommand(receiver, args[1].toLowerCase());
		UtilPlayer.message(caller, F.main("Allow Command", "You allowed the player " + F.name(receiver.getName()) + " to use the command " + F.elem(args[1]) + "!"));
		UtilPlayer.message(receiver, F.main("Allow Command", "The player " + F.name(caller.getName()) + " allowed you to use the command " + F.elem(args[1]) + "!"));
	}
}