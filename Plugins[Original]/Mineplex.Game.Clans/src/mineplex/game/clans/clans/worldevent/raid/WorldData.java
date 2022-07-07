package mineplex.game.clans.clans.worldevent.raid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.scheduler.BukkitTask;

import mineplex.core.common.timing.TimingManager;
import mineplex.core.common.util.FileUtil;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.WorldUtil;
import mineplex.core.common.util.ZipUtil;
import mineplex.core.common.util.worldgen.WorldGenCleanRoom;
import mineplex.game.clans.spawn.Spawn;

public class WorldData
{
	public int Id = -1;
	
	public String RaidName;
	
	public boolean Loaded = false;
	
	protected BukkitTask LoadChecker = null;
	
	public String File = null;
	public String Folder = null;
	
	public World World;
	public int MinX = 0;
	public int MinZ = 0;
	public int MaxX = 0;
	public int MaxZ = 0;

	public int MinY = -1;
	public int MaxY = 256;
	
	public Map<String, List<Location>> SpawnLocs = new LinkedHashMap<>();
	private Map<String, List<Location>> DataLocs = new LinkedHashMap<>();
	private Map<String, List<Location>> CustomLocs = new LinkedHashMap<>();
	private final Map<String, String> _dataEntries = new LinkedHashMap<>();
	
	public WorldData(String raidName)
	{
		RaidName = raidName;
		initialize();
		
		Id = getNewId();
	}
	
	private List<String> loadFiles()
	{
		TimingManager.start("RaidManager LoadFiles");

		File folder = new File(".." + java.io.File.separatorChar + ".." + java.io.File.separatorChar + "update" + java.io.File.separatorChar
				+ "maps" + java.io.File.separatorChar + "Clans" + java.io.File.separatorChar + "Raids" + java.io.File.separatorChar + RaidName);
		System.out.println(folder.getAbsolutePath() + " -=-=-=-=-=");
		if (!folder.exists())
		{
			folder.mkdirs();
		}

		List<String> maps = new ArrayList<>();

		System.out.println("Searching Maps in: " + folder);

		if (folder.listFiles() != null)
		{
			for (File file : folder.listFiles())
			{
				if (!file.isFile())
				{
					System.out.println(file.getName() + " is not a file!");
					continue;
				}

				String name = file.getName();

				if (name.length() < 5)
				{
					continue;
				}

				name = name.substring(name.length() - 4, name.length());

				if (!name.equals(".zip"))
				{
					System.out.println(file.getName() + " is not a zip.");
					continue;
				}

				maps.add(file.getName().substring(0, file.getName().length() - 4));
			}
		}

		for (String map : maps)
		{
			System.out.println("Found Map: " + map);
		}

		TimingManager.stop("RaidManager LoadFiles");

		return maps;
	}
	
	public void initialize()
	{
		getFile();
		
		UtilServer.runAsync(() ->
		{
			WorldData.this.unzipWorld();
			UtilServer.runSync(() ->
			{
				TimingManager.start("WorldData loading world.");

				WorldCreator creator = new WorldCreator(getFolder());
				creator.generator(new WorldGenCleanRoom());
				World = WorldUtil.LoadWorld(creator);

				TimingManager.stop("WorldData loading world.");
				
				World.setDifficulty(Difficulty.HARD);
				World.setGameRuleValue("showDeathMessages", "false");
				((CraftWorld)World).getHandle().allowAnimals = false;
				((CraftWorld)World).getHandle().allowMonsters = false;

				TimingManager.start("WorldData loading WorldConfig.");
				//Load World Data
				WorldData.this.loadWorldConfig();
				TimingManager.stop("WorldData loading WorldConfig.");
			});
		});
	}
	
	protected String getFile()
	{
		if (File == null)
		{
			int map;
			try
			{
				map = UtilMath.r(loadFiles().size());
			}
			catch (IllegalArgumentException e)
			{
				System.out.println("No maps found!");
				return null;
			}
			File = loadFiles().get(map);
		}
		
		return File;
	}
	
	public String getFolder()
	{
		if (Folder == null) 
		{
			Folder = RaidName + Id + "_" + getFile();
		}	
		return Folder;
	}
	
	protected void unzipWorld() 
	{
		TimingManager.start("UnzipWorld creating folders");
		String folder = getFolder();
		new File(folder).mkdir();
		new File(folder + java.io.File.separator + "region").mkdir();
		new File(folder + java.io.File.separator + "data").mkdir();
		TimingManager.stop("UnzipWorld creating folders");
		
		TimingManager.start("UnzipWorld UnzipToDirectory");
		ZipUtil.UnzipToDirectory("../../update/maps/Clans/Raids/" + RaidName + "/" + getFile() + ".zip", folder);
		TimingManager.stop("UnzipWorld UnzipToDirectory");
	}
	
	public void loadWorldConfig() 
	{
		//Load Track Data
		String line = null;
		
		try
		{
			FileInputStream fstream = new FileInputStream(getFolder() + java.io.File.separator + "WorldConfig.dat");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			List<Location> currentTeam = null;
			List<Location> currentData = null;
			
			int currentDirection = 0;
		
			while ((line = br.readLine()) != null)  
			{
				String[] tokens = line.split(":");
				
				if (tokens.length < 2)
					continue;
				
				if (tokens[0].length() == 0)
					continue;
				
				//Spawn Locations
				if (tokens[0].equalsIgnoreCase("TEAM_NAME"))
				{
					SpawnLocs.put(tokens[1], new ArrayList<>());
					currentTeam = SpawnLocs.get(tokens[1]);
					currentDirection = 0;
				}
				else if (tokens[0].equalsIgnoreCase("TEAM_DIRECTION"))
				{
					currentDirection = Integer.parseInt(tokens[1]);
				}
				else if (tokens[0].equalsIgnoreCase("TEAM_SPAWNS"))
				{
					for (int i=1; i < tokens.length; i++)
					{
						Location loc = strToLoc(tokens[i]);
						if (loc == null)	continue;
						
						loc.setYaw(currentDirection);
						
						currentTeam.add(loc);
					}
				}
				
				//Data Locations
				else if (tokens[0].equalsIgnoreCase("DATA_NAME"))
				{
					DataLocs.put(tokens[1], new ArrayList<>());
					currentData = DataLocs.get(tokens[1]);
				}
				else if (tokens[0].equalsIgnoreCase("DATA_LOCS"))
				{
					for (int i=1; i < tokens.length; i++)
					{
						Location loc = strToLoc(tokens[i]);
						if (loc == null)	continue;
						
						currentData.add(loc);
					}
				}
				
				//Custom Locations
				else if (tokens[0].equalsIgnoreCase("CUSTOM_NAME"))
				{
					CustomLocs.put(tokens[1], new ArrayList<>());
					currentData = CustomLocs.get(tokens[1]);
				}
				else if (tokens[0].equalsIgnoreCase("CUSTOM_LOCS"))
				{
					for (int i=1; i < tokens.length; i++)
					{
						Location loc = strToLoc(tokens[i]);
						if (loc == null)	continue;
						
						currentData.add(loc);
					}
				}
				
				//Map Bounds
				else if (tokens[0].equalsIgnoreCase("MIN_X"))
				{
					try
					{
						MinX = Integer.parseInt(tokens[1]);
					}
					catch (Exception e)
					{
						System.out.println("World Data Read Error: Invalid MinX [" + tokens[1] + "]");
					}
					
				}
				else if (tokens[0].equalsIgnoreCase("MAX_X"))
				{
					try
					{
						MaxX = Integer.parseInt(tokens[1]);
					}
					catch (Exception e)
					{
						System.out.println("World Data Read Error: Invalid MaxX [" + tokens[1] + "]");
					}
				}
				else if (tokens[0].equalsIgnoreCase("MIN_Z"))
				{
					try
					{
						MinZ = Integer.parseInt(tokens[1]);
					}
					catch (Exception e)
					{
						System.out.println("World Data Read Error: Invalid MinZ [" + tokens[1] + "]");
					}
				}
				else if (tokens[0].equalsIgnoreCase("MAX_Z"))
				{
					try
					{
						MaxZ = Integer.parseInt(tokens[1]);
					}
					catch (Exception e)
					{
						System.out.println("World Data Read Error: Invalid MaxZ [" + tokens[1] + "]");
					}
				}
				else if (tokens[0].equalsIgnoreCase("MIN_Y"))
				{
					try
					{
						MinY = Integer.parseInt(tokens[1]);
					}
					catch (Exception e)
					{
						System.out.println("World Data Read Error: Invalid MinY [" + tokens[1] + "]");
					}
				}
				else if (tokens[0].equalsIgnoreCase("MAX_Y"))
				{
					try
					{
						MaxY = Integer.parseInt(tokens[1]);
					}
					catch (Exception e)
					{
						System.out.println("World Data Read Error: Invalid MaxY [" + tokens[1] + "]");
					}
				}
				else
				{
					_dataEntries.put(tokens[0], tokens[1]);
				}
			}

			in.close();
			
			Loaded = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("Line: " + line);
		}
	}
	
	protected Location strToLoc(String loc)
	{
		String[] coords = loc.split(",");
		
		try
		{
			return new Location(World, Integer.valueOf(coords[0]) +0.5, Integer.valueOf(coords[1]), Integer.valueOf(coords[2]) + 0.5);
		}
		catch (Exception e)
		{
			System.out.println("World Data Read Error: Invalid Location String [" + loc + "]");
		}
	
		return null;
	}
	
	public void uninitialize() 
	{	
		if (World == null)
		{
			return;
		}
		
		World.getPlayers().forEach(player -> player.teleport(Spawn.getNorthSpawn()));
		
		//Wipe World
		MapUtil.UnloadWorld(UtilServer.getPlugin(), World);
		MapUtil.ClearWorldReferences(World.getName());
		FileUtil.DeleteFolder(new File(World.getName()));
		
		World = null;
	}
	
	public int getNewId() 
	{
		File file = new File(RaidName + "RaidId.dat");
		
		int id = 0;

		//Write If Blank
		if (!file.exists())
		{
			try (
				FileWriter fstream = new FileWriter(file);
				BufferedWriter out = new BufferedWriter(fstream);
				)
			{
				out.write("0");
			}
			catch (Exception e)
			{
				System.out.println("Error: Raid World GetId Write Exception");
			}
		}
		else
		{
			try (
				FileInputStream fstream = new FileInputStream(file);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				)
			{
				String line = br.readLine();

				id = Integer.parseInt(line);
			}
			catch (Exception e)
			{
				System.out.println("Error: Raid World GetId Read Exception");
			}
		}

		try (
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			)
		{
			out.write("" + (id + 1));
		}
		catch (Exception e)
		{
			System.out.println("Error: Game World GetId Re-Write Exception");
		}

		return id;
	}

	public List<Location> getDataLocs(String data)
	{
		if (!DataLocs.containsKey(data))
		{
			return new ArrayList<>();
		}
		
		return DataLocs.get(data);
	}
	
	public List<Location> getCustomLocs(String id)
	{
		if (!CustomLocs.containsKey(id))
		{
			return new ArrayList<>();
		}
		
		return CustomLocs.get(id);
	}
	
	public Map<String, List<Location>> getAllCustomLocs()
	{
		return CustomLocs;
	}
	
	public Map<String, List<Location>> getAllDataLocs()
	{
		return DataLocs;
	}

	public Location getRandomXZ() 
	{
		Location loc = new Location(World, 0, 250, 0);
		
		int xVar = MaxX - MinX;
		int zVar = MaxZ - MinZ;
		
		loc.setX(MinX + UtilMath.r(xVar));
		loc.setZ(MinZ + UtilMath.r(zVar));

		return loc;
	}

	public String get(String key)
	{
		return _dataEntries.get(key);
	}	
}