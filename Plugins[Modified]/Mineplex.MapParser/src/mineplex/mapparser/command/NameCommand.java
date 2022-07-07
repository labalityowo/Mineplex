package mineplex.mapparser.command;

import org.bukkit.World;
import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;

/**
 * Created by Shaun on 8/15/2014.
 */
public class NameCommand extends MapAdminCommand
{

	public NameCommand(MapParser plugin)
	{
		super(plugin, "name");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		World world = player.getWorld();

		if (args.length < 1)
		{
			message(player, "Invalid Input. " + F.elem("/name <MapName>"));
			return true;
		}

		if (world.getName().equals("world_lobby"))
		{
			message(player, "Cannot set name for Lobby.");
			return true;
		}

		StringBuilder mapName = new StringBuilder();
		for(String arg : args)
			mapName.append(arg).append(" ");

		mapName = new StringBuilder(mapName.toString().trim());

		MapData data = getPlugin().getData(world.getName());

		data.MapName = mapName.toString();
		data.Write();

		getPlugin().announce("Map Name for " + F.elem(world.getName()) + " set to " + F.elem(mapName.toString()) + ".");

		return true;
	}
}
