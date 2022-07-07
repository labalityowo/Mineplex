package mineplex.core.give.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.give.Give;

public class GiveCommand extends CommandBase<Give>
{
	public GiveCommand(Give plugin)
	{
		super(plugin, Give.Perm.GIVE_COMMAND, "give", "g", "item", "i");
	}

	@Override
	public void Execute(final Player caller, final String[] args)
	{
		Plugin.parseInput(caller, args);
	}
}