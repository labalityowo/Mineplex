package nautilus.game.pvp.worldevent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class EventTerrainFinder
{
	public EventManager Manager;

	public EventTerrainFinder(EventManager manager) 
	{
		Manager = manager;
	}

	public double PosNeg()
	{
		if (Math.random() > 0.5)
			return -1;
		return 1;
	}

	public Location FindArea(World world, int size, int vert)
	{
		for (int i=0 ; i<20 ; i++)
		{
			int x = 0;
			int z = 0;

			//X Side
			if (Math.random() > 0.5)
			{
				x = (int) (PosNeg() * ((400 + size) + UtilMath.r(200 - (size*2))));
				z = (int) (PosNeg() * UtilMath.r(600 - size));
			}
			//Z Side
			else
			{
				z = (int) (PosNeg() * ((400 + size) + UtilMath.r(200 - (size*2))));
				x = (int) (PosNeg() * UtilMath.r(600 - size));
			}

			Location loc = UtilBlock.getHighest(world, x, z).getLocation();

			int total = ((size*2)+1)*((size*2)+1);

			int liquid = 0;			
			HashMap<Integer, Integer> heights = new HashMap<Integer, Integer>();

			HashSet<Chunk> chunks = new HashSet<Chunk>();
			boolean wilderness = true;

			//Gather Data
			for (x=-size ; x<=size && wilderness ; x++)
				for (z=-size ; z<=size && wilderness ; z++)
				{
					Block block = UtilBlock.getHighest(world, loc.getBlockX()+x, loc.getBlockZ()+z);

					if (!chunks.contains(block.getChunk()))
						if (Manager.Clans().CUtil().isClaimed(block.getLocation()))
						{
							chunks.add(block.getChunk());
							wilderness = false;
							break;
						}

					//Liquid
					if (block.getRelative(BlockFace.DOWN).isLiquid() || block.isLiquid())
						liquid++;

					//Height
					int heightDiff = block.getY() - loc.getBlockY();
					if (!heights.containsKey(heightDiff))		heights.put(heightDiff, 1);
					else										heights.put(heightDiff, heights.get(heightDiff) + 1);
				}

			if (!wilderness)
				continue;

			//Too Watery
			if ((double)liquid/(double)total > 0.25)
				continue;

			//Too Height Variable
			int withinHeight = 0;
			for (int h=-vert ; h<=vert ; h++)
			{
				if (!heights.containsKey(h))
					continue;

				withinHeight += heights.get(h);
			}

			if ((double)withinHeight/(double)total < 0.9)
				continue; 

			//Success
			return loc;
		}

		return null;
	}

	public Location LocateSpace(Location areaSource, int areaRadius, int xArea, int yArea, int zArea, boolean replaceBlocks, boolean aboveOther, Set<Block> otherBlock)
	{
		for (int i=0 ; i<20 ; i++)
		{
			int x = UtilMath.r(areaRadius*2) - areaRadius + areaSource.getBlockX();
			int z = UtilMath.r(areaRadius*2) - areaRadius + areaSource.getBlockZ();

			Block block = UtilBlock.getHighest(areaSource.getWorld(), x, z);

			if (!aboveOther)
				if (otherBlock.contains(block.getRelative(BlockFace.DOWN)))
					continue;

			boolean valid = true;

			int overlaps = 0;

			//Previous
			for (x=-xArea ; x<=xArea ; x++)
			{
				for (z=-zArea ; z<=zArea ; z++)
				{		
					for (int y=0 ; y<=yArea ; y++)
					{
						//Check Blocks
						Block cur = areaSource.getWorld().getBlockAt(block.getX()+x, block.getY()+y, block.getZ()+z);

						if (cur.getRelative(BlockFace.DOWN).isLiquid())
						{
							valid = false;
							break;
						}

						if (otherBlock.contains(cur))
						{
							valid = false;
							break;
						}

						//Check Area
						if (!UtilBlock.airFoliage(cur))
							overlaps += 1;	
					}

					if (!valid)
						break;
				}

				if (!valid)
					break;
			}

			if (!replaceBlocks && overlaps > 0)
				continue;

			if (!valid)
				continue;				

			return block.getLocation();
		}

		return null;
	}
}
