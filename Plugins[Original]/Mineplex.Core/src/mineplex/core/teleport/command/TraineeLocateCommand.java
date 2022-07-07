package mineplex.core.teleport.command;

import org.bukkit.entity.Player;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.teleport.Teleport;

public class TraineeLocateCommand extends CommandBase<Teleport>
{
	public TraineeLocateCommand(Teleport plugin)
	{
		super(plugin, Teleport.Perm.FIND_TRAINEE_COMMAND, "tlocate", "twhere", "tfind");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.locateRank(caller, PermissionGroup.TRAINEE);
	}
}