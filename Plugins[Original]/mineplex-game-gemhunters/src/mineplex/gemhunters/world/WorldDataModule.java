package mineplex.gemhunters.world;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.timing.TimingManager;

@ReflectivelyCreateMiniPlugin
public class WorldDataModule extends MiniPlugin
{
	public World World;
	public int MinX = 0;
	public int MinZ = 0;
	public int MaxX = 0;
	public int MaxZ = 0;

	public int MinY = -1;
	public int MaxY = 256;

	private final Map<String, List<Location>> SPAWN_LOCATIONS = new LinkedHashMap<>();
	private final Map<String, List<Location>> DATA_LOCATIONS = new LinkedHashMap<>();
	private final Map<String, List<Location>> CUSTOM_LOCAITONS = new LinkedHashMap<>();

	private WorldDataModule()
	{
		super("World Data");

		initialize();
	}

	public void initialize()
	{
		final WorldDataModule worldData = this;

		World = Bukkit.getWorlds().get(0);

		World.setDifficulty(Difficulty.EASY);
		World.setGameRuleValue("showDeathMessages", "false");

		TimingManager.start("WorldData loading WorldConfig.");
		worldData.loadWorldConfig();
		TimingManager.stop("WorldData loading WorldConfig.");
	}

	public String getFolder()
	{
		return "world";
	}

	public void loadWorldConfig()
	{
		// Load Track Data
		String line = null;

		try
		{
			FileInputStream fstream = new FileInputStream(getFolder() + File.separator + "WorldConfig.dat");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			List<Location> currentTeam = null;
			List<Location> currentData = null;

			int currentDirection = 0;

			while ((line = br.readLine()) != null)
			{
				String[] tokens = line.split(":");

				if (tokens.length < 2)
				{
					continue;
				}

				String key = tokens[0];
				String value = tokens[1];

				if (key.length() == 0)
				{
					continue;
				}

				// Spawn Locations
				if (key.equalsIgnoreCase("TEAM_NAME"))
				{
					SPAWN_LOCATIONS.put(value, new ArrayList<Location>());
					currentTeam = SPAWN_LOCATIONS.get(value);
					currentDirection = 0;
				}
				else if (key.equalsIgnoreCase("TEAM_DIRECTION"))
				{
					currentDirection = Integer.parseInt(value);
				}
				else if (key.equalsIgnoreCase("TEAM_SPAWNS"))
				{
					for (int i = 1; i < tokens.length; i++)
					{
						Location loc = stringToLocation(tokens[i]);
						if (loc == null)
						{
							continue;
						}

						loc.setYaw(currentDirection);

						currentTeam.add(loc);
					}
				}

				// Data Locations
				else if (key.equalsIgnoreCase("DATA_NAME"))
				{
					DATA_LOCATIONS.put(value, new ArrayList<Location>());
					currentData = DATA_LOCATIONS.get(value);
				}
				else if (key.equalsIgnoreCase("DATA_LOCS"))
				{
					for (int i = 1; i < tokens.length; i++)
					{
						Location loc = stringToLocation(tokens[i]);
						if (loc == null)
						{
							continue;
						}

						currentData.add(loc);
					}
				}

				// Custom Locations
				else if (key.equalsIgnoreCase("CUSTOM_NAME"))
				{
					CUSTOM_LOCAITONS.put(value, new ArrayList<Location>());
					currentData = CUSTOM_LOCAITONS.get(value);
				}
				else if (key.equalsIgnoreCase("CUSTOM_LOCS"))
				{
					for (int i = 1; i < tokens.length; i++)
					{
						Location loc = stringToLocation(tokens[i]);
						if (loc == null)
						{
							continue;
						}

						currentData.add(loc);
					}
				}

				// Map Bounds
				else if (key.equalsIgnoreCase("MIN_X"))
				{
					try
					{
						MinX = Integer.parseInt(value);
					}
					catch (Exception e)
					{
						System.out.println("World Data Read Error: Invalid MinX [" + value + "]");
					}

				}
				else if (key.equalsIgnoreCase("MAX_X"))
				{
					try
					{
						MaxX = Integer.parseInt(value);
					}
					catch (Exception e)
					{
						System.out.println("World Data Read Error: Invalid MaxX [" + value + "]");
					}
				}
				else if (key.equalsIgnoreCase("MIN_Z"))
				{
					try
					{
						MinZ = Integer.parseInt(value);
					}
					catch (Exception e)
					{
						System.out.println("World Data Read Error: Invalid MinZ [" + value + "]");
					}
				}
				else if (key.equalsIgnoreCase("MAX_Z"))
				{
					try
					{
						MaxZ = Integer.parseInt(value);
					}
					catch (Exception e)
					{
						System.out.println("World Data Read Error: Invalid MaxZ [" + value + "]");
					}
				}
				else if (key.equalsIgnoreCase("MIN_Y"))
				{
					try
					{
						MinY = Integer.parseInt(value);
					}
					catch (Exception e)
					{
						System.out.println("World Data Read Error: Invalid MinY [" + value + "]");
					}
				}
				else if (key.equalsIgnoreCase("MAX_Y"))
				{
					try
					{
						MaxY = Integer.parseInt(value);
					}
					catch (Exception e)
					{
						System.out.println("World Data Read Error: Invalid MaxY [" + value + "]");
					}
				}
			}

			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("Line: " + line);
		}
	}

	private Location stringToLocation(String loc)
	{
		String[] coords = loc.split(",");

		try
		{
			return new Location(World, Integer.valueOf(coords[0]) + 0.5, Integer.valueOf(coords[1]), Integer.valueOf(coords[2]) + 0.5);
		}
		catch (Exception e)
		{
			System.out.println("World Data Read Error: Invalid Location String [" + loc + "]");
		}

		return null;
	}

	public List<Location> getSpawnLocation(String colour)
	{
		if (!SPAWN_LOCATIONS.containsKey(colour))
		{
			return new ArrayList<Location>();
		}

		return SPAWN_LOCATIONS.get(colour);
	}

	public List<Location> getDataLocation(String colour)
	{
		if (!DATA_LOCATIONS.containsKey(colour))
		{
			return new ArrayList<Location>();
		}

		return DATA_LOCATIONS.get(colour);
	}

	public List<Location> getCustomLocation(String id)
	{
		if (!CUSTOM_LOCAITONS.containsKey(id))
		{
			return new ArrayList<Location>();
		}

		return CUSTOM_LOCAITONS.get(id);
	}

	public Map<String, List<Location>> getAllSpawnLocations()
	{
		return SPAWN_LOCATIONS;
	}

	public Map<String, List<Location>> getAllCustomLocations()
	{
		return CUSTOM_LOCAITONS;
	}

	public Map<String, List<Location>> getAllDataLocations()
	{
		return DATA_LOCATIONS;
	}
}