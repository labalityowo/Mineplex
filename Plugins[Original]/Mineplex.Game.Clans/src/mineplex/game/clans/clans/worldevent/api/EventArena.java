package mineplex.game.clans.clans.worldevent.api;

import org.bukkit.Location;

import mineplex.core.common.util.UtilMath;

public class EventArena
{
	private final Location _centerLocation;
	private final double _radius;
	private final double _radiusSquared;
	
	public EventArena(Location center, double radius)
	{
		_centerLocation = center;
		_radius = radius;
		_radiusSquared = Math.pow(radius, 2);
	}
	
	public Location getCenterLocation()
	{
		return _centerLocation;
	}
	
	public double getRadius()
	{
		return _radius;
	}
	
	public double getRadiusSquared()
	{
		return _radiusSquared;
	}
	
	public boolean isInArena(Location checkLocation, boolean flat)
	{
		if (flat)
		{
			return UtilMath.offset2dSquared(checkLocation, _centerLocation) <= _radiusSquared;
		}
		
		return UtilMath.offsetSquared(checkLocation, _centerLocation) <= _radiusSquared;
	}	
}