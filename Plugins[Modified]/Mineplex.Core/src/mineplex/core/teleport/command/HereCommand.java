package mineplex.core.teleport.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.teleport.Teleport;

public class HereCommand extends CommandBase<Teleport>
{
	public HereCommand(Teleport plugin)
	{
		super(plugin, Teleport.Perm.TELEPORT_OTHER_COMMAND, "here", "h");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 1)
		{
			Plugin.playerToPlayer(caller, args[0], caller.getName());
		}
		else if (args.length == 2)
		{
			Plugin.playerToPlayer(caller, args[0], args[1]);
		}
	}
}