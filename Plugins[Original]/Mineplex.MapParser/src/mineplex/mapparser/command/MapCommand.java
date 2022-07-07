package mineplex.mapparser.command;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayerBase;
import mineplex.mapparser.GameType;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;
import mineplex.mapparser.command.teleport.TeleportManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

/**
 * Created by Shaun on 8/15/2014.
 */
public class MapCommand extends BaseCommand
{
	private TeleportManager _teleportManager;

	public MapCommand(TeleportManager teleportManager)
	{
		super(teleportManager.getPlugin(), "map");

		setDescription("Teleport to a map");
		setUsage("/map <name> [gametype]");

		_teleportManager = teleportManager;
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if (args.length < 1)
		{
			//UtilPlayerBase.message(event.getPlayer(), F.main("Parser", "Invalid Input. " + F.elem("/map <MapName> [GameType]")));
			return false;
		}

		String worldName = null;
		// Look up maps without a specific game type
		if (args.length == 1)
		{
			List<String> possibleMaps = getPlugin().getMapsByName(args[0]);
			if (possibleMaps.size() == 0)
			{
				message(player, "No maps found with the name: " + F.elem(args[0]));
				return true;
			}
			if (possibleMaps.size() > 1)
			{
				message(player, "Found more than one possible match:");
				for (String s : possibleMaps)
					UtilPlayerBase.message(player, s);

				return true;
			}

			worldName = possibleMaps.get(0);
		}
		else // Get map with specified name and gametype
		{
			GameType gameType = null;
			try
			{
				gameType = GameType.valueOf(args[1]);
			}
			catch (Exception e)
			{
				getPlugin().sendValidGameTypes(player);
				return true;
			}

			worldName = getPlugin().getWorldString(args[0], gameType);
		}

		if (getPlugin().getMapsBeingZipped().contains(worldName))
		{
			message(player, "That map is being backed up now. Try again soon");
			return true;
		}
		
		//Delete UID 
		File file = new File(worldName + "/uid.dat");
		if (file.exists())
		{
			System.out.println("Deleting uid.dat for " + worldName);
			file.delete();
		}
		else
		{
			System.out.println("Could not delete uid.dat for " + worldName);
		}

		World world = getPlugin().getWorldFromName(worldName);

		//Error (This should not occur!)
		if (world == null)
		{
			message(player, "Null World Error: " + F.elem(worldName));
			return true;
		}

		//Permission
		if (!_teleportManager.canTeleportTo(player, world.getSpawnLocation()))
		{
			message(player, "You are not permitted to access this map.");
			return true;
		}

		//Teleport
		message(player, "Teleporting to World: " + F.elem(worldName));

		_teleportManager.teleportPlayer(player, new Location(world, 0, 106, 0));
		player.setFlying(true);

		MapData data = getPlugin().getData(worldName);

		data.sendInfo(player);
		return true;
	}
}
