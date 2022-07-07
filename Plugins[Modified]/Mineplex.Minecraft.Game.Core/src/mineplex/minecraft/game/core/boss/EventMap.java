package mineplex.minecraft.game.core.boss;

import org.bukkit.Location;
import org.bukkit.Material;

import mineplex.core.common.block.schematic.Schematic;

public class EventMap
{
	private Schematic _schematic;
	private Location _centerLocation;
	private double _xDiff;
	private double _yDiff;
	private double _zDiff;
	private double _xLength;
	private double _yLength;
	private double _zLength;
	
	public EventMap(Schematic schematic, Location cornerLocation)
	{
		_schematic = schematic;
		_xLength = schematic.getWidth();
		_yLength = schematic.getHeight();
		_zLength = schematic.getLength();
		_xDiff = _xLength / 2D;
		_yDiff = _yLength / 2D;
		_zDiff = _zLength / 2D;
		
		_centerLocation = cornerLocation.clone().add(_xDiff, _yDiff, _zDiff);
	}
	
	public Location getCenterLocation()
	{
		return _centerLocation;
	}
	
	public boolean isInMap(Location checkLocation)
	{
		Location center = getCenterLocation();
		
		double x = Math.abs(center.getX() - checkLocation.getX());
		double y = Math.abs(center.getY() - checkLocation.getY());
		double z = Math.abs(center.getZ() - checkLocation.getZ());
		
		return x <= _xDiff && y <= _yDiff && z <= _zDiff;
	}
	
	public Schematic getSchematic()
	{
		return _schematic;
	}
	
}
