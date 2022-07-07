package mineplex.mapparser.command;

import java.io.File;

import org.bukkit.World;
import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.mapparser.GameType;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;

/**
 * Created by Shaun on 8/16/2014.
 */
public class GameTypeCommand extends BaseCommand
{
	public GameTypeCommand(MapParser plugin)
	{
		super(plugin, "gametype");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		World world = player.getWorld();

		if (args.length != 1)
		{
			message(player, "Invalid Input. " + F.elem("/gametype <GameType>"));
			return true;
		}

		if (world.getName().equals("world_lobby"))
		{
			message(player, "Cannot set GameType for Lobby.");
			return true;
		}

		//Permission
		if (!getPlugin().getData(world.getName()).HasAccess(player))
		{
			message(player, "You do not have Build-Access on this Map.");
			return true;
		}

		//Check Gametype
		GameType type = null;
		try
		{
			type = GameType.valueOf(args[0]);
		}
		catch (Exception ex)
		{
			getPlugin().sendValidGameTypes(player);
			return true;
		}

		if (getPlugin().doesMapExist(getPlugin().getShortWorldName(world.getName()), type))
		{
			message(player, "A world with the same name already exists for the new gametype: " + type.GetName());
			return true;
		}

		// Rename world
		for (Player other : world.getPlayers())
		{
			other.teleport(getPlugin().getSpawnLocation());
			message(player, "Unloading world for rename...");
		}
		getPlugin().getServer().unloadWorld(world, true);

		File typeFolder = new File("map/" + type.GetName());
		if (!typeFolder.exists())
			typeFolder.mkdir();

		File mapFolder = new File(world.getName());
		String newName = "map/" + type.GetName() + "/" + getPlugin().getShortWorldName(world.getName());
		File newFolder = new File(newName);
		mapFolder.renameTo(newFolder);

		message(player, "Map " + world.getName() + " renamed to " + newName);


		MapData data = getPlugin().getData(newName);
		data.MapGameType = type;
		data.Write();

		getPlugin().announce("GameType for " + F.elem(newName) + " set to " + F.elem(args[0]) + ".");

		return true;
	}
}
