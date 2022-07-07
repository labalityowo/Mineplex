package mineplex.mapparser.command;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilWorld;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 *
 */
public class WarpCommand extends BaseCommand
{

	public WarpCommand(MapParser plugin)
	{
		super(plugin, "warp");
		setUsage("/warp <name> & /warp <set> <name>");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if (player.getWorld().getName().equals("world_lobby"))
		{
			message(player, "Cannot use warps in Lobby.");
			return true;
		}
		MapData data = getPlugin().getData(player.getWorld().getName());

		if(data == null)
		{
			player.sendMessage(C.cRed + "There was an error with your map.");
			return true;
		}

		Map<String, Location> warps = data._warps;

		if(args.length == 1)
		{
			if(args[0].equalsIgnoreCase("list"))
			{
				for(String s : warps.keySet())
				{
					player.sendMessage(F.elem(s) + " @ " + F.elem(UtilWorld.locToStrClean(warps.get(s))));
				}
				return true;
			}

			Location location = warps.get(args[0].toLowerCase());

			if(location == null){
				player.sendMessage(C.cRed + "Unknown warp!");
				return true;
			}

			player.sendMessage(C.cGray + "Warping to " + F.elem(args[0]));
			player.teleport(location);
			return true;
		}

		if(args.length == 2)
		{
			if(!args[0].equalsIgnoreCase("set"))
			{
				player.sendMessage(C.cRed + "Please use " + F.elem("/warp set <name>") + C.cRed + " to set a warp");
				return true;
			}
			String warp = args[1].toLowerCase();
			if(warps.containsKey(warp))
			{
				player.sendMessage(C.cRed + "That warp already exists!");
				return true;
			}
			warps.put(warp, player.getLocation());
			player.sendMessage(C.cGray + "Created a new warp: " + F.elem(warp));
			return true;
		}

		return false;
	}

}
