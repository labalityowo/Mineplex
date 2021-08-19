package mineplex.mapparser;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.material.Wool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Parse 
{
	//Parse Data
	private MapParser Host;
	private World _world;
	private String[] _args;
	private Location _callLoc;
	
	private int _size = 600;
	private int _x = 0;
	private int _y = 0;
	private int _z = 0;
	
	//Map Data
	private MapData _mapData;

	//World Data
	private HashSet<Integer> _dataId = new HashSet<Integer>();
	private HashMap<String, ArrayList<Location>> _teamLocs = new HashMap<String, ArrayList<Location>>();
	private HashMap<String, ArrayList<Location>> _dataLocs = new HashMap<String, ArrayList<Location>>();
	private HashMap<String, ArrayList<Location>> _customLocs = new HashMap<String, ArrayList<Location>>();

	private Location _cornerA = null;
	private Location _cornerB = null;

	private int _processed = 0;

	public Parse(MapParser host, World world, String[] args, Location loc, MapData data, int size)
	{
		Host = host;

		_world = world;
		_args = args;
		_callLoc = new Location(world, loc.getX(), loc.getY(), loc.getZ());
		
		_mapData = data;
		
		_size = size;
		
		for (String arg : args)
			Host.announce("Parse Arg: " + F.elem(arg));

		Initialize();
	}

	private void Initialize() 
	{
		Host.announce("Commencing Parse of World: " + F.elem(_world.getName()));

		//Take BlockID Arguments
		for (String arg : _args)
		{
			try
			{
				_dataId.add(Integer.parseInt(arg));
			}
			catch (Exception e)
			{
				Host.announce("Invalid Data ID: " + F.elem(arg));
			}
		}

		_x = -_size;
		_z = -_size;
		_y = 0;
	}

	@SuppressWarnings("deprecation")
	public boolean Update()
	{
		long startTime = System.currentTimeMillis();

		for ( ; _x <= _size ; _x++)
		{
			for ( ; _z <= _size ; _z++)
			{
				for ( ; _y <= 256 ; _y++)
				{
					if (UtilTime.elapsed(startTime, 10))
						return false;

					_processed++;
					if (_processed % 10000000 == 0)
						Host.announce("Scanning World: " + F.elem((int)(_processed/1000000) + "M of " + (int)(((_size*2)*(_size*2)*256)/1000000) + "M"));

					Block block = _world.getBlockAt(_callLoc.getBlockX()+_x, _y, _callLoc.getBlockZ()+_z);					
					
					//ID DATA
					if (_dataId.contains(block.getTypeId()))
					{
						String key = ""+block.getTypeId();

						if (!_customLocs.containsKey(key))
							_customLocs.put(key, new ArrayList<Location>());

						_customLocs.get(key).add(block.getLocation());
						continue;
					}

					//Signs
					if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN)
					{
						if (block.getRelative(BlockFace.DOWN).getType() == Material.SPONGE)
						{
							Sign s = (Sign) block.getState();

							String name = "";

							try
							{ 
								name = s.getLine(0);
								
								if (s.getLine(1) != null && s.getLine(1).length() > 0)
									name += " " + s.getLine(1);
								
								if (s.getLine(2) != null && s.getLine(2).length() > 0)
									name += " " + s.getLine(2);
								
								if (s.getLine(3) != null && s.getLine(3).length() > 0)
									name += " " + s.getLine(3);
								
								System.out.println("Custom Location: " + name);
							}
							catch (Exception e)
							{
								Host.announce("Invalid Sign Data: " + UtilWorld.locToStr(block.getLocation()));
							}

							//Add
							if (!_customLocs.containsKey(name))
								_customLocs.put(name, new ArrayList<Location>());

							_customLocs.get(name).add(block.getRelative(BlockFace.DOWN).getLocation());

							//Remove Blocks
							block.setTypeId(0);
							block.getRelative(BlockFace.DOWN).setTypeId(0);
						}
					}
					else if (block.getType() == Material.LEAVES || block.getType() == Material.LEAVES_2)
					{
						if (block.getData() <= 3)
						{
							// https://minecraft.gamepedia.com/Java_Edition_data_values#Leaves
							// For leaves, you add 4 to get the no decay version
							block.setData((byte) ((int)block.getData() + 4));
						}
					}

					//Spawns + Borders
					if (block.getTypeId() == 147)
					{
						Block wool = block.getRelative(BlockFace.DOWN);
						if (wool == null)
						{
							continue;
						}
							

						if (wool.getType() == Material.WOOL)
						{
							if (wool.getData() == 0)
							{
								if (_cornerA == null)		
								{
									_cornerA = wool.getLocation();
									Host.announce("Corner A: " + UtilWorld.locToStrClean(_cornerA));
								}
									
								else if (_cornerB == null)	
								{
									_cornerB = wool.getLocation();
									Host.announce("Corner B: " + UtilWorld.locToStrClean(_cornerB));
								}
									
								else						
								{
									Host.announce("More than 2 Corner Markers:");
									Host.announce("Corner A: " + UtilWorld.locToStrClean(_cornerA));
									Host.announce("Corner B: " + UtilWorld.locToStrClean(_cornerB));
									Host.announce("Excess: " + UtilWorld.locToStrClean(wool.getLocation()));
								}

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}

							if (wool.getData() == 1)	
							{
								if (!_teamLocs.containsKey("Orange"))
									_teamLocs.put("Orange", new ArrayList<Location>());

								_teamLocs.get("Orange").add(wool.getLocation());

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}

							if (wool.getData() == 2)	
							{
								if (!_teamLocs.containsKey("Magenta"))
									_teamLocs.put("Magenta", new ArrayList<Location>());

								_teamLocs.get("Magenta").add(wool.getLocation());

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}

							if (wool.getData() == 3)	
							{
								if (!_teamLocs.containsKey("Sky"))
									_teamLocs.put("Sky", new ArrayList<Location>());

								_teamLocs.get("Sky").add(wool.getLocation());

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}

							if (wool.getData() == 4)	
							{
								if (!_teamLocs.containsKey("Yellow"))
									_teamLocs.put("Yellow", new ArrayList<Location>());

								_teamLocs.get("Yellow").add(wool.getLocation());

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}

							if (wool.getData() == 5)	
							{
								if (!_teamLocs.containsKey("Lime"))
									_teamLocs.put("Lime", new ArrayList<Location>());

								_teamLocs.get("Lime").add(wool.getLocation());

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}

							if (wool.getData() == 6)	
							{
								if (!_teamLocs.containsKey("Pink"))
									_teamLocs.put("Pink", new ArrayList<Location>());

								_teamLocs.get("Pink").add(wool.getLocation());

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}

							if (wool.getData() == 7)	
							{
								if (!_teamLocs.containsKey("Gray"))
									_teamLocs.put("Gray", new ArrayList<Location>());

								_teamLocs.get("Gray").add(wool.getLocation());

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}

							if (wool.getData() == 8)	
							{
								if (!_teamLocs.containsKey("LGray"))
									_teamLocs.put("LGray", new ArrayList<Location>());

								_teamLocs.get("LGray").add(wool.getLocation());

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}

							if (wool.getData() == 9)	
							{
								if (!_teamLocs.containsKey("Cyan"))
									_teamLocs.put("Cyan", new ArrayList<Location>());

								_teamLocs.get("Cyan").add(wool.getLocation());

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}

							if (wool.getData() == 10)	
							{
								if (!_teamLocs.containsKey("Purple"))
									_teamLocs.put("Purple", new ArrayList<Location>());

								_teamLocs.get("Purple").add(wool.getLocation());

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}

							if (wool.getData() == 11)	
							{
								if (!_teamLocs.containsKey("Blue"))
									_teamLocs.put("Blue", new ArrayList<Location>());

								_teamLocs.get("Blue").add(wool.getLocation());

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}

							if (wool.getData() == 12)	
							{
								if (!_teamLocs.containsKey("Brown"))
									_teamLocs.put("Brown", new ArrayList<Location>());

								_teamLocs.get("Brown").add(wool.getLocation());

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}

							if (wool.getData() == 13)	
							{
								if (!_teamLocs.containsKey("Green"))
									_teamLocs.put("Green", new ArrayList<Location>());

								_teamLocs.get("Green").add(wool.getLocation());

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}

							if (wool.getData() == 14)	
							{
								if (!_teamLocs.containsKey("Red"))
									_teamLocs.put("Red", new ArrayList<Location>());

								_teamLocs.get("Red").add(wool.getLocation());

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}

							if (wool.getData() == 15)	
							{
								if (!_teamLocs.containsKey("Black"))
									_teamLocs.put("Black", new ArrayList<Location>());

								_teamLocs.get("Black").add(wool.getLocation());

								//Remove Blocks
								block.setTypeId(0);
								wool.setTypeId(0);
							}
						}
					}

					if (block.getTypeId() != 148)
						continue;

					Block wool = block.getRelative(BlockFace.DOWN);
					if (wool == null)
						continue;

					if (wool.getType() != Material.WOOL)
						continue;

					Wool woolData = new Wool(wool.getType(), wool.getData());

					String dataType = woolData.getColor().name();

					if (!_dataLocs.containsKey(dataType))
						_dataLocs.put(dataType, new ArrayList<Location>());

					_dataLocs.get(dataType).add(wool.getLocation());

					//Remove Blocks
					block.setTypeId(0);
					wool.setTypeId(0);
				}
				
				_y = 0;
			}
			
			_z = -_size;
		}

		//Finalize

		if (_cornerA == null || _cornerB == null)
		{
			Host.announce("Missing Corner Locations! Defaulted to -256 to +256.");

			_cornerA = new Location(_world, -256, 0, -256);
			_cornerB = new Location(_world, 256, 0, 256);
		}

		//Save
		try
		{
			FileWriter fstream = new FileWriter(_world.getName() + File.separator + "WorldConfig.dat");
			BufferedWriter out = new BufferedWriter(fstream);

			out.write("MAP_NAME:"+_mapData.MapName);
			out.write("\n");
			out.write("MAP_AUTHOR:"+_mapData.MapCreator);
			out.write("\n");
			out.write("\n");
			out.write("MIN_X:"+Math.min(_cornerA.getBlockX(), _cornerB.getBlockX()));
			out.write("\n");
			out.write("MAX_X:"+Math.max(_cornerA.getBlockX(), _cornerB.getBlockX()));
			out.write("\n");
			out.write("MIN_Z:"+Math.min(_cornerA.getBlockZ(), _cornerB.getBlockZ()));
			out.write("\n");
			out.write("MAX_Z:"+Math.max(_cornerA.getBlockZ(), _cornerB.getBlockZ()));
			out.write("\n");
			out.write("\n");
			if (_cornerA.getBlockY() == _cornerB.getBlockY())
			{
				out.write("MIN_Y:0");
				out.write("\n");
				out.write("MAX_Y:256");
			}
			else
			{
				out.write("MIN_Y:"+Math.min(_cornerA.getBlockY(), _cornerB.getBlockY()));
				out.write("\n");
				out.write("MAX_Y:"+Math.max(_cornerA.getBlockY(), _cornerB.getBlockY()));
			}

			//Teams
			for (String team : _teamLocs.keySet())
			{
				out.write("\n");
				out.write("\n");
				out.write("TEAM_NAME:" + team);
				out.write("\n");
				out.write("TEAM_SPAWNS:" + LocationsToString(_teamLocs.get(team)));		
			}

			//Data
			for (String data : _dataLocs.keySet())
			{
				out.write("\n");
				out.write("\n");
				out.write("DATA_NAME:" + data);
				out.write("\n");
				out.write("DATA_LOCS:" + LocationsToString(_dataLocs.get(data)));
			}

			//Custom
			for (String data : _customLocs.keySet())
			{
				out.write("\n");
				out.write("\n");
				out.write("CUSTOM_NAME:" + data);
				out.write("\n");
				out.write("CUSTOM_LOCS:" + LocationsToString(_customLocs.get(data)));
			}

			out.close();
		}
		catch (Exception e)
		{
			Host.announce("Error: File Write Error");
			
			e.printStackTrace();
		}

		Host.announce("WorldConfig.dat Saved.");

		return true;
	}

	public String LocationsToString(ArrayList<Location> locs)
	{
		String out = "";

		for (Location loc : locs)
			out += loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ":";

		return out;
	}

	public String LocationSignsToString(HashMap<Location, String> locs)
	{
		String out = "";

		for (Location loc : locs.keySet())
			out += locs.get(loc) + "@" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ":";

		return out;
	}

	public GameType getGameType()
	{
		return _mapData.MapGameType;
	}
	
	public World getWorld()
	{
		return _world;
	}
}
