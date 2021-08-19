package mineplex.core.teleport.command;

import org.bukkit.entity.Player;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.teleport.Teleport;

public class ModLocateCommand extends CommandBase<Teleport>
{
	public ModLocateCommand(Teleport plugin)
	{
		super(plugin, Teleport.Perm.FIND_MOD_COMMAND, "mlocate", "mwhere", "mfind");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.locateRank(caller, PermissionGroup.MOD);
	}
}