package mineplex.core.common.util;

import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

public class UtilWorld
{	
	public static World getWorld(String world)
	{
		return Bukkit.getServer().getWorld(world);
	}
	
	public static boolean isInChunk(Location location, Chunk chunk)
	{
		return location.getChunk().getX() == chunk.getX() && location.getChunk().getZ() == chunk.getZ() && chunk.getWorld().equals(location.getChunk().getWorld());
	}

	public static boolean areChunksEqual(Location first, Location second)
	{
		return first.getBlockX() >> 4 == second.getBlockX() >> 4 && first.getBlockZ() >> 4 == second.getBlockZ() >> 4;
	}

	public static String chunkToStr(Chunk chunk)
	{
		if (chunk == null)
			return "";
		
		return chunkToStr(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	public static String chunkToStr(Location location)
	{
		return chunkToStr(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
	}

	public static String chunkToStr(String world, int x, int z)
	{
		return world + "," + x + "," + z;
	}
	
	public static String chunkToStrClean(Chunk chunk)
	{
		if (chunk == null)
			return "";
		
		return "(" + chunk.getX() + "," + chunk.getZ() + ")";
	}
	
	public static Chunk strToChunk(String string)
	{
		try
		{
			String[] tokens = string.split(",");
			
			return getWorld(tokens[0]).getChunkAt(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public static String blockToStr(Block block)
	{
		if (block == null)
			return "";

		return block.getWorld().getName() + "," +
				block.getX() + "," +
				block.getY() + "," +
				block.getZ();
	}

	public static String blockToStrClean(Block block)
	{
		if (block == null)
			return "";

		return  "(" + block.getX() + "," +
				block.getY() + "," +
				block.getZ() + ")";
	}

	public static Block strToBlock(String string)
	{
		if (string.length() == 0)
			return null;

		String[] parts = string.split(",");

		try
		{
			for (World cur : Bukkit.getServer().getWorlds())
			{
				if (cur.getName().equalsIgnoreCase(parts[0]))
				{
					int x = Integer.parseInt(parts[1]);
					int y = Integer.parseInt(parts[2]);
					int z = Integer.parseInt(parts[3]);
					return cur.getBlockAt(x, y, z);
				}
			}
		}
		catch (Exception e)
		{
		}

		return null;
	}

	public static String locToStr(Location loc)
	{
		if (loc == null)
			return "";
		
		return loc.getWorld().getName() + "," + 
		UtilMath.trim(1, loc.getX()) + "," + 
		UtilMath.trim(1, loc.getY()) + "," + 
		UtilMath.trim(1, loc.getZ());
	}
	
	public static String locToStrClean(Location loc)
	{
		if (loc == null)
			return "Null";
		
		return "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
	}
	
	public static String vecToStrClean(Vector loc)
	{
		if (loc == null)
			return "Null";
		
		return "(" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")";
	}
	
	public static Location strToLoc(String string)
	{
		if (string.length() == 0)
			return null;
		
		String[] tokens = string.split(",");
		
		try
		{
			for (World cur : Bukkit.getServer().getWorlds())
			{
				if (cur.getName().equalsIgnoreCase(tokens[0]))
				{
					return new Location(cur, Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3]));
				}
			}
		}
		catch (Exception e)
		{
			return null;
		}
		
		return null;
	}
	
	public static Location locMerge(Location a, Location b)
	{
		a.setX(b.getX());
		a.setY(b.getY());
		a.setZ(b.getZ());
		return a;
	}
	
	public static String envToStr(Environment env)
	{
		if (env == Environment.NORMAL)	return "Overworld";
		if (env == Environment.NETHER)	return "Nether";
		if (env == Environment.THE_END)	return "The End";
		return "Unknown";
	}

	public static World getWorldType(Environment env) 
	{
		for (World cur : Bukkit.getServer().getWorlds())
			if (cur.getEnvironment() == env)
				return cur;
		
		return null;
	}
	
	public static Location averageLocation(Collection<Location> locs)
	{
		if (locs.isEmpty())
			return null;
		
		Vector vec = new Vector(0,0,0);
		double count = 0;
		
		World world = null;

		for (Location spawn : locs)
		{				
			count++;
			vec.add(spawn.toVector());
			
			world = spawn.getWorld();
		}
		
		vec.multiply(1d/count);
		
		return vec.toLocation(world);
	}
	
	private static List<Block> branch(Location origin)
	{
		return Lists.newArrayList(origin.getBlock(),
								  origin.getBlock().getRelative(BlockFace.DOWN),
								  origin.getBlock().getRelative(BlockFace.UP),
								  origin.getBlock().getRelative(BlockFace.NORTH),
								  origin.getBlock().getRelative(BlockFace.EAST),
								  origin.getBlock().getRelative(BlockFace.SOUTH),
								  origin.getBlock().getRelative(BlockFace.WEST));
	}

	/**
	 * This method will use the World provided by the given Location.<p>
	 * @return <b>true</b> if the specified location is within the bounds of the
	 * world's set border, or <b>false</b> if {@link World#getWorldBorder()} returns null.
	 */
	public static boolean inWorldBorder(Location location)
	{
		WorldBorder border = location.getWorld().getWorldBorder();
		
		if (border == null)
		{
			return false;
		}
		
		double size = border.getSize() / 2;
		
		double maxX = size;
		double maxZ = size;
		double minX = -size;
		double minZ = -size;
		
		return location.getX() >= minX && location.getX() <= maxX && location.getZ() >= minZ && location.getZ() <= maxZ;
	}
	
	/**
	 * This method will use the World specified by the second argument, and the
	 * x, y, and z provided by the given Location.<p>
	 * @return <b>true</b> if the specified location is within the bounds of the
	 * world's set border, or <b>false</b> if {@link World#getWorldBorder()} returns null.
	 */
	public static boolean inWorldBorder(World world, Location location)
	{
		WorldBorder border = world.getWorldBorder();
		
		if (border == null)
		{
			return false;
		}
		
		double size = border.getSize() / 2;
		
		double maxX = size;
		double maxZ = size;
		double minX = -size;
		double minZ = -size;
		
		return location.getX() >= minX && location.getX() <= maxX && location.getZ() >= minZ && location.getZ() <= maxZ;
	}
	
	/**
	 * @return <b>true</b> if the specified bounding box is within the bounds of the
	 * world's set border, or <b>false</b> if {@link World#getWorldBorder()} returns null.
	 */
	public static boolean isBoxInWorldBorder(World world, Location min, Location max)
	{
		WorldBorder border = world.getWorldBorder();
		
		if (border == null)
		{
			return false;
		}
		
		double size = border.getSize() / 2;
		
		double maxX = size;
		double maxZ = size;
		double minX = -size;
		double minZ = -size;
		
		double startX = Math.min(min.getX(), max.getX());
		double startZ = Math.min(min.getZ(), max.getZ());
		double endX = Math.max(min.getX(), max.getX());
		double endZ = Math.max(min.getZ(), max.getZ());
		
		return startX >= minX && startZ <= maxX && endX >= minZ && endZ <= maxZ;
	}
}
