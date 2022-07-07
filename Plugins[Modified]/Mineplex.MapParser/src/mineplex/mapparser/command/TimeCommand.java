package mineplex.mapparser.command;

import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.mapparser.MapParser;

public class TimeCommand extends BaseCommand
{

	public TimeCommand(MapParser plugin)
	{
		super(plugin, "time");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if (args.length == 0)
		{
			player.resetPlayerTime();

			message(player, "Reset your local player time.");
			return true;
		}

		long time;
		try
		{
			time = Long.parseLong(args[0]);

			if (time < 0)
			{
				throw new NumberFormatException("Time must be positive");
			}
		}
		catch (NumberFormatException ex)
		{
			message(player, "Please enter a valid value for time.");
			return true;
		}

		player.setPlayerTime(time, false);
		message(player, "Your local player time has been updated to " + C.cYellow + time + C.mBody + ".");
		return true;
	}
}
