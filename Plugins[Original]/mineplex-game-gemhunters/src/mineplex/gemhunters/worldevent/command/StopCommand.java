package mineplex.gemhunters.worldevent.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.gemhunters.worldevent.WorldEvent;
import mineplex.gemhunters.worldevent.WorldEventModule;
import mineplex.gemhunters.worldevent.WorldEventState;

public class StopCommand extends CommandBase<WorldEventModule>
{
	public StopCommand(WorldEventModule plugin)
	{
		super(plugin, WorldEventModule.Perm.STOP_WORLD_EVENT_COMMAND, "stop");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			caller.sendMessage(F.main(Plugin.getName(), "Stopping all world events."));
			
			for (WorldEvent event : Plugin.getActiveEvents())
			{
				event.setEventState(WorldEventState.COMPLETE);
			}
			return;
		}
		
		for (WorldEvent event : Plugin.getActiveEvents())
		{
			if (args[0].equalsIgnoreCase(event.getEventType().name()) && event.getEventState() != WorldEventState.COMPLETE)
			{
				caller.sendMessage(F.main(Plugin.getName(), "Stopping " + F.elem(event.getEventType().name()) + "."));
				event.setEventState(WorldEventState.COMPLETE);
				return;
			}
		}
		
		caller.sendMessage(F.main(Plugin.getName(), "I wasn\'t able to find an active world event by the name " + F.elem(args[0]) + ". Possible values:"));

		for (WorldEvent event : Plugin.getActiveEvents())
		{
			caller.sendMessage(C.cGray + "- " + F.elem(event.getEventType().name()));
		}
	}
}