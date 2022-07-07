package mineplex.mapparser.command;

import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.mapparser.MapParser;

public class FlySpeedCommand extends BaseCommand
{
	private final static float BASE_SPEED = 1F;

	public FlySpeedCommand(MapParser plugin)
	{
		super(plugin, "speed", "flyspeed");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		float newSpeed = BASE_SPEED;

		if (args.length > 0)
		{
			try
			{
				newSpeed = Float.parseFloat(args[0]);

				if (newSpeed > 10 || newSpeed < 0)
				{
					throw new NumberFormatException("Speed must be between 0 and 10.");
				}
			}
			catch (NumberFormatException ex)
			{
				message(player, "Please enter a valid speed value from " + C.cYellow + "0" + C.mBody + " to " + C.cYellow + "10");
				return true;
			}
		}

		player.setFlySpeed(newSpeed / 10);
		message(player, "Set your flight speed to " + F.elem(newSpeed));
		return true;
	}
}
