package mineplex.mapparser.command.teleport;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.mapparser.command.BaseCommand;

public class BackCommand extends BaseCommand
{
	private TeleportManager _teleportManager;

	public BackCommand(TeleportManager teleportManager)
	{
		super(teleportManager.getPlugin(), "back", "tpback");

		_teleportManager = teleportManager;
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		LinkedList<Pair<Vector, String>> teleportHistory = _teleportManager.getTeleportHistory(player);

		if (teleportHistory == null || teleportHistory.isEmpty())
		{
			message(player, "You don't have any teleport history.");
			return true;
		}

		int steps = 1;

		if (args.length > 0)
		{
			try
			{
				steps = Integer.parseInt(args[0]);

				if (steps < 1)
				{
					throw new NumberFormatException("Steps must be >0");
				}
			}
			catch (NumberFormatException ex)
			{
				message(player, "Please enter a valid number of steps to teleport back.");
				return true;
			}
		}

		Pair<Vector, String> locationPair = null;

		if (steps == 1)
		{
			locationPair = teleportHistory.removeFirst();
		}
		else
		{
			for (int i = 0; i < steps; i++)
			{
				locationPair = teleportHistory.remove(i);
			}
		}

		if (locationPair == null)
		{
			message(player, "Something went wrong. Couldn't teleport you back.");
			return true;
		}

		World locationWorld =_teleportManager.getPlugin().getWorldFromName(locationPair.getRight());
		Vector locationVec = locationPair.getLeft();
		Location location = new Location(locationWorld, locationVec.getX(), locationVec.getY(), locationVec.getZ());

		if (!_teleportManager.canTeleportTo(player, location))
		{
			message(player, "You don't have access to teleport back to that location.");
			return true;
		}

		player.teleport(location);
		message(player, "You undid your last " + ((steps == 1) ? "teleport." : F.count(steps) + C.mBody + " teleports."));
		return true;
	}
}
