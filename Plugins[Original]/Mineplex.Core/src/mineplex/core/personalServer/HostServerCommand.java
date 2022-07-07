package mineplex.core.personalServer;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;

public class HostServerCommand extends CommandBase<PersonalServerManager>
{
	public HostServerCommand(PersonalServerManager plugin)
	{
		super(plugin, PersonalServerManager.Perm.MPS, "hostserver", "mps");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.hostServer(caller, caller.getName(), false);
	}
}