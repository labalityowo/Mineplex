package mineplex.gemhunters.supplydrop.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.gemhunters.supplydrop.SupplyDropModule;

public class StartCommand extends CommandBase<SupplyDropModule>
{
	public StartCommand(SupplyDropModule plugin)
	{
		super(plugin, SupplyDropModule.Perm.START_SUPPLY_DROP_COMMAND, "start");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		boolean override = false;
		
		if (Plugin.isActive())
		{			
			for (String arg : args)
			{
				if (arg.equalsIgnoreCase("-f"))
				{
					override = true;
					caller.sendMessage(F.main(Plugin.getName(), "Overriding the current supply drop. You know best."));
					Plugin.stopSequence();
					break;
				}
			}
			
			if (!override)
			{
				caller.sendMessage(F.main(Plugin.getName(), "Just saying there is another supply drop already running. If you really really want to override the current one. Add " + F.elem("-f") + " as an additional argument."));
				return;
			}
		}
				
		if (args.length == 0 || override && args.length == 1)
		{
			caller.sendMessage(F.main(Plugin.getName(), "Starting the supply drop sequence at one of the random locations."));
			Plugin.startSequence();
		}
		else
		{
			StringBuilder inputBuilder = new StringBuilder();
			
			for (int i = 0; i < args.length; i++)
			{
				inputBuilder.append(args[i]);
				inputBuilder.append(" ");
			}

			String input = inputBuilder.toString().trim();
			
			for (String key : Plugin.getLocationKeys())
			{
				if (input.equalsIgnoreCase(key))
				{
					caller.sendMessage(F.main(Plugin.getName(), "Starting the supply drop sequence at " + F.elem(key) + "."));
					Plugin.startSequence(key);
					return;
				}
			}

			caller.sendMessage(F.main(Plugin.getName(), "I wasn\'t able to find a location key of the name " + F.elem(input) + ". Possible values:"));

			for (String key : Plugin.getLocationKeys())
			{
				caller.sendMessage(C.cGray + "- " + F.elem(key));
			}
		}
	}
}