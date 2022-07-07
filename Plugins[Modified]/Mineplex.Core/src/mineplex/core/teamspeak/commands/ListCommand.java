package mineplex.core.teamspeak.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.teamspeak.TeamspeakManager;

public class ListCommand extends CommandBase<TeamspeakManager>
{
	public ListCommand(TeamspeakManager plugin)
	{
		super(plugin, TeamspeakManager.Perm.LIST_COMMAND, "list");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.displayUnlinkPrompt(caller, args.length == 0 ? "1" : args[0]);
	}
}