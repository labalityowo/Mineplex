package mineplex.core.teleport.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.teleport.Teleport;

public class LocateCommand extends CommandBase<Teleport>
{
	public LocateCommand(Teleport plugin)
	{
		super(plugin, Teleport.Perm.FIND_COMMAND, "locate", "where", "find");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length == 0)
		{
			UtilPlayer.message(caller, F.main("Locate", "Player argument missing."));
			return;
		}
		
		Plugin.locatePlayer(caller, args[0]);
	}
}