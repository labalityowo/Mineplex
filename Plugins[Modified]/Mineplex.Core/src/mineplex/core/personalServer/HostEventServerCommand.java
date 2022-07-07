package mineplex.core.personalServer;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.command.LoggedCommand;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;

public class HostEventServerCommand extends CommandBase<PersonalServerManager> implements LoggedCommand
{
	public HostEventServerCommand(PersonalServerManager plugin)
	{
		super(plugin, PersonalServerManager.Perm.EVENT_COMMAND, "hostevent");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (!Recharge.Instance.use(caller, "Host Event", 30000, false, false))
		{
			return;
		}
		
		if(Plugin.getClients().Get(caller).isDisguised())
		{
			UtilPlayer.message(caller, F.main("Disguise", "You can't create a event server while you are disguised!"));
			return;
		}
		Plugin.hostServer(caller, caller.getName(), true);	
	}
}