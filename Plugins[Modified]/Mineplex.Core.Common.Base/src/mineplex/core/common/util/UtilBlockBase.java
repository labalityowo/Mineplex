package mineplex.core.common.util;

import java.util.ArrayList;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class UtilBlockBase
{

	public static ArrayList<Block> getSurrounding(Block block, boolean diagonals)
	{
		ArrayList<Block> blocks = new ArrayList<Block>();
		
		if (diagonals)
		{
			for (int x = -1; x <= 1; x++)
				for (int z = -1; z <= 1; z++)
					for (int y = 1; y >= -1; y--)
					{
						if (x == 0 && y == 0 && z == 0) continue;
						
						blocks.add(block.getRelative(x, y, z));
					}
		}
		else
		{
			blocks.add(block.getRelative(BlockFace.UP));
			blocks.add(block.getRelative(BlockFace.NORTH));
			blocks.add(block.getRelative(BlockFace.SOUTH));
			blocks.add(block.getRelative(BlockFace.EAST));
			blocks.add(block.getRelative(BlockFace.WEST));
			blocks.add(block.getRelative(BlockFace.DOWN));
		}
		
		return blocks;
	}
}
