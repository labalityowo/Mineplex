package mineplex.mapparser.command;

import java.io.File;

import org.bukkit.World;
import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.mapparser.GameType;
import mineplex.mapparser.MapParser;
import org.apache.commons.io.FileUtils;

/**
 * Created by Shaun on 8/16/2014.
 */
public class DeleteCommand extends BaseCommand
{
	public DeleteCommand(MapParser plugin)
	{
		super(plugin, "delete");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if (args.length < 2)
		{
			message(player, "Invalid Input. " + F.elem("/delete <MapName> [GameType]"));
			return true;
		}

		String mapName = args[0];
		GameType gameType = null;

		try
		{
			gameType = GameType.valueOf(args[1]);
		}
		catch (Exception e)
		{
			getPlugin().sendValidGameTypes(player);
		}

		final String worldName = getPlugin().getWorldString(mapName, gameType);

		if (!getPlugin().doesMapExist(worldName))
		{
			message(player, "Map does not exist: " + F.elem(worldName));
			return true;
		}

		if (!getPlugin().getData(worldName).HasAccess(player))
		{
			message(player, "You do not have Build-Access on this Map.");
			return true;
		}

		if (getPlugin().getMapWorld(worldName) != null)
		{
			World world = getPlugin().getMapWorld(worldName);

			//Teleport Out
			for (Player other : world.getPlayers())
				other.teleport(getPlugin().getSpawnLocation());

			//Unload World
			//Things break if this isn't set to true for saving the world
			getPlugin().getServer().unloadWorld(world, true);
		}

		//Delete
		boolean deleted = FileUtils.deleteQuietly(new File(worldName));

		if (deleted)
			getPlugin().announce("Deleted World: " + F.elem(worldName));
		else
			getPlugin().announce("Failed to delete World: " + F.elem(worldName));

		return true;
	}
}
