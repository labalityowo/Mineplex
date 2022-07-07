package mineplex.clansgenerator;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.WatchdogThread;

import net.minecraft.server.v1_8_R3.BiomeBase;

public class ClansGenerator extends JavaPlugin implements Runnable, Listener
{
	private static final int MIN_X = -100;
	private static final int MIN_Z = -100;
	private static final int MAX_X = 100;
	private static final int MAX_Z = 100;
	
	private File _root;
	private File _outputDir;
	private boolean _debug = false;

	public void onEnable()
	{
		_root = new File(".");
		if (!_root.exists())
		{
			getLogger().severe("Root folder does not exist. Aborting");
			getServer().shutdown();
			return;
		}
		_outputDir = new File(_root, "output");
		if (new File(_root, "DEBUG.dat").exists())
		{
			_debug = true;
		}
		if (!_outputDir.exists())
		{
			if (_debug)
			{
				getLogger().info("Creating map output directory!");
			}
			_outputDir.mkdir();
		}
		BiomeBase.getBiomes()[BiomeBase.OCEAN.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.PLAINS.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.DESERT.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.EXTREME_HILLS.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.FOREST.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.TAIGA.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.SWAMPLAND.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.RIVER.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.HELL.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.SKY.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.FROZEN_OCEAN.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.FROZEN_RIVER.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.ICE_PLAINS.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.ICE_MOUNTAINS.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.MUSHROOM_ISLAND.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.MUSHROOM_SHORE.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.BEACH.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.DESERT_HILLS.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.FOREST_HILLS.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.TAIGA_HILLS.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.SMALL_MOUNTAINS.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.JUNGLE.id] = BiomeBase.EXTREME_HILLS;
		BiomeBase.getBiomes()[BiomeBase.JUNGLE_HILLS.id] = BiomeBase.EXTREME_HILLS;
		BiomeBase.getBiomes()[BiomeBase.JUNGLE_EDGE.id] = BiomeBase.EXTREME_HILLS;
		BiomeBase.getBiomes()[BiomeBase.DEEP_OCEAN.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.STONE_BEACH.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.COLD_BEACH.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.BIRCH_FOREST.id] = BiomeBase.BIRCH_FOREST;
		BiomeBase.getBiomes()[BiomeBase.BIRCH_FOREST_HILLS.id] = BiomeBase.BIRCH_FOREST_HILLS;
		BiomeBase.getBiomes()[BiomeBase.ROOFED_FOREST.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.COLD_TAIGA.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.COLD_TAIGA_HILLS.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.MEGA_TAIGA.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.MEGA_TAIGA_HILLS.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.EXTREME_HILLS_PLUS.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.SAVANNA.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.SAVANNA_PLATEAU.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.MESA.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.MESA_PLATEAU_F.id] = BiomeBase.FOREST;
		BiomeBase.getBiomes()[BiomeBase.MESA_PLATEAU.id] = BiomeBase.FOREST;
		WatchdogThread.doStop();
		getServer().getScheduler().runTaskTimer(this, this, 20L, 100L);
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPopulate(ChunkPopulateEvent event)
	{
		Block block;
		for (int x = 0; x < 16; x++)
		{
			for (int y = 1; y < 128; y++)
			{
				for (int z = 0; z < 16; z++)
				{
					block = event.getChunk().getBlock(x, y, z);
					if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST || block.getType() == Material.MOB_SPAWNER)
					{
						block.setType(Material.AIR);
						if (_debug)
						{
							getLogger().info("Removing dungeon pieces");
						}
						continue;
					}
					if (block.getType() == Material.LAVA)
					{
						byte data = block.getData();
						block.setTypeIdAndData(Material.WATER.getId(), data, false);
						if (_debug)
						{
							getLogger().info("Removing lava");
						}
						continue;
					}
					if (block.getType() == Material.STATIONARY_LAVA)
					{
						byte data = block.getData();
						block.setTypeIdAndData(Material.STATIONARY_WATER.getId(), data, false);
						if (_debug)
						{
							getLogger().info("Removing lava");
						}
						continue;
					}
				}
			}
		}
	}

	@EventHandler
	public void onJoin(AsyncPlayerPreLoginEvent event)
	{
		event.setLoginResult(Result.KICK_OTHER);
		event.setKickMessage("Shoo, go away");
	}

	public void run()
	{
		int nextFileId = 0;
		for (int existingFiles = 0; new File(_outputDir, "Clans_Map_" + existingFiles).exists(); existingFiles++)
		{
			nextFileId++;
		}

		getLogger().info("Generating world id " + nextFileId);
		World world = (new WorldCreator("Clans_Map_" + nextFileId)).environment(Environment.NORMAL).generateStructures(false).seed(ThreadLocalRandom.current().nextLong()).createWorld();
		world.setKeepSpawnInMemory(false);
		for (int x = MIN_X; x <= MAX_X; x++)
		{
			getLogger().info("Generating chunks for x coord " + x);
			for (int z = MIN_Z; z <= MAX_Z; z++)
			{
				world.getChunkAt(x, z).load(true);
			}
		}

		for (int x = MIN_X; x <= MAX_X; x++)
		{
			getLogger().info("Unloading chunks for x coord " + x);
			for (int z = MIN_Z; z <= MAX_Z; z++)
			{
				world.getChunkAt(x, z).unload(true, false);
			}
		}

		getLogger().info("Unloading and saving world");
		Bukkit.unloadWorld(world, true);
		getLogger().info("Finished unloading and saving world");
		try
		{
			FileUtils.moveDirectoryToDirectory(new File(_root, "Clans_Map_" + nextFileId), _outputDir, false);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		getLogger().info("Finished generating world id " + nextFileId);
		getServer().shutdown();
	}
}