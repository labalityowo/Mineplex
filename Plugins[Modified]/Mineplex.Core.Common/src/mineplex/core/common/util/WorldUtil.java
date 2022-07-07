package mineplex.core.common.util;

import java.io.File;

import net.minecraft.server.v1_8_R3.Convertable;
import net.minecraft.server.v1_8_R3.EntityTracker;
import net.minecraft.server.v1_8_R3.EnumDifficulty;
import net.minecraft.server.v1_8_R3.IDataManager;
import net.minecraft.server.v1_8_R3.IProgressUpdate;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.ServerNBTManager;
import net.minecraft.server.v1_8_R3.WorldData;
import net.minecraft.server.v1_8_R3.WorldLoaderServer;
import net.minecraft.server.v1_8_R3.WorldManager;
import net.minecraft.server.v1_8_R3.WorldServer;
import net.minecraft.server.v1_8_R3.WorldSettings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;

public class WorldUtil
{
	public static World LoadWorld(WorldCreator creator)
	{
		CraftServer server = (CraftServer) Bukkit.getServer();
		if (creator == null)
		{
			throw new IllegalArgumentException("Creator may not be null");
		}

		String name = creator.name();
		ChunkGenerator generator = creator.generator();
		File folder = new File(server.getWorldContainer(), name);
		World world = server.getWorld(name);
		net.minecraft.server.v1_8_R3.WorldType type = net.minecraft.server.v1_8_R3.WorldType.getType(creator.type().getName());
		boolean generateStructures = creator.generateStructures();

		if (world != null)
		{
			return world;
		}

		if ((folder.exists()) && (!folder.isDirectory()))
		{
			throw new IllegalArgumentException("File exists with the name '" + name + "' and isn't a folder");
		}

		if (generator == null)
		{
			generator = server.getGenerator(name);
		}
		

		Convertable converter = new WorldLoaderServer(server.getWorldContainer());
		if (converter.isConvertable(name))
		{
			server.getLogger().info("Converting world '" + name + "'");
			converter.convert(name, new IProgressUpdate()
			{
				private long b = System.currentTimeMillis();

				public void a(String s)
				{
				}

				public void a(int i)
				{
					if (System.currentTimeMillis() - this.b >= 1000L)
					{
						this.b = System.currentTimeMillis();
						MinecraftServer.LOGGER.info("Converting... " + i + "%");
					}
				}

				public void c(String s)
				{
				}
			});
		}
		int dimension = 10 + server.getServer().worlds.size();
		boolean used = false;
		do
			for (WorldServer s : server.getServer().worlds)
			{
				used = s.dimension == dimension;
				if (used)
				{
					dimension++;
					break;
				}
			}
		while (used);
		boolean hardcore = false;

		Object sdm = new ServerNBTManager(server.getWorldContainer(), name, true);
		WorldData worlddata = ((IDataManager) sdm).getWorldData();
		if (worlddata == null)
		{
			WorldSettings worldSettings = new WorldSettings(creator.seed(),
					WorldSettings.EnumGamemode.getById(server.getDefaultGameMode().getValue()), generateStructures, hardcore, type);
			worldSettings.setGeneratorSettings(creator.generatorSettings());
			worlddata = new WorldData(worldSettings, name);
		}
		worlddata.checkName(name);
		WorldServer internal = (WorldServer) new WorldServer(server.getServer(), (IDataManager) sdm, worlddata, dimension,
				server.getServer().methodProfiler, creator.environment(), generator).b();

		boolean containsWorld = false;
		for (World otherWorld : server.getWorlds())
		{
			if (otherWorld.getName().equalsIgnoreCase(name.toLowerCase()))
			{
				containsWorld = true;
				break;
			}
		}

		if (!containsWorld)
			return null;

		internal.scoreboard = server.getScoreboardManager().getMainScoreboard().getHandle();

		internal.tracker = new EntityTracker(internal);
		internal.addIWorldAccess(new WorldManager(server.getServer(), internal));
		internal.worldData.setDifficulty(EnumDifficulty.EASY);
		internal.setSpawnFlags(true, true);
		server.getServer().worlds.add(internal);

		if (generator != null)
		{
			internal.getWorld().getPopulators().addAll(generator.getDefaultPopulators(internal.getWorld()));
		}

		server.getPluginManager().callEvent(new WorldInitEvent(internal.getWorld()));
		server.getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld()));

		/*
		for (WorldServer worlder : server.getServer().worlds)
		{
			System.out.println(worlder.getWorldData().getName() + " with dimension: " + worlder.dimension);
		}
		*/

		return internal.getWorld();
	}
}
