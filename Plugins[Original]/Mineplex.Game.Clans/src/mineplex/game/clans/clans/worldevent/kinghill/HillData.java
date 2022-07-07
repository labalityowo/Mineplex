package mineplex.game.clans.clans.worldevent.kinghill;

import java.io.File;
import java.io.IOException;

import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.UtilSchematic;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;

import org.bukkit.Location;

public class HillData
{
	private Schematic _schematic;
	private int _hillX;
	private int _hillY;
	private int _hillZ;
	private int _lengthX;
	private int _lengthY;
	private int _lengthZ;

	public HillData(String fileName, int hillX, int hillY, int hillZ, int lengthX, int lengthY, int lengthZ) throws IOException
	{
		File file = new File("schematic" + File.separator + fileName);
		System.out.println(file.getAbsolutePath());
		_schematic = UtilSchematic.loadSchematic(file);
		_hillX = hillX;
		_hillY = hillY;
		_hillZ = hillZ;
		_lengthX = lengthX;
		_lengthY = lengthY;
		_lengthZ = lengthZ;
	}

	public Schematic getSchematic()
	{
		return _schematic;
	}

	public boolean isOnHill(Location location, Location eventLocation)
	{
		return location.getWorld().equals(eventLocation.getWorld()) && UtilMath.offset(location, eventLocation.clone().add(0, 12, 0)) <= 7.5;
	}

	public Location getHillCenter(Location eventLocation)
	{
		Location hill = eventLocation.clone();
		hill.add(_hillX, _hillY, _hillZ);
		hill.add(_lengthX / 2.0, _lengthY / 2.0, _lengthZ / 2.0);
		return hill;
	}

}
