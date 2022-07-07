package mineplex.game.clans.clans.worldevent.capturepoint;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.World;

public enum CapturePointLocation
{
	ONE(1075, 66, -456),
	TWO(636, 65, 1102),
	THREE(-1140, 57, -163),
	FOUR(-636, 66, -948),
	FIVE(-75, 51, -1004),
	;
	
	private final double _x, _y, _z;
	
	private CapturePointLocation(double x, double y, double z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	public Location toLocation(World world)
	{
		return new Location(world, _x, _y, _z);
	}
	
	public static CapturePointLocation getRandomLocation()
	{
		return CapturePointLocation.values()[ThreadLocalRandom.current().nextInt(CapturePointLocation.values().length)];
	}
}