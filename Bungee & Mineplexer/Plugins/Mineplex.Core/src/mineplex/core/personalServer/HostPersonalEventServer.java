package mineplex.core.personalServer;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.command.LoggedCommand;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;

public class HostPersonalEventServer extends CommandBase<PersonalServerManager> implements LoggedCommand
{
	public HostPersonalEventServer(PersonalServerManager plugin)
	{
		super(plugin, PersonalServerManager.Perm.PERSONAL_EVENT_COMMAND, "hostmes");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.hostServer(caller, caller.getName(), false, true);	
	}
}