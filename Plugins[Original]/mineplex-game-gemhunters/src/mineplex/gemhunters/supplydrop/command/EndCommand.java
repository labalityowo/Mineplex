package mineplex.gemhunters.supplydrop.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.gemhunters.supplydrop.SupplyDropModule;

public class EndCommand extends CommandBase<SupplyDropModule>
{
	public EndCommand(SupplyDropModule plugin)
	{
		super(plugin, SupplyDropModule.Perm.STOP_SUPPLY_DROP_COMMAND, "stop");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (!Plugin.isActive())
		{
			caller.sendMessage(F.main(Plugin.getName(), "There is no current supply drop."));
			return;
		}
		
		caller.sendMessage(F.main(Plugin.getName(), "Stopping the current supply drop."));
		Plugin.stopSequence();
	}
}