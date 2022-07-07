package nautilus.game.minekart.track;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class TrackProcessor
{
	private Location progressStart = null;
	private ArrayList<Location> progress = new ArrayList<Location>();	
	private ArrayList<Location> spawns = new ArrayList<Location>();
	private ArrayList<Location> returns = new ArrayList<Location>();
	private ArrayList<Location> items = new ArrayList<Location>();
	private HashMap<Location, String> mobs = new HashMap<Location, String>();
	private HashMap<Location, String> jumps = new HashMap<Location, String>();

	private ArrayList<Location> spawnsOrdered = new ArrayList<Location>();
	private ArrayList<Location> progressOrdered = new ArrayList<Location>();

	private int spawnsDisplayIndex = 0;
	private int progressDisplayIndex = 0;

	public void ProcessTrack(Player caller) 
	{
		progressStart = null;
		progress.clear();
		spawns.clear();
		returns.clear();
		items.clear();
		
		mobs.clear();
		jumps.clear();

		progressOrdered.clear();
		spawnsOrdered.clear();

		int processed = 0;

		caller.sendMessage("Scanning for Blocks...");
		for (int x=-500 ; x < 500 ; x++)
			for (int z=-500 ; z < 500 ; z++)
				for (int y=0 ; y < 256 ; y++)
				{
					processed++;
					if (processed % 10000000 == 0)
						caller.sendMessage("Processed: " + processed);

					Block block = caller.getWorld().getBlockAt(caller.getLocation().getBlockX()+x, caller.getLocation().getBlockY()+y, caller.getLocation().getBlockZ()+z);


					if (block.getType() == Material.SIGN_POST)
					{
						Block type = block.getRelative(BlockFace.DOWN);
						if (type == null)
							continue;

						BlockState state = block.getState();

						Sign sign = (Sign)state;

						String lineA = sign.getLine(0);
						String lineB = sign.getLine(1);

						if (type.getType() == Material.WOOL && lineA.equals("MOB"))
						{
							if (type.getData() == 14)	// red > mob
							{
								mobs.put(type.getLocation().add(0.5, 1.5, 0.5), lineB);

								//Remove Blocks
								block.setTypeId(0);
								type.setTypeId(0);
							}
						}

						else if (type.getType() == Material.EMERALD_BLOCK && lineA.equals("JUMP"))
						{
							jumps.put(type.getLocation(), lineB);
							
							//Remove Blocks
							block.setTypeId(0);
						}
					}

					if (block.getTypeId() != 148)
						continue;

					Block type = block.getRelative(BlockFace.DOWN);
					if (type == null)
						continue;

					if (type.getType() != Material.WOOL)
						continue;

					if (type.getData() == 1)		// orange > progress start
					{
						if (progressStart != null)
							caller.sendMessage("Error: Duplicate Progress Start");

						progressStart = type.getLocation().add(0.5, 0, 0.5);
					}
					else if (type.getData() == 4)	// yellow > progress
					{
						progress.add(type.getLocation().add(0.5, 0, 0.5));
					}
					else if (type.getData() == 5)	// green > items
					{
						items.add(type.getLocation().add(0.5, 0, 0.5));
					}
					else if (type.getData() == 11)	// blue > spawns
					{
						spawns.add(type.getLocation().add(0.5, 0, 0.5));
					}
					else if (type.getData() == 10)	// purple > return
					{
						returns.add(type.getLocation().add(0.5, 0, 0.5));
					}
					else
					{
						continue;
					}

					//Remove Blocks
					block.setTypeId(0);
					type.setTypeId(0);
				}

		caller.sendMessage("Ordering Progress Blocks...");

		if (progressStart == null)
		{
			caller.sendMessage("Error: No Progress Start Found (Orange Wool)");
			return;
		}

		//Order Progress
		progressOrdered.add(progressStart);

		while (!progress.isEmpty())
		{
			Location cur = progressOrdered.get(progressOrdered.size() - 1);
			Location next = null;
			double dist = 9999;

			for (Location loc : progress)
			{
				double newDist = UtilMath.offset(cur, loc);

				if (next == null)
				{
					next = loc;
					dist = newDist;
				}

				else if (newDist < dist)
				{
					next = loc;
					dist = newDist;
				}
			}

			progress.remove(next);
			progressOrdered.add(next);
		}

		//Order Spawns
		while (!spawns.isEmpty())
		{
			Location spawn = null;
			double dist = 9999;

			for (Location loc : spawns)
			{
				double newDist = UtilMath.offset(progressStart, loc);

				if (spawn == null)
				{
					spawn = loc;
					dist = newDist;
				}

				else if (newDist < dist)
				{
					spawn = loc;
					dist = newDist;
				}
			}

			spawns.remove(spawn);
			spawnsOrdered.add(spawn);
		}

		//Lakitu
		if (returns.isEmpty())
			returns = progressOrdered;

		//Save
		try
		{
			FileWriter fstream = new FileWriter(caller.getWorld().getName() + File.separatorChar + "TrackInfo.dat");
			BufferedWriter out = new BufferedWriter(fstream);

			out.write("TRACK_NAME:");
			out.write("\n");
			out.write("\n");
			out.write("MIN_X:");
			out.write("\n");
			out.write("MAX_X:");
			out.write("\n");
			out.write("MIN_Z:");
			out.write("\n");
			out.write("MAX_Z:");
			out.write("\n");
			out.write("\n");
			out.write("ROAD_TYPES:");
			out.write("\n");
			out.write("RETURN_TYPES:");
			out.write("\n");
			out.write("\n");
			out.write("SPAWN_DIRECTION:0");
			out.write("\n");
			out.write("SPAWNS:" + LocationsToString(spawnsOrdered));
			out.write("\n");
			out.write("\n");
			out.write("PROGRESS:" + LocationsToString(progressOrdered));
			out.write("\n");
			out.write("\n");
			out.write("RETURNS:" + LocationsToString(returns));
			out.write("\n");
			out.write("\n");
			out.write("ITEMS:" + LocationsToString(items));
			out.write("\n");
			out.write("\n");
			out.write("CREATURES:" + LocationSignsToString(mobs));
			out.write("\n");
			out.write("\n");
			out.write("JUMPS:" + LocationSignsToString(jumps));

			out.close();
		}
		catch (Exception e)
		{
			caller.sendMessage("Error: File Write Error");
		}


		caller.sendMessage("Track Data Saved.");
	}	

	public String LocationsToString(ArrayList<Location> locs)
	{
		String out = "";

		for (Location loc : locs)
			out += loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ":";

		return out;
	}
	
	public String LocationSignsToString(HashMap<Location, String> locs)
	{
		String out = "";

		for (Location loc : locs.keySet())
			out += locs.get(loc) + "@" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ":";

		return out;
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (!progressOrdered.isEmpty())
		{
			if (progressDisplayIndex >= progressOrdered.size())
				progressDisplayIndex = 0;

			Location loc = progressOrdered.get(progressDisplayIndex);
			loc.getBlock().getWorld().playEffect(loc.getBlock().getLocation(), Effect.STEP_SOUND, 41);

			progressDisplayIndex++;
		}

		if (!spawnsOrdered.isEmpty())
		{
			if (spawnsDisplayIndex >= spawnsOrdered.size())
				spawnsDisplayIndex = 0;

			Location loc = spawnsOrdered.get(spawnsDisplayIndex);
			loc.getBlock().getWorld().playEffect(loc.getBlock().getLocation(), Effect.STEP_SOUND, 22);

			spawnsDisplayIndex++;
		}

		for (Location loc : returns)
			loc.getBlock().getWorld().playEffect(loc.getBlock().getLocation(), Effect.STEP_SOUND, 90);

		for (Location loc : items)
			loc.getBlock().getWorld().playEffect(loc.getBlock().getLocation(), Effect.STEP_SOUND, 18);
	}
}

