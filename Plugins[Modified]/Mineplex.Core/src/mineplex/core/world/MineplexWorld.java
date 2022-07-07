package mineplex.core.world;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;

import mineplex.core.common.util.C;

public class MineplexWorld
{

	private final World _world;

	private Location _min, _max;
	private String _mapName, _mapAuthor;

	private final Map<String, List<Location>> _ironLocations, _goldLocations, _spongeLocations;

	public MineplexWorld(World world)
	{
		_world = world;
		_ironLocations = new LinkedHashMap<>();
		_goldLocations = new LinkedHashMap<>();
		_spongeLocations = new LinkedHashMap<>();

		loadWorldConfig();
	}

	private void loadWorldConfig()
	{
		try
		{
			List<String> lines = Files.readAllLines(new File(_world.getWorldFolder() + File.separator + "WorldConfig.dat").toPath());
			List<Location> current = null;
			int minX = -256, minY = 0, minZ = -256, maxX = 256, maxY = 256, maxZ = 256;

			for (String line : lines)
			{
				String[] tokens = line.split(":");

				if (tokens.length < 2 || tokens[0].isEmpty())
				{
					continue;
				}

				String key = tokens[0], value = tokens[1];

				//Name & Author
				if (key.equalsIgnoreCase("MAP_NAME"))
				{
					_mapName = value;
				}
				else if (key.equalsIgnoreCase("MAP_AUTHOR"))
				{
					_mapAuthor = value;
				}

				//Spawn Locations
				else if (key.equalsIgnoreCase("TEAM_NAME"))
				{
					current = getGoldLocations(value);
				}
				else if (key.equalsIgnoreCase("TEAM_SPAWNS"))
				{
					for (int i = 1; i < tokens.length; i++)
					{
						Location location = fromString(tokens[i]);

						if (location == null)
						{
							continue;
						}

						current.add(location);
					}
				}

				//Data Locations
				else if (key.equalsIgnoreCase("DATA_NAME"))
				{
					current = getIronLocations(value);
				}
				else if (key.equalsIgnoreCase("DATA_LOCS"))
				{
					for (int i = 1; i < tokens.length; i++)
					{
						Location location = fromString(tokens[i]);

						if (location == null)
						{
							continue;
						}

						current.add(location);
					}
				}

				//Custom Locations
				else if (key.equalsIgnoreCase("CUSTOM_NAME"))
				{
					current = getSpongeLocations(value);
				}
				else if (key.equalsIgnoreCase("CUSTOM_LOCS"))
				{
					for (int i = 1; i < tokens.length; i++)
					{
						Location location = fromString(tokens[i]);

						if (location == null)
						{
							continue;
						}

						current.add(location);
					}
				}

				//Map Bounds
				else if (key.equalsIgnoreCase("MIN_X"))
				{
					minX = Integer.parseInt(value);
				}
				else if (key.equalsIgnoreCase("MAX_X"))
				{
					maxX = Integer.parseInt(value);
				}
				else if (key.equalsIgnoreCase("MIN_Z"))
				{
					minZ = Integer.parseInt(value);
				}
				else if (key.equalsIgnoreCase("MAX_Z"))
				{
					maxZ = Integer.parseInt(value);
				}
				else if (key.equalsIgnoreCase("MIN_Y"))
				{
					minY = Integer.parseInt(value);
				}
				else if (key.equalsIgnoreCase("MAX_Y"))
				{
					maxY = Integer.parseInt(value);
				}
			}

			_min = new Location(_world, minX, minY, minZ);
			_max = new Location(_world, maxX, maxY, maxZ);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private Location fromString(String location)
	{
		String[] cords = location.split(",");

		try
		{
			return new Location(_world, Integer.valueOf(cords[0]) + 0.5, Integer.valueOf(cords[1]), Integer.valueOf(cords[2]) + 0.5);
		}
		catch (Exception e)
		{
			System.err.println("World Data Read Error: Invalid Location String [" + location + "]");
		}

		return null;
	}

	public World getWorld()
	{
		return _world;
	}

	public Location getMin()
	{
		return _min;
	}

	public Location getMax()
	{
		return _max;
	}

	public String getMapName()
	{
		return _mapName;
	}

	public String getMapAuthor()
	{
		return _mapAuthor;
	}

	public String getFormattedName()
	{
		return C.cGreen + "Map - " + C.cWhiteB + getMapName() + C.cGray + " created by " + C.cWhiteB + getMapAuthor();
	}

	public Location getIronLocation(String key)
	{
		List<Location> locations = getIronLocations(key);
		return locations.isEmpty() ? null : locations.get(0);
	}

	public Location getGoldLocation(String key)
	{
		List<Location> locations = getGoldLocations(key);
		return locations.isEmpty() ? null : locations.get(0);
	}

	public Location getSpongeLocation(String key)
	{
		List<Location> locations = getSpongeLocations(key);
		return locations.isEmpty() ? null : locations.get(0);
	}

	public List<Location> getIronLocations(String key)
	{
		return _ironLocations.computeIfAbsent(key, k -> new ArrayList<>());
	}

	public List<Location> getGoldLocations(String key)
	{
		return _goldLocations.computeIfAbsent(key, k -> new ArrayList<>());
	}

	public List<Location> getSpongeLocations(String key)
	{
		return _spongeLocations.computeIfAbsent(key, k -> new ArrayList<>());
	}

	public Map<String, List<Location>> getIronLocations()
	{
		return _ironLocations;
	}

	public Map<String, List<Location>> getGoldLocations()
	{
		return _goldLocations;
	}

	public Map<String, List<Location>> getSpongeLocations()
	{
		return _spongeLocations;
	}
}
