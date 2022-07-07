package nautilus.game.arcade.world;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.spigotmc.SpigotConfig;

import mineplex.core.common.Pair;
import mineplex.core.common.api.enderchest.EnderchestWorldLoader;
import mineplex.core.common.timing.TimingManager;
import mineplex.core.common.util.FileUtil;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.WorldUtil;
import mineplex.core.common.util.ZipUtil;
import mineplex.core.common.util.worldgen.WorldGenCleanRoom;
import mineplex.core.world.MineplexWorld;

import nautilus.game.arcade.GameType;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.uhc.UHC;

/**
 * WorldData is a legacy class to bridge the standardised {@link MineplexWorld} system, and the Arcade's games.
 */
public class WorldData
{

	public final Game Host;
	private final int Id;
	private MineplexWorld _mineplexWorld;

	public String File;
	private String Folder;

	public World World;
	public int MinX = 0;
	public int MinZ = 0;
	public int MaxX = 0;
	public int MaxZ = 0;
	public int MinY = -1;
	public int MaxY = 256;

	public String MapName;
	public GameType Game;

	public WorldData(Game game)
	{
		Host = game;
		Id = GetNewId();
	}

	public void Initialize()
	{
		final WorldData worldData = this;
		GetFile();

		Host.getArcadeManager().runAsync(() ->
		{
			//Unzip
			if (Host instanceof UHC)
			{
				boolean uhcLoaded = loadUHCMap(); // attempt to load from enderchest
				if (!uhcLoaded)
				{
					// failsafe on normal UHC map
					worldData.UnzipWorld();
				}
			}
			else
			{
				worldData.UnzipWorld();
			}

			Host.getArcadeManager().runSync(() ->
			{
				TimingManager.start("WorldData loading world.");

				WorldCreator creator = new WorldCreator(GetFolder());
				creator.generator(new WorldGenCleanRoom());
				World = WorldUtil.LoadWorld(creator);

				TimingManager.stop("WorldData loading world.");

				World.setDifficulty(Difficulty.HARD);
				World.setGameRuleValue("showDeathMessages", "false");

				Host.getArcadeManager().runAsync(() ->
				{
					TimingManager.start("WorldData loading WorldConfig.");

					//Load World Data
					_mineplexWorld = new MineplexWorld(World);

					Location min = _mineplexWorld.getMin(), max = _mineplexWorld.getMax();

					MinX = min.getBlockX();
					MinY = min.getBlockY();
					MinZ = min.getBlockZ();
					MaxX = max.getBlockX();
					MaxY = max.getBlockY();
					MaxZ = max.getBlockZ();

					MapName = _mineplexWorld.getMapName();
					Host.getArcadeManager().GetGameWorldManager().RegisterWorld(this);

					TimingManager.stop("WorldData loading WorldConfig.");
				});
			});
		});
	}

	private boolean loadUHCMap()
	{
		EnderchestWorldLoader worldLoader = new EnderchestWorldLoader();
		boolean success = false;

		for (int attempt = 1; !success && attempt <= 3; attempt++)
		{
			System.out.println("Grabbing UHC map from Enderchest, attempt " + attempt);

			try
			{
				worldLoader.loadMap("uhc", GetFolder());
				SpigotConfig.config.set("world-settings." + GetFolder() + ".view-distance", UHC.VIEW_DISTANCE);
				success = true;
			}
			catch (Exception e)
			{
				attempt++;
				e.printStackTrace();
			}
		}

		return success;
	}

	private String GetFile()
	{
		if (File == null)
		{
			Pair<GameType, String> mapFile = Host.getArcadeManager().GetGameCreationManager().getMapFile();
			Game = mapFile.getLeft();
			File = mapFile.getRight();
		}

		return File;
	}

	public String GetFolder()
	{
		if (Folder == null)
		{
			Folder = "Game" + Id + "_" + Game.getName() + "_" + GetFile();
		}
		return Folder;
	}

	protected void UnzipWorld()
	{
		TimingManager.start("UnzipWorld creating folders");
		String folder = GetFolder();
		new File(folder).mkdir();
		new File(folder + java.io.File.separator + "region").mkdir();
		new File(folder + java.io.File.separator + "data").mkdir();
		TimingManager.stop("UnzipWorld creating folders");

		TimingManager.start("UnzipWorld UnzipToDirectory");
		ZipUtil.UnzipToDirectory("../../update/maps/" + Game.getName() + "/" + GetFile() + ".zip", folder);
		TimingManager.stop("UnzipWorld UnzipToDirectory");
	}

	public void Uninitialize()
	{
		if (World == null)
		{
			return;
		}

		//Wipe World
		MapUtil.UnloadWorld(Host.getArcadeManager().getPlugin(), World);
		MapUtil.ClearWorldReferences(World.getName());
		FileUtil.DeleteFolder(new File(World.getName()));

		World = null;
	}

	public int GetNewId()
	{
		File file = new File("GameId.dat");

		//Write If Blank
		if (!file.exists())
		{
			try
			{
				FileWriter fstream = new FileWriter(file);
				BufferedWriter out = new BufferedWriter(fstream);

				out.write("0");

				out.close();
			}
			catch (Exception e)
			{
				System.out.println("Error: Game World GetId Write Exception");
			}
		}

		int id;

		//Read
		try
		{
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = br.readLine();

			id = Integer.parseInt(line);

			in.close();
		}
		catch (Exception e)
		{
			System.out.println("Error: Game World GetId Read Exception");
			id = 0;
		}

		try
		{
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);

			out.write("" + (id + 1));

			out.close();
		}
		catch (Exception e)
		{
			System.out.println("Error: Game World GetId Re-Write Exception");
		}

		return id;
	}

	public ArrayList<Location> GetDataLocs(String data)
	{
		return (ArrayList<Location>) _mineplexWorld.getIronLocations(data);
	}

	public ArrayList<Location> GetCustomLocs(String id)
	{
		return (ArrayList<Location>) _mineplexWorld.getSpongeLocations(id);
	}

	public Map<String, List<Location>> GetAllCustomLocs()
	{
		return _mineplexWorld.getSpongeLocations();
	}

	public Map<String, List<Location>> GetAllDataLocs()
	{
		return _mineplexWorld.getIronLocations();
	}

	public Map<String, List<Location>> getAllSpawnLocations()
	{
		return _mineplexWorld.getGoldLocations();
	}

	public String getFormattedName()
	{
		return _mineplexWorld.getFormattedName();
	}
}
