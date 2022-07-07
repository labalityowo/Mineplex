package mineplex.mapparser.command;

import java.io.File;

import org.bukkit.World;
import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;

/**
 * Created by Shaun on 8/16/2014.
 */
public class RenameCommand extends BaseCommand
{
	public RenameCommand(MapParser plugin)
	{
		super(plugin, "rename");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		World world = player.getWorld();
		
		if (world.getName().equals("world_lobby"))
		{
			message(player, "Cannot rename Lobby.");
			return true;
		}
		
		MapData data = getPlugin().getData(world.getName());

		if (data == null)
		{
			message(player, "Map not found: " + F.elem(args[0]));
			return true;
		}
		else if (!data.CanRename(player))
		{
			message(player, "You do not have access to rename this map");
			return true;
		}
		else if (args.length != 1)
		{
			message(player, "Usage: /rename <new name>");
			return true;
		}

		String newName = args[0];

		for (Player other : world.getPlayers())
		{
			other.teleport(getPlugin().getSpawnLocation());
			message(other, "Unloading world for rename...");
		}
		getPlugin().getServer().unloadWorld(world, true);
		message(player, "World unloaded!");


		File mapFolder = new File(world.getName());
		File newFolder = new File("map" + File.separator + data.MapGameType.GetName() + File.separator + newName);
		mapFolder.renameTo(newFolder);

		message(player, "Map " + world.getName() + " renamed to " + newFolder.getName());

		return true;
	}
}
