package mineplex.mapparser.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;
import mineplex.mapparser.Parse;

public class ParseCommand extends OpCommand
{
	private static final String LOBBY_NAME = "world_lobby";

	public ParseCommand(MapParser plugin)
	{
		super(plugin, "parse");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		int radius = 200;

		// Custom radius settings
		if (args.length > 0)
		{
			try
			{
				radius = Integer.parseInt(args[0]);

				if (radius < 1)
				{
					throw new NumberFormatException("Radius cannot be less than 1.");
				}
			}
			catch (NumberFormatException ex)
			{
				message(player, "Please enter a valid radius.");
				return true;
			}

			// If there are more args, set the new args
			// to everything past the radius
			if (args.length > 1)
			{
				args = Arrays.asList(args).subList(1, args.length).toArray(new String[] {});
			}
			// Otherwise, set it to an empty array.
			else
			{
				args = new String[] {};
			}
		}

		Location parseLoc = player.getLocation();

		World world = parseLoc.getWorld();

		if (world.getName().equals(LOBBY_NAME))
		{
			message(player, "You can't parse the Lobby!");
			return true;
		}

		MapData data = getPlugin().getData(world.getName());

		if (data.MapName.equals("null") || data.MapCreator.equals("null") || data.MapGameType == null)
		{
			message(player, "Map Name/Author/GameType are not set!");
			return true;
		}

		// Teleport Players Out
		for (Player worldPlayer : world.getPlayers())
		{
			worldPlayer.teleport(getPlugin().getSpawnLocation());
			message(player, "World " + F.elem(world.getName()) + " is preparing to be parsed, so you were sent back to the lobby.");
		}

		// Unload World > Copy
		World parseableWorld = getPlugin().getWorldManager().prepMapParse(world);

		if (parseableWorld == null)
		{
			message(player, "Could not prepare world for parsing! Parse aborted.");
			return true;
		}

		//Parse the World
		getPlugin().setCurrentParse(new Parse(getPlugin(), parseableWorld, args, parseLoc, getPlugin().getData(parseLoc.getWorld().getName()), radius));

		return true;
	}
}
