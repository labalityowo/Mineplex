package mineplex.gemhunters.worldevent.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.gemhunters.worldevent.WorldEventModule;
import mineplex.gemhunters.worldevent.WorldEventType;

public class StartCommand extends CommandBase<WorldEventModule>
{
	public StartCommand(WorldEventModule plugin)
	{
		super(plugin, WorldEventModule.Perm.START_WORLD_EVENT_COMMAND, "start");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			caller.sendMessage(F.main(Plugin.getName(), "Starting a random world event."));
			Plugin.startRandomEvent();
			return;
		}
		
		StringBuilder nameBuilder = new StringBuilder();
		
		for (int i = 0; i < args.length; i++)
		{
			nameBuilder.append(args[i]);
			nameBuilder.append(" ");
		}
		
		String name = nameBuilder.toString().trim();
		
		for (WorldEventType eventType : WorldEventType.values())
		{
			if (name.equalsIgnoreCase(eventType.getName()))
			{
				caller.sendMessage(F.main(Plugin.getName(), "Starting the " + F.elem(eventType.getName()) + " world event."));
				Plugin.startEvent(eventType);
				return;
			}
		}
		
		caller.sendMessage(F.main(Plugin.getName(), "I wasn\'t able to find a world event by the name " + F.elem(args[0]) + ". Possible values:"));

		for (WorldEventType eventType : WorldEventType.values())
		{
			caller.sendMessage(C.cGray + "- " + F.elem(eventType.getName()));
		}
	}
}