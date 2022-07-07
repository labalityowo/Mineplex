package nautilus.game.arcade.uhc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import net.minecraft.server.v1_8_R3.BiomeBase;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.WatchdogThread;
import org.zeroturnaround.zip.ByteSource;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.IOUtils;

public class WorldGen extends JavaPlugin implements Listener
{
	// The world will be -MAP_SIZE to MAP_SIZE large
	private static final int MAP_SIZE = 1000;
	private static final int VIEW_DISTANCE = 5;

	private static final String API_HOST_FILE = "api-config.dat";
	private static final Map<String, String> API_HOST_MAP = new HashMap<>();

	static
	{
		try
		{
			File configFile = new File(API_HOST_FILE);
			YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);

			for (String key : configuration.getKeys(false))
			{
				String ip = configuration.getConfigurationSection(key).getString("ip");
				// Use parseInt to catch non-ints instead of a 0
				int port = Integer.parseInt(configuration.getConfigurationSection(key).getString("port"));
				if (ip == null)
				{
					throw new NullPointerException();
				}

				API_HOST_MAP.put(key, ip + ":" + port);
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	@EventHandler
	public void login(AsyncPlayerPreLoginEvent event)
	{
		event.setKickMessage("get out");
		event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
	}

	@EventHandler
	public void unload(ChunkUnloadEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void init(WorldInitEvent event)
	{
		// Prevent any eager generation
		event.getWorld().setKeepSpawnInMemory(false);
	}

	@Override
	public void onEnable()
	{
		getLogger().info("Cleaning up other worlds");
		for (World world : getServer().getWorlds())
		{
			world.setKeepSpawnInMemory(false);
			world.setSpawnFlags(false, false);
			world.setAmbientSpawnLimit(0);
			world.setAnimalSpawnLimit(0);
			world.setMonsterSpawnLimit(0);
			world.setWaterAnimalSpawnLimit(0);
			world.getEntities().forEach(Entity::remove);
			for (Chunk chunk : world.getLoadedChunks())
			{
				chunk.unload(false, false);
			}
			getServer().unloadWorld(world, false);
			getLogger().info("Unloaded " + world.getName());
		}

		getLogger().info("Replacing biomes");
		BiomeBase.getBiomes()[BiomeBase.OCEAN.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.DEEP_OCEAN.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.SWAMPLAND.id] = BiomeBase.PLAINS;
		BiomeBase.getBiomes()[BiomeBase.RIVER.id] = BiomeBase.PLAINS;

		getLogger().info("Forcing system GC");
		System.gc();

		WatchdogThread.doStop();

		getServer().getPluginManager().registerEvents(this, this);

		File root = new File(".");

		if (!root.exists())
		{
			getLogger().severe("Root folder does not exist. Aborting");
			System.exit(0);
			return;
		}

		File outputDirectory = new File(root, "output");
		if (!outputDirectory.exists())
		{
			if (!outputDirectory.mkdir())
			{
				getLogger().severe("Could not create output folder. Aborting");
				System.exit(0);
				return;
			}
		}

		long seed = ThreadLocalRandom.current().nextLong();

		File outputFile = new File(outputDirectory, "UHC_Map" + seed + ".zip");

		if (outputFile.exists())
		{
			getLogger().info("Seed " + seed + " has already been generated. Skipping");
			System.exit(0);
			return;
		}

		try
		{
			if (!outputFile.createNewFile())
			{
				getLogger().severe("Could not create new output file. Aborting");
				System.exit(0);
				return;
			}
		}
		catch (IOException e)
		{
			getLogger().log(Level.SEVERE, "Could not create new output file. Aborting", e);
			System.exit(0);
			return;
		}

		File previousSession = new File("generating");

		if (previousSession.exists())
		{
			if (!FileUtils.deleteQuietly(previousSession))
			{
				getLogger().severe("Could not delete previous generation session. Aborting");
				System.exit(0);
				return;
			}
		}

		getLogger().info("Generating world seed " + seed);

		World world = new WorldCreator("generating")
				.environment(World.Environment.NORMAL)
				.seed(seed)
				.createWorld();
		world.setKeepSpawnInMemory(false);
		world.setDifficulty(Difficulty.HARD);
		WorldBorder border = world.getWorldBorder();
		border.setCenter(0.0, 0.0);
		border.setSize(MAP_SIZE * 2);

		int minChunkX = (-MAP_SIZE >> 4) - VIEW_DISTANCE;
		int minChunkZ = (-MAP_SIZE >> 4) - VIEW_DISTANCE;
		int maxChunkX = (MAP_SIZE >> 4) + VIEW_DISTANCE;
		int maxChunkZ = (MAP_SIZE >> 4) + VIEW_DISTANCE;

		net.minecraft.server.v1_8_R3.WorldServer nmsWorld = ((CraftWorld) world).getHandle();
//
//			Field mfield = nmsWorld.getClass().getDeclaredField("M");
//			mfield.setAccessible(true);
//
//			HashTreeSet<NextTickListEntry> treeSet = ((HashTreeSet) mfield.get(nmsWorld));

		for (int x = minChunkX; x <= maxChunkX; x++)
		{
			getLogger().info("Generating x coord " + x);
			for (int z = minChunkZ; z <= maxChunkZ; z++)
			{
				world.getChunkAt(x, z).load(true);
				nmsWorld.a(true);
				// Manually tick blocks - this should be the equivalent of letting a full server tick run once
				// between each chunk generation, except we cut out the extra useless stuff
			}

//				System.out.println("M: " + treeSet.size());
//				System.out.println("E: " + nmsWorld.entityList.size());
//				System.out.println("TE: " + nmsWorld.tileEntityList.size());
//				System.out.println("C: " + nmsWorld.chunkProviderServer.chunks.size());
		}

		for (int x = minChunkX; x <= maxChunkX; x++)
		{
			getLogger().info("Unloading x coord " + x);
			for (int z = minChunkZ; z <= maxChunkZ; z++)
			{
				world.getChunkAt(x, z).unload(true, false);
			}

//				System.out.println("M: " + treeSet.size());
//				System.out.println("E: " + nmsWorld.entityList.size());
//				System.out.println("TE: " + nmsWorld.tileEntityList.size());
//				System.out.println("C: " + nmsWorld.chunkProviderServer.chunks.size());
		}

		getLogger().info("Unloading and saving world");

		Bukkit.unloadWorld(world, true);

		getLogger().info("Finished unloading and saving world");

		StringBuilder worldconfig = new StringBuilder();
		worldconfig.append("MAP_NAME:UHC World").append(System.lineSeparator());
		worldconfig.append("MAP_AUTHOR:Mineplex").append(System.lineSeparator());
		worldconfig.append("MIN_X:").append(-MAP_SIZE).append(System.lineSeparator());
		worldconfig.append("MIN_Z:").append(-MAP_SIZE).append(System.lineSeparator());
		worldconfig.append("MAX_X:").append(MAP_SIZE).append(System.lineSeparator());
		worldconfig.append("MAX_Z:").append(MAP_SIZE).append(System.lineSeparator());
		for (int i = 1; i <= 60; i++)
		{
			worldconfig.append("TEAM_NAME:").append(i).append(System.lineSeparator());
			worldconfig.append("TEAM_SPAWNS:0,0,0").append(System.lineSeparator());
		}

		File worldFolder = new File(root, "generating");

		File regionFolder = new File(worldFolder, "region");

		File[] regionFiles = regionFolder.listFiles();

		if (regionFiles == null)
		{
			getLogger().severe("Unexpected null region files. Aborting");
			System.exit(0);
			return;
		}

		List<ZipEntrySource> zipEntrySourceList = new ArrayList<>();
		zipEntrySourceList.add(new ByteSource("WorldConfig.dat", worldconfig.toString().getBytes(StandardCharsets.UTF_8)));
		for (File file : regionFiles)
		{
			zipEntrySourceList.add(new FileSource("region/" + file.getName(), file));
		}
		zipEntrySourceList.add(new FileSource("level.dat", new File(worldFolder, "level.dat")));

		ZipUtil.pack(zipEntrySourceList.toArray(new ZipEntrySource[zipEntrySourceList.size()]), outputFile);

		FileUtils.deleteQuietly(worldFolder);

		try
		{
			getLogger().info("Uploading " + seed + "!");

			URL url = new URL("http://" + API_HOST_MAP.get("ENDERCHEST") + "/map/uhc/upload?name=" + outputFile.getName());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			IOUtils.copy(new FileInputStream(outputFile), connection.getOutputStream());
			connection.connect();

			if (connection.getResponseCode() != 200)
			{
				if (connection.getResponseCode() == 409)
				{
					getLogger().warning("Oops - Server rejected " + seed + " because it was already generated");

					if (!outputFile.delete())
					{
						getLogger().warning("Could not clean up " + seed);
					}
				}
				else
				{
					getLogger().severe("Failed to upload " + seed + ": " + connection.getResponseCode() + " " + connection.getResponseMessage());
				}
			}
			else
			{
				getLogger().info("Uploaded " + seed + "!");

				if (!outputFile.delete())
				{
					getLogger().warning("Could not clean up " + seed);
				}
			}
		}
		catch (IOException e)
		{
			getLogger().log(Level.SEVERE, "An error occurred while uploading " + seed + "!", e);
		}
		finally
		{
			getLogger().info("Finished generating world seed " + seed);
		}

		Bukkit.shutdown();
	}
}
