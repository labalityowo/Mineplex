package mineplex.core.common.util.worldgen;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

/**
 * A simple clean room void chunk generator
 */

public class WorldGenCleanRoom extends ChunkGenerator
{
	
	/**
	 * Creates a clean void chunk with no blocks
	 */
	@Override
	public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome)
	{
		return createChunkData(world);
	}

}
