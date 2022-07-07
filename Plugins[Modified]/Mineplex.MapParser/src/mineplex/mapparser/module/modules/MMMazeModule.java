package mineplex.mapparser.module.modules;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilGear;
import mineplex.mapparser.MapParser;
import mineplex.mapparser.module.Module;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 *
 */
public class MMMazeModule extends Module
{

	public MMMazeModule(MapParser plugin)
	{
		super("MM-Maze", plugin);
	}


	@EventHandler
	public void mmMazeParser(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

		//Permission
		if (!GetData(event.getPlayer().getWorld().getName()).HasAccess(event.getPlayer()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!UtilGear.isMat(player.getItemInHand(), Material.WEB))
			return;

		event.setCancelled(true);

		// parse

		Block clicked = event.getClickedBlock();
		Location center = clicked.getLocation();
		Location lowestCorner = center.clone().subtract(49, 0, 49);

		// 0 = air or other
		// 1 = path - quartz
		// 2 = mob spawn - gold
		// 3 = safe spawn - stone

		int[][] maze = new int[99][99];

		for (int i = 0; i < 99; i++)
			for (int j = 0; j < 99; j++)
				maze[i][j] = getMMParseValue(lowestCorner.clone().add(j, 0, i).getBlock().getType());

		//Save
		try
		{
			FileWriter fstream = new FileWriter(GetData(player.getWorld().getName()).MapFolder + File.separator + "Maze.dat");
			BufferedWriter out = new BufferedWriter(fstream);

			out.write("private static final int[][] PARSED_MAZE = {" + System.lineSeparator());
			for (int j[] : maze)
			{
				out.write("{");
				boolean first = true;
				for (int k : j)
				{
					if(!first) out.write(",");
					out.write(k + "");

					first = false;
				}
				out.write("}," + System.lineSeparator());
			}
			out.write("};" + System.lineSeparator());

			out.close();
		}
		catch (Exception e)
		{
			player.sendMessage(C.cRed + C.Bold + "MMMazeParse: " + ChatColor.RESET + "An error has occured, see console.");
			e.printStackTrace();
		}
		player.sendMessage(C.cGreen + C.Bold + "MMMazeParse: " + ChatColor.RESET + "Maze parsed.");
	}

	private int getMMParseValue(Material m)
	{
		switch (m) {
			case QUARTZ_BLOCK:
				return 1;

			case GOLD_BLOCK:
				return 2;

			case STONE:
				return 3;

			case DIRT:
				return 4;

			case COBBLESTONE:
				return 5;

			case BRICK:
				return 6;

			default:
				return 0;
		}
	}
}
