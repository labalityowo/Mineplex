package mineplex.mapparser.command;

import org.bukkit.World;
import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;

/**
 * Created by Shaun on 8/15/2014.
 */
public class AuthorCommand extends MapAdminCommand
{
	public AuthorCommand(MapParser plugin)
	{
		super(plugin, "author");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		World world = player.getWorld();

		if (args.length < 1)
		{
			message(player, "Invalid Input. " + F.elem("/author <MapAuthor>"));
			return true;
		}

		StringBuilder authorName = new StringBuilder();
		for (String arg : args)
			authorName.append(arg).append(" ");
		authorName = new StringBuilder(authorName.toString().trim());

		if (world.getName().equals("world_lobby"))
		{
			message(player, "Cannot set author for Lobby.");
			return true;
		}

		MapData data = getPlugin().getData(world.getName());

		data.MapCreator = authorName.toString();
		data.Write();

		getPlugin().announce("Map Author for " + F.elem(world.getName()) + " set to " + F.elem(authorName.toString()) + ".");

		return true;
	}
}
