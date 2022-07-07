package nautilus.game.arcade.game.games.monstermaze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import nautilus.game.arcade.game.games.monstermaze.mazes.Maze1;
import nautilus.game.arcade.game.games.monstermaze.mazes.Maze2;
import nautilus.game.arcade.game.games.monstermaze.mazes.Maze3;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class MMMazes
{
	private static int[][] _lastSelected = null;
	
	public static MazePreset getRandomMapPreset(Location loc, MazeBlockData data)
	{
		return new MazePreset(getRandomMap(), loc, data);		
	}
	
	public static class MazePreset
	{
		private MazeBlockData _data;
		
		private boolean _built = false;
		
		private ArrayList<Location> _maze = new ArrayList<Location>();
		private ArrayList<Location> _validSafePadSpawns;
		private ArrayList<Location> _spawns = new ArrayList<Location>();
		
		private ArrayList<Location> _centerSafeZone = new ArrayList<Location>();
		private ArrayList<Location> _centerSafeZonePaths = new ArrayList<Location>();
		
		private ArrayList<Location> _glassBounds = new ArrayList<Location>();
		
		private ArrayList<Location> _safeZones = new ArrayList<Location>();
		
		private Location _center;
		
		public MazePreset(int[][] rawMaze, Location loc, MazeBlockData data)
		{
			int[][] maze = rawMaze;
			
			_data = data;
			
			_center = loc;
			
			Location cur = _center.clone().subtract(49, 0, 49);
			
			// x/y refers to array coordinates, not physical minecraft location
			
			//0 = Air
			//1 = Maze
			//2 = Spawn
			//3 = Center Safe Area
			//4 = Safe Barrier
			//5 = Paths through center safe area
			//6 = paths through safe barrier
			
			for (int y = 0; y < 99; y++)
			{
				for (int x = 0; x < 99; x++)
				{
					int i = maze[y][x];
					if(i == 1)
					{
						_maze.add(cur);
					}
					else if(i == 2)
					{
						_maze.add(cur);
						_spawns.add(cur);
					}
					else if(i == 3)
					{
						_centerSafeZone.add(cur);
					}
					else if(i == 4)
					{
						_centerSafeZone.add(cur);
						_glassBounds.add(cur.clone().add(0, 2, 0));
					}
					else if (i == 5)
					{
						_centerSafeZone.add(cur);
						_centerSafeZonePaths.add(cur);
						_maze.add(cur);
					}
					else if (i == 6)
					{
						_centerSafeZone.add(cur);
						_glassBounds.add(cur.clone().add(0, 2, 0));
						_centerSafeZonePaths.add(cur);
						_maze.add(cur);
					}
					cur = cur.clone().add(0, 0, 1);
				}
				cur.setZ(_center.clone().subtract(49, 0, 49).getZ());
				cur = cur.clone().add(1, 0, 0);
			}
			
			_validSafePadSpawns = new ArrayList<Location>(_maze);
			
			Iterator<Location> iter = _validSafePadSpawns.iterator();
			removeLoop: while(iter.hasNext())
			{
				Location l = iter.next();
				
				for(Location s : _spawns)
				{
					if(UtilMath.offset2d(l, s) < 10)
					{
						iter.remove();
						continue removeLoop;
					}
				}
				
				for(Location b : _glassBounds)
				{
					if(UtilMath.offset2d(l, b) < 7)
					{
						iter.remove();
						continue removeLoop;
					}
				}
			}

			ArrayList<Location> locsToPickFrom = new ArrayList<>(_validSafePadSpawns);

			int numberOfSafeZones = 8;
			for (int i = 0; i < numberOfSafeZones; i++)
			{
				Location toAdd = null;
				if (locsToPickFrom == null)
				{
					toAdd = UtilAlg.Random(locsToPickFrom);
				}
				else
				{
					ArrayList<Location> toBeAwayFrom = new ArrayList<Location>(_safeZones);
					toBeAwayFrom.add(_center);
					toAdd = UtilAlg.getLocationAwayFromOtherLocations(locsToPickFrom, toBeAwayFrom);
				}

				_safeZones.add(toAdd);
//				for(Block b : UtilBlock.getInBoundingBox(toAdd.clone().add(1, 0, 1), toAdd.clone().subtract(1, 0, 1), false))
//				{
//					_safeZones.add(b.getLocation());
//				}

				for (Block b : UtilBlock.getInRadius(toAdd.getBlock(), 6).keySet())
				{
					locsToPickFrom.remove(b.getLocation());
				}
			}
			System.out.println("_safeZones.size() = " + _safeZones.size());
			
			Iterator<Location> it = _maze.iterator();
			while(it.hasNext())
			{
				Location lo = it.next();
				if(_safeZones.contains(lo.getBlock().getLocation())) it.remove();
			}
			
			Iterator<Location> iter2 = _validSafePadSpawns.iterator();
			removeLoop: while(iter2.hasNext())
			{
				Location l = iter2.next();
				
				for(Location s : _safeZones)
				{
					if(UtilMath.offset2d(l, s) < 7)
					{
						iter2.remove();
						continue removeLoop;
					}
				}
			
			}
		}
		
		@SuppressWarnings("deprecation")
		public void build()
		{
			for (Location loc : _maze)
			{	
				buildMazeBlockAt(loc);
			}
			
			for (Location loc : _spawns)
			{
				loc.clone().subtract(0, 1, 0).getBlock().setType(Material.REDSTONE_BLOCK);
			}
			
			for (Location loc : _centerSafeZone)
			{
				loc.clone().subtract(0, 1, 0).getBlock().setTypeIdAndData(159, (byte) 5, true);
			}
			
			_built = true;
		}
		
		public ArrayList<Location> getSpawns()
		{
			return _spawns;
		}
		
		public ArrayList<Location> getSafeZones()
		{
			return _safeZones;
		}
		
		public boolean isBuilt()
		{
			return _built;
		}
		
		@SuppressWarnings("deprecation")
		public void buildMazeBlockAt(Location loc)
		{
			Location mod = loc.clone();
			
			mod.subtract(0, 1, 0).getBlock().setTypeIdAndData(_data.Top.Type.getId(), _data.Top.Data, true);
			mod.subtract(0, 1, 0).getBlock().setTypeIdAndData(_data.Middle.Type.getId(), _data.Middle.Data, true);
			mod.subtract(0, 1, 0).getBlock().setTypeIdAndData(_data.Bottom.Type.getId(), _data.Bottom.Data, true);
		}
		
		// anywhere a monster can walk
		public ArrayList<Location> getMaze()
		{
			return _maze;
		}
		
		public ArrayList<Location> getGlassBounds()
		{
			return _glassBounds;
		}
		
		public ArrayList<Location> getCenterSafeZone()
		{
			return _centerSafeZone;
		}

		public ArrayList<Location> getCenterSafeZonePaths()
		{
			return _centerSafeZonePaths;
		}
		
		public ArrayList<Location> getValidSafePadSpawns()
		{
			return _validSafePadSpawns;
		}

		public Location getCenter()
		{
			return _center;
		}
	}

	// PARSED MAZES
	
	private static final int[][] getRandomMap()
	{
		List<int[][]> maps = Arrays.asList(Maze1.MAZE, Maze2.MAZE, Maze3.MAZE);
		
		//Attempt 10 times to get a new random maze.
		int[][] selected = null;
		for (int i = 0 ; i < 10 && selected == null ; i++)
		{	
			int[][] possible = maps.get(UtilMath.r(maps.size()));
			
			if (_lastSelected != possible)
				selected = possible;
		}
		
		//If the random hit the last selected every time
		//Then just pick a random one.
		if (selected == null)
			selected = maps.get(UtilMath.r(maps.size()));

		//This makes it work between different games.
		_lastSelected = selected;
		return maps.get(UtilMath.r(maps.size()));
	}
	
	//0 = Air
	//1 = Maze
	//2 = Spawn
	//3 = Center Safe Area
	//4 = Safe Barrier
	//5 = Paths through center safe area
	//6 = paths through safe barrier
}
