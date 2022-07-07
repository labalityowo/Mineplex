package mineplex.game.clans.clans.worldevent.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.worldevent.WorldEventManager;
import mineplex.game.clans.clans.worldevent.WorldEventType;
import mineplex.game.clans.clans.worldevent.api.WorldEvent;

public class StartCommand extends CommandBase<WorldEventManager>
{
	public StartCommand(WorldEventManager plugin)
	{
		super(plugin, WorldEventManager.Perm.START_EVENT_COMMAND, "start", "create");
	}
	
	@Override
	public void Execute(Player caller, String[] args)
	{
		// start specific world event type
		if (args != null && args.length == 1)
		{
			try
			{
				WorldEventType eventType = WorldEventType.valueOf(args[0]);
				WorldEvent event = Plugin.startEventFromType(eventType);
				
				if (event == null)
				{
					UtilPlayer.message(caller, F.main(Plugin.getName(), "Error whilst starting the world event you chose."));
				}
				else
				{
					UtilPlayer.message(caller, F.main(Plugin.getName(), "Started WorldEvent " + F.elem(args[0]) + "!"));
				}
			}
			catch (IllegalArgumentException e)
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Could not find a WorldEvent with the name " + F.elem(args[0]) + "! Available types:"));
				for (WorldEventType type : WorldEventType.values())
				{
					UtilPlayer.message(caller, C.cBlue + "- " + C.cGray + type.toString());
				}
			}
		}
		else
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "No World Event type specified. Available types:"));
			for (WorldEventType type : WorldEventType.values())
			{
				UtilPlayer.message(caller, C.cBlue + "- " + C.cGray + type.toString());
			}
		}
	}
}