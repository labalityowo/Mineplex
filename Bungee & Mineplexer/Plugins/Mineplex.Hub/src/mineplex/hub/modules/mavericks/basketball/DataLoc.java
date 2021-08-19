package mineplex.hub.modules.mavericks.basketball;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Enum for easy references to data location keys
 */
public enum DataLoc
{
	RED_HOOP(0, 15, 45),
	BLUE_HOOP(0, 15, -47),
	CENTER_COURT(0, 8, -1),
	CORNER_MIN(-24.8, 8, -47.8),
	CORNER_MAX(24.8, 8, 45.8),
	RED_SCORE_SPAWN(new Double[] {7D, 7D, -7D, -7D}, new Double[] {8D, 8D, 8D, 8D}, new Double[] {43D, 28D, 43D, 28D}),
	RED_UNDER_HOOP(0, 8, 43),
	BLUE_SCORE_SPAWN(new Double[] {-7D, -7D, 7D, 7D}, new Double[] {8D, 8D, 8D, 8D}, new Double[] {-45D, -30D, -45D, -30D}),
	BLUE_UNDER_HOOP(0, 8, -45),
	RED_SPAWNS(new Double[] {-8D, -5D, 0D, 5D, 8D}, new Double[] {8D, 8D, 8D, 8D, 8D}, new Double[] {2D, 6D, 8D, 6D, 2D}),
	BLUE_SPAWNS(new Double[] {-8D, -5D, 0D, 5D, 8D}, new Double[] {8D, 8D, 8D, 8D, 8D}, new Double[] {-4D, -8D, -10D, -8D, -4D})
	;
	
	private Double[] _x, _y, _z;
	
	private DataLoc(Double[] x, Double[] y, Double[] z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	private DataLoc(double x, double y, double z)
	{
		this(new Double[] {x}, new Double[] {y}, new Double[] {z});
	}
	
	/**
	 * Fetches the Locations bound to this DataLoc
	 * @param world The world to input this DataLoc's coordinates into
	 * @return The Locations for this DataLoc
	 */
	public Location[] getLocations(World world)
	{
		Location[] array = new Location[_x.length];
		
		for (int i = 0; i < _x.length; i++)
		{
			array[i] = new Location(world, _x[i], _y[i], _z[i]);
		}
		
		return array;
	}
	
	/**
	 * Fetches the first Location bound to this DataLoc
	 * @param world The world to input this DataLoc's coordinates into
	 * @return The first Location for this DataLoc
	 */
	public Location getLocation(World world)
	{
		if (getLocations(world).length < 1)
		{
			return null;
		}
		return getLocations(world)[0];
	}
}