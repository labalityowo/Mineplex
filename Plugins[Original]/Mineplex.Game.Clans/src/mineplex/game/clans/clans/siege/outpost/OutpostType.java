package mineplex.game.clans.clans.siege.outpost;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;

import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.UtilSchematic;
import mineplex.core.common.util.UtilWorld;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.banners.BannerManager;
import mineplex.game.clans.clans.banners.BannerPattern;
import mineplex.game.clans.clans.banners.ClanBanner;
import mineplex.game.clans.clans.siege.outpost.build.OutpostBlock;
import mineplex.game.clans.clans.siege.outpost.build.OutpostBlockBanner;

public enum OutpostType
{
	MK_I(1, 3, 6) {
		public LinkedHashMap<String, OutpostBlock> createBuildQueue(Location location, ClansManager clans, ClanInfo owner)
		{
			LinkedHashMap<String, OutpostBlock> build = new LinkedHashMap<>();
			
			for (int y = -1; y <= _ySize; y++)
			{
				for (int x = -_size; x <= _size; x++)
				{
					for (int z = -_size; z <= _size; z++)
					{
						Location loc = new Location(location.getWorld(), location.getX()+x, location.getY()+y, location.getZ()+z);

						if (clans.getClanUtility().isClaimed(loc))
						{
							continue;
						}
						
						boolean added = false;
						
						//Floor
						if (y == -1 && Math.abs(x) <= _size-1 && Math.abs(z) <= _size-1)
						{
							build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 98, (byte)0));
							added = true;
						}

						//Walls
						if (Math.abs(x) == _size || Math.abs(z) == _size)
						{
							build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 98, (byte)0));
							added = true;
						}

						//Roof
						if (y == 5 && Math.abs(x) <= _size-1 && Math.abs(z) <= _size-1)
						{
							build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 44, (byte)13));
							added = true;
						}
						
						//Clear
						if (!added)
						{
							if (loc.getBlock().getTypeId() != 0)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 0, (byte) 0));
							}
						}
					}
				}	
			}
			
			for (int y= -1; y <= _ySize; y++)
			{
				for (int x = -_size; x <= _size; x++)
				{
					for (int z = -_size; z <= _size; z++)
					{
						Location loc = new Location(location.getWorld(), location.getX()+x, location.getY()+y, location.getZ()+z);

						if (clans.getClanUtility().isClaimed(loc))
						{
							continue;
						}
						
						//Doors
						if (y == 0 || y == 1)
						{
							if (x == 0 && z == _size)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 71, (byte)(y * 8 + 2 + 4)));
							}
							
							if (x == 0 && z == -_size)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 71, (byte)(y * 8 + 4)));
							}
							
							if (x == _size && z == 0)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 71, (byte)(y * 8 + 3 + 4)));
							}
							
							if (x == -_size && z == 0)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 71, (byte)(y * 8 + 1 + 4)));
							}
						}
						
						//Platform
						if (y == 2)
						{
							if (Math.abs(x) == _size-1 && Math.abs(z) < _size)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 44, (byte)13));
							}
							
							if (Math.abs(z) == _size-1 && Math.abs(x) < _size)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 44, (byte)13));
							}
						}
						
						//Windows
						if (y == 4)
						{
							if (Math.abs(x) == _size && Math.abs(z) < _size-1)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 0, (byte)0));
							}
							
							if (Math.abs(z) == _size && Math.abs(x) < _size-1)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 0, (byte)0));
							}
						}
						
						//Ladders
						if (y >= 0 && y < 3)
						{
							if (x == _size-1 && z == _size-1)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 65, (byte)2));
							}
							
							if (x == (-_size)+1 && z == (-_size)+1)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 65, (byte)3));
							}
						}
						
						//Chests
						if (y == 0)
						{
							if (x == _size-1 && z == (-_size)+1)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 54, (byte)0));
							}
							
							if (x == (-_size)+1 && z == _size-1)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 54, (byte)0));
							}
							
							if (x == _size-2 && z == (-_size)+1)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 54, (byte)0));
							}
							
							if (x == (-_size)+2 && z == _size-1)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 54, (byte)0));
							}
						}
						
						//Beacon Floor
						if (y == -1)
						{
							if (Math.abs(x) <= 1 && Math.abs(z) <= 1)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 42, (byte)0));
							}
						}
						
						//Beacon Roof
						if (y == 5)
						{
							if (Math.abs(x) == 1 && Math.abs(z) <= 1)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 98, (byte)0));
							}	
							
							if (Math.abs(z) == 1 && Math.abs(x) <= 1)
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 98, (byte)0));
							}
						}
						
						//Beacon Glass
						if (y == 5 && x == 0 && z == 0)
						{
							build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, 20, (byte)0));
						}
					}
				}
			}
			
			//Core
			build.put(UtilWorld.locToStr(getCoreLocation(location)), new OutpostBlock(build, getCoreLocation(location), Material.DIAMOND_BLOCK.getId(), (byte)0));
			
			return build;
		}
		
		public List<Location> getWallLocations(Location middle)
		{
			List<Location> list = new ArrayList<>();
			
			list.add(middle.clone().add(_size / 2, 0, 0).add(1, 0, 0));
			list.add(middle.clone().add(_size / 2, 0, -(_size / 2)).add(1, 0, -1));
			list.add(middle.clone().add(-(_size / 2), 0, -(_size / 2)).add(-1, 0, -1));
			list.add(middle.clone().add(-(_size / 2), 0, 0).add(-1, 0, 0));
			
			return list;
		}
		
		public Location getCoreLocation(Location location)
		{
			return location.clone().subtract(0, 1, 0);
		}
	},
	MK_II(2, 5, 25) {
		public LinkedHashMap<String, OutpostBlock> createBuildQueue(Location location, ClansManager clans, ClanInfo owner)
		{
			try
			{
				LinkedHashMap<String, OutpostBlock> build = new LinkedHashMap<>();
				
				File file = new File("schematic" + File.separator + "outpost_mk_II.schematic");
				Schematic schematic = UtilSchematic.loadSchematic(file);
				
				for (int y = 0; y < schematic.getHeight(); y++)
				{
					for (int x = 0; x < schematic.getWidth(); x++)
					{
						for (int z = 0; z < schematic.getLength(); z++)
						{
							int relativeX = -(schematic.getWidth() / 2) + x;
							int relativeZ = -(schematic.getLength() / 2) + z;
							
							Location loc = location.clone().add(relativeX, y - 1, relativeZ);
							
							if (schematic.getBlock(x, y, z) == 0 && loc.getBlock().getType() == Material.AIR)
							{
								continue;
							}
							
							build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, schematic.getBlock(x, y, z), schematic.getData(x, y, z)));
						}	
					}	
				}
				
				//Core
				build.put(UtilWorld.locToStr(getCoreLocation(location)), new OutpostBlock(build, getCoreLocation(location), Material.DIAMOND_BLOCK.getId(), (byte)0));
				
				return build;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		
		public List<Location> getWallLocations(Location middle)
		{
			List<Location> list = new ArrayList<>();
			
			list.add(middle.clone().add(_size / 2, 0, 0).add(1, 0, 0));
			list.add(middle.clone().add(_size / 2, 0, -(_size / 2)).add(1, 0, -1));
			list.add(middle.clone().add(-(_size / 2), 0, -(_size / 2)).add(-1, 0, -1));
			list.add(middle.clone().add(-(_size / 2), 0, 0).add(-1, 0, 0));
			
			return list;
		}
		
		public Location getCoreLocation(Location location)
		{
			return location.clone().subtract(0, 1, 0);
		}
	},
	MK_III(3, 5, 25) {
		public LinkedHashMap<String, OutpostBlock> createBuildQueue(Location location, ClansManager clans, ClanInfo owner)
		{
			try
			{
				LinkedHashMap<String, OutpostBlock> build = new LinkedHashMap<>();
				
				File file = new File("schematic" + File.separator + "outpost_mk_III.schematic");
				Schematic schematic = UtilSchematic.loadSchematic(file);
				BannerManager bm = clans.getBannerManager();
				ClanBanner cb = bm.LoadedBanners.get(owner.getName());
				
				for (int y = 0; y < schematic.getHeight(); y++)
				{
					for (int x = 0; x < schematic.getWidth(); x++)
					{
						for (int z = 0; z < schematic.getLength(); z++)
						{
							int relativeX = -(schematic.getWidth() / 2) + x;
							int relativeZ = -(schematic.getLength() / 2) + z;
							
							Location loc = location.clone().add(relativeX, y - 1, relativeZ);
							
							if (schematic.getBlock(x, y, z) == 0 && loc.getBlock().getType() == Material.AIR)
							{
								continue;
							}
							
							if (Material.getMaterial(schematic.getBlock(x, y, z)).name().contains("BANNER"))
							{
								if (cb == null)
								{
									continue;
								}
								build.put(UtilWorld.locToStr(loc), new OutpostBlockBanner(build, loc, schematic.getBlock(x, y, z), schematic.getData(x, y, z), cb.getBaseColor(), cb.getPatterns().stream().map(BannerPattern::getBukkitPattern).filter(Objects::nonNull).toArray(size -> new Pattern[size])));
							}
							else
							{
								build.put(UtilWorld.locToStr(loc), new OutpostBlock(build, loc, schematic.getBlock(x, y, z), schematic.getData(x, y, z)));
							}
						}	
					}	
				}
				
				//Core
				build.put(UtilWorld.locToStr(getCoreLocation(location)), new OutpostBlock(build, getCoreLocation(location), Material.DIAMOND_BLOCK.getId(), (byte)0));
				
				return build;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		
		public List<Location> getWallLocations(Location middle)
		{
			List<Location> list = new ArrayList<>();
			
			list.add(middle.clone().add(_size / 2, 0, 0).add(1, 0, 0));
			list.add(middle.clone().add(_size / 2, 0, -(_size / 2)).add(1, 0, -1));
			list.add(middle.clone().add(-(_size / 2), 0, -(_size / 2)).add(-1, 0, -1));
			list.add(middle.clone().add(-(_size / 2), 0, 0).add(-1, 0, 0));
			
			return list;
		}
		
		public Location getCoreLocation(Location location)
		{
			return location.clone().subtract(0, 1, 0);
		}
	};
	
	protected int _size;
	protected int _ySize;
	
	private int _id;
	
	OutpostType(int id, int size, int ySize)
	{
		_size = size;
		_ySize = ySize;
		_id = id;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public abstract LinkedHashMap<String, OutpostBlock> createBuildQueue(Location location, ClansManager clans, ClanInfo owner);

	public abstract Location getCoreLocation(Location location);

	public abstract List<Location> getWallLocations(Location location);

	public static OutpostType ById(byte id)
	{
		for (OutpostType type : values())
		{
			if (type._id == id)
			{
				return type;
			}
		}
		
		return null;
	}
}
