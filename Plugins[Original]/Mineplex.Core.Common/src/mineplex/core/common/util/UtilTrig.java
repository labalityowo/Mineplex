package mineplex.core.common.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class UtilTrig
{
	public static List<Vector> GetCirclePoints(Vector origin, int points, double radius)
	{
		List<Vector> list = new LinkedList<>();
		
		double slice = 2 * Math.PI / points;
		
		for (int point = 0; point < points; point++)
		{
			double angle = slice * point;
			list.add(new Vector(origin.getX() + radius * Math.cos(angle), 0, origin.getZ() + radius * Math.sin(angle)));
		}
		
		return list;
	}

	public static ArrayList<Location> GetSpherePoints(Location loc, double radius, double height, boolean hollow, double addition)
	{
		ArrayList<Location> circleblocks = new ArrayList<Location>();
		double cx = loc.getBlockX();
		double cy = loc.getBlockY();
		double cz = loc.getBlockZ();

		for (double y = cy - radius; y < cy + radius; y += addition)
		{
			for (double x = cx - radius; x <= cx + radius; x += addition)
			{
				for (double z = cz - radius; z <= cz + radius; z += addition)
				{
					double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (cy - y) * (cy - y);

					if (dist < radius * radius && !(hollow && dist < (radius - 1) * (radius - 1)))
					{
						Location l = new Location(loc.getWorld(), x, y, z);
						circleblocks.add(l);
					}
				}
			}
		}

		return circleblocks;
	}
	
	public static List<Vector> GetSpherePoints(Vector vector, double radius, double height, boolean hollow, double addition)
	{
		List<Vector> circleblocks = new ArrayList<>();
		double cx = vector.getX();
		double cy = vector.getY();
		double cz = vector.getZ();

		for (double y = cy - radius; y < cy + radius; y += addition)
		{
			for (double x = cx - radius; x <= cx + radius; x += addition)
			{
				for (double z = cz - radius; z <= cz + radius; z += addition)
				{
					double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (cy - y) * (cy - y);

					if (dist < radius * radius && !(hollow && dist < (radius - 1) * (radius - 1)))
					{
						Vector l = new Vector(x, y, z);
						circleblocks.add(l);
					}
				}
			}
		}

		return circleblocks;
	}
}
