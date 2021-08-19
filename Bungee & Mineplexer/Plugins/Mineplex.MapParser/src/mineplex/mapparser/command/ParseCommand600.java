package mineplex.mapparser.command;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;
import mineplex.mapparser.Parse;

/**
 * Created by Shaun on 8/15/2014.
 */
public class ParseCommand600 extends BaseCommand
{
	public ParseCommand600(MapParser plugin)
	{
		super(plugin, "parse600");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if (!player.isOp())
		{
			message(player, "Only OPs can parse maps!");
			return true;
		}
		
		Location parseLoc = player.getLocation();

		World world = parseLoc.getWorld();
		
		if (world.getName().equals("world_lobby"))
		{
			message(player, "Cannot parse Lobby.");
			return true;
		}

		MapData data = getPlugin().getData(world.getName());

		if (data.MapName.equals("null") || data.MapCreator.equals("null") || data.MapGameType.equals("null"))
		{
			message(player, "Map Name/Author/GameType are not set!");
			return true;
		}

		//Teleport Players Out
		for (Player worldPlayer : world.getPlayers())
		{
			worldPlayer.teleport(getPlugin().getSpawnLocation());
			message(player, "World " + F.elem(world.getName()) + " is preparing to be parsed.");
		}

		//Unload World > Copy
		World parseableWorld = getPlugin().getWorldManager().prepMapParse(world);

		if (parseableWorld == null)
		{
			message(player, "Could not prepare world for parsing!");
			return true;
		}

		//Parse the World
		getPlugin().setCurrentParse(new Parse(getPlugin(), parseableWorld, args, parseLoc, getPlugin().getData(parseLoc.getWorld().getName()), 600));

		return true;
	}
}
