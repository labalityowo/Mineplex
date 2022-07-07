package mineplex.mapparser.command.teleport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayerBase;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;
import mineplex.mapparser.command.BaseCommand;

public class TeleportCommand extends BaseCommand
{
	private final static List<String> COMMANDS = Arrays.asList(
		"/tp <player> - Teleport to another player",
		"/tp <from> <destination> - Teleport the from player to the destination player",
		"/tp <x> <y> <z> - Teleport to coordinates"
	);
	private final static String COORDINATE_FORMAT = "%.2f";
	private TeleportManager _teleportManager;

	public TeleportCommand(TeleportManager teleportManager)
	{
		super(teleportManager.getPlugin(), "tp", "teleport");

		_teleportManager = teleportManager;
	}

	private static String formatCoordinate(double in)
	{
		return String.format(COORDINATE_FORMAT, in);
	}

	private static Double parseCoordinate(String in, double beginning)
	{
		try
		{
			if (in.startsWith("~"))
			{
				if (in.length() == 1)
				{
					return beginning;
				}

				String relativeIn = in.substring(1);

				double relative = Double.parseDouble(relativeIn);

				return beginning + relative;
			}

			return Double.parseDouble(in);
		}
		catch (NumberFormatException ex)
		{
			return null;
		}
	}

	private void help(Player player)
	{
		player.sendMessage(F.main(getPlugin().getName(), "Teleport command usage:"));
		COMMANDS.stream().map(c ->
		{
			List<String> parts = Arrays.stream(c.split("\\s*-\\s*")).collect(Collectors.toList());

			String commandMessage = "  ";

			commandMessage += C.cYellow + parts.get(0);
			commandMessage += C.cGray + " - ";
			commandMessage += C.cGold + parts.get(1);

			return commandMessage;
		}).forEach(player::sendMessage);
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if (args.length == 1)
		{
			Player target = UtilPlayerBase.searchOnline(player, args[0], true);

			if (target == null)
			{
				// Player has already been informed
				return true;
			}

			if (!_teleportManager.canTeleportTo(player, target.getLocation()))
			{
				message(player, "That map is currently locked, and you don't have access to enter it.");
				return true;
			}

			message(player, "You teleported to " + F.name(target.getName()) + C.mBody + ".");
			_teleportManager.teleportPlayer(player, target);
		}
		// Teleport player to player
		else if (args.length == 2)
		{
			// They must be OP to teleport a player to another player
			if (!player.isOp())
			{
				message(player, "You don't have permission to teleport players to other players.");
				return true;
			}

			Player sending = UtilPlayerBase.searchOnline(player, args[0], true);

			if (sending == null)
			{
				return true;
			}

			Player destination = UtilPlayerBase.searchOnline(player, args[1], true);

			if (destination == null)
			{
				return true;
			}

			message(player, "You teleported " + F.name(sending.getName()) + " to " + F.name(destination.getName()) + C.mBody + ".");
			_teleportManager.teleportPlayer(sending, destination);
		}
		// Teleport to coordinates...
		else if (args.length == 3)
		{
			List<Double> coordinates = new ArrayList<>();

			coordinates.add(parseCoordinate(args[0], player.getLocation().getX()));
			coordinates.add(parseCoordinate(args[1], player.getLocation().getY()));
			coordinates.add(parseCoordinate(args[2], player.getLocation().getZ()));

			for (Double coordinate : coordinates)
			{
				if (coordinate == null)
				{
					message(player, "Hmm, those coordinates don't look quite right.");
					help(player);
					return true;
				}
			}

			Location destination = player.getLocation().clone();
			destination.setX(coordinates.get(0));
			destination.setY(coordinates.get(1));
			destination.setZ(coordinates.get(2));

			message(player, "You teleported to ("
					+ F.name(formatCoordinate(destination.getX()))
					+ C.mBody + ", "
					+ F.name(formatCoordinate(destination.getY()))
					+ C.mBody + ", "
					+ F.name(formatCoordinate(destination.getZ()))
					+ C.mBody + ").");
			_teleportManager.teleportPlayer(player, destination);
		}
		else
		{
			message(player, "Hmm, your command doesn't look quite right.");
			help(player);
		}

		return true;
	}
}
