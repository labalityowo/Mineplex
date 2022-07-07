package mineplex.game.clans.clans.worldevent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.common.util.UtilWorld;
import mineplex.game.clans.clans.ClansManager;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.google.common.collect.Lists;

public class EventTerrainFinder
{
	private static final long EVENT_AREA_COOLDOWN = UtilTime.convert(2, TimeUnit.HOURS, TimeUnit.MILLISECONDS);
	private ClansManager _clansManager;
	
	public EventTerrainFinder(ClansManager clansManager)
	{
		_clansManager = clansManager;
	}
	
	public Location findAreaInBorderlands(boolean force)
	{
		List<EventLocation> locs = Lists.newArrayList();
		locs.addAll(Arrays.asList(EventLocation.values()));
		locs.sort(new Comparator<EventLocation>()
		{
			public int compare(EventLocation loc1, EventLocation loc2)
			{
				if (!UtilTime.elapsed(loc1.getLastUsed(), EVENT_AREA_COOLDOWN))
				{
					return 1;
				}
				if (!UtilTime.elapsed(loc2.getLastUsed(), EVENT_AREA_COOLDOWN))
				{
					return -1;
				}
				int[] rand = new int[] {1, -1};
				return rand[new Random().nextInt(rand.length)];
			}
		});
		
		for (EventLocation loc : locs)
		{
			if (!UtilTime.elapsed(loc.getLastUsed(), EVENT_AREA_COOLDOWN))
			{
				continue;
			}
			if (_clansManager.getClanUtility().getClaim(loc.getLocation()) == null || _clansManager.getClanUtility().getClaim(loc.getLocation()).Owner.contains("Borderlands"))
			{
				loc.use();
				return loc.getLocation();
			}
		}
		
		if (force && !locs.isEmpty())
		{
			EventLocation loc = locs.get(UtilMath.r(locs.size()));
			loc.use();
			return loc.getLocation();
		}
		
		return null;
	}
	
	public Location locateSpace(Location areaSource, int areaRadius, int xArea, int yArea, int zArea, boolean replaceBlocks, boolean aboveOther, Set<Block> otherBlock)
	{
		for (int i = 0; i < 20; i++)
		{
			int x = UtilMath.r(areaRadius * 2) - areaRadius + areaSource.getBlockX();
			int z = UtilMath.r(areaRadius * 2) - areaRadius + areaSource.getBlockZ();
			
			Block block = UtilBlock.getHighest(areaSource.getWorld(), x, z);
			
			if (!aboveOther) if (otherBlock.contains(block.getRelative(BlockFace.DOWN))) continue;
			
			boolean valid = true;
			
			int overlaps = 0;
			
			// Previous
			for (x = -xArea; x <= xArea; x++)
			{
				for (z = -zArea; z <= zArea; z++)
				{
					for (int y = 0; y <= yArea; y++)
					{
						// Check Blocks
						Block cur = areaSource.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);
						
						if (cur.getRelative(BlockFace.DOWN).isLiquid())
						{
							valid = false;
							break;
						}
						
						if (otherBlock.contains(cur))
						{
							valid = false;
							break;
						}
						
						// Check Area
						if (!UtilBlock.airFoliage(cur)) overlaps += 1;
					}
					
					if (!valid) break;
				}
				
				if (!valid) break;
			}
			
			if (!replaceBlocks && overlaps > 0) continue;
			
			if (!valid) continue;
			
			return block.getLocation();
		}
		
		return null;
	}
	
	/**
	 * Enum with locations around the map for world events to spawn
	 */
	private static enum EventLocation
	{
		ONE("world", -662, 64, -1108),
		TWO("world", 738, 64, -986),
		THREE("world", 1180, 64, -435),
		FOUR("world", 995, 64, 550),
		FIVE("world", 375, 64, 1142),
		SIX("world", -479, 64, 975),
		SEVEN("world", -1140, 64, 449),
		EIGHT("world", -1014, 64, -342);
		
		private String _world;
		private double _x, _y, _z;
		private long _last = 0;
		
		private EventLocation(String worldName, double x, double y, double z)
		{
			_world = worldName;
			_x = x;
			_y = y;
			_z = z;
		}
		
		/**
		 * Returns the Bukkit center location for this event space
		 * @return The Bukkit center location for this event space
		 */
		public Location getLocation()
		{
			return new Location(UtilWorld.getWorld(_world), _x, _y, _z);
		}
		
		/**
		 * Gets the last time this event space has been used
		 * @return The last time this event space has been used
		 */
		public Long getLastUsed()
		{
			return _last;
		}
		
		/**
		 * Updates this event space's last used time to now
		 */
		public void use()
		{
			_last = System.currentTimeMillis();
		}
	}
}