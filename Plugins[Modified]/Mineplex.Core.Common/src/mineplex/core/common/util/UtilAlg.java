package mineplex.core.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import net.minecraft.server.v1_8_R3.AxisAlignedBB;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.TrigMath;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class UtilAlg
{

	public static TreeSet<String> sortKey(Set<String> toSort)
	{
		return sortSet(toSort, null);
	}

	public static <T> TreeSet<T> sortSet(Collection<T> toSort, Comparator<T> comparator)
	{
		TreeSet<T> sortedSet = new TreeSet<>(comparator);
		sortedSet.addAll(toSort);

		return sortedSet;
	}

	public static Location getMidpoint(Location a, Location b)
	{
		return a.clone().add(b.clone().subtract(a.clone()).multiply(0.5));
	}

	public static Vector getTrajectory(Entity from, Entity to)
	{
		return getTrajectory(from.getLocation().toVector(), to.getLocation().toVector());
	}

	public static Vector getTrajectory(Location from, Location to)
	{
		return getTrajectory(from.toVector(), to.toVector());
	}

	public static Vector getTrajectory(Vector from, Vector to)
	{
		return to.clone().subtract(from).normalize();
	}

	public static double[] getTrajectory(double srcx, double srcy, double srcz, double dstx, double dsty, double dstz)
	{
		double dx = dstx - srcx;
		double dy = dsty - srcy;
		double dz = dstz - srcz;

		double len = Math.sqrt(dx * dx + dy * dy + dz * dz);

		return new double[]{dx / len, dy / len, dz / len};
	}

	public static Vector getTrajectory2d(Entity from, Entity to)
	{
		return getTrajectory2d(from.getLocation().toVector(), to.getLocation().toVector());
	}

	public static Vector getTrajectory2d(Location from, Location to)
	{
		return getTrajectory2d(from.toVector(), to.toVector());
	}

	public static Vector getTrajectory2d(Vector from, Vector to)
	{
		return to.clone().subtract(from).setY(0).normalize();
	}

	public static boolean HasSight(Location from, Player to)
	{
		return HasSight(from, to.getLocation()) || HasSight(from, to.getEyeLocation());
	}

	public static boolean HasSight(Location from, Location to)
	{
		//Clone Location
		Location cur = new Location(from.getWorld(), from.getX(), from.getY(), from.getZ());

		double rate = 0.1;
		Vector vec = getTrajectory(from, to).multiply(0.1);

		while (UtilMath.offset(cur, to) > rate)
		{
			cur.add(vec);

			if (!UtilBlock.airFoliage(cur.getBlock()))
				return false;
		}

		return true;
	}
	public static Vector getTrajectory(float yaw, float pitch)
	{
		return new Location(null, 0, 0, 0, yaw, pitch).getDirection();
	}
	public static float GetPitch(Vector vec)
	{
		return GetPitch(vec.getX(), vec.getY(), vec.getZ());
	}

	public static float GetPitch(double[] vec)
	{
		return GetPitch(vec[0], vec[1], vec[2]);
	}

	public static float GetPitch(double x, double y, double z)
	{
		double xz = Math.sqrt((x * x) + (z * z));

		double pitch = Math.toDegrees(TrigMath.atan(xz / y));
		if (y <= 0) pitch += 90;
		else pitch -= 90;

		//Fix for two vectors at same Y giving 180
		if (pitch == 180)
			pitch = 0;

		return (float) pitch;
	}

	public static float GetYaw(Vector vec)
	{
		return GetYaw(vec.getX(), vec.getY(), vec.getZ());
	}

	public static float GetYaw(double[] vec)
	{
		return GetYaw(vec[0], vec[1], vec[2]);
	}

	public static float GetYaw(double x, double y, double z)
	{
		double yaw = Math.toDegrees(TrigMath.atan((-x) / z));
		if (z < 0) yaw += 180;

		return (float) yaw;
	}

	public static Vector Normalize(Vector vec)
	{
		if (vec.length() > 0)
			vec.normalize();

		return vec;
	}

	public static <T> T Random(Set<T> set)
	{
		return Random(new ArrayList<>(set));
	}

	public static <T> T Random(List<T> list)
	{
		if (list == null || list.isEmpty())
		{
			return null;
		}

		return list.get(UtilMath.r(list.size()));
	}

	public static <T> T Random(List<T> list, List<T> exclude)
	{
		int attempts = 0;
		T element;

		do
		{
			element = Random(list);
			attempts++;
		}
		while (element != null && exclude.contains(element) && attempts < 15);

		return element;
	}

	public static <T> void shuffle(T[] array)
	{
		int size = array.length;

		for (int from = 0; from < size; from++)
		{
			int to = UtilMath.r(size);
			T temp = array[from];
			array[from] = array[to];
			array[to] = temp;
		}
	}

	public static List<Block> getBox(Block cornerA, Block cornerB)
	{
		if (cornerA == null || cornerB == null || (cornerA.getWorld() != cornerB.getWorld()))
			return Collections.emptyList();

		ArrayList<Block> list = new ArrayList<>();

		int minX = Math.min(cornerA.getX(), cornerB.getX());
		int minY = Math.min(cornerA.getY(), cornerB.getY());
		int minZ = Math.min(cornerA.getZ(), cornerB.getZ());
		int maxX = Math.max(cornerA.getX(), cornerB.getX());
		int maxY = Math.max(cornerA.getY(), cornerB.getY());
		int maxZ = Math.max(cornerA.getZ(), cornerB.getZ());

		for (int x = minX; x <= maxX; x++)
		{
			for (int y = minY; y <= maxY; y++)
			{
				for (int z = minZ; z <= maxZ; z++)
				{
					list.add(cornerA.getWorld().getBlockAt(x, y, z));
				}
			}
		}

		return list;
	}

	public static boolean inBoundingBox(Location loc, Location cornerA, Location cornerB)
	{
		if (loc.getX() <= Math.min(cornerA.getX(), cornerB.getX())) return false;
		if (loc.getX() >= Math.max(cornerA.getX(), cornerB.getX())) return false;

		if (cornerA.getY() != cornerB.getY())
		{
			if (loc.getY() <= Math.min(cornerA.getY(), cornerB.getY())) return false;
			if (loc.getY() >= Math.max(cornerA.getY(), cornerB.getY())) return false;
		}

		if (loc.getZ() <= Math.min(cornerA.getZ(), cornerB.getZ())) return false;
		if (loc.getZ() >= Math.max(cornerA.getZ(), cornerB.getZ())) return false;

		return true;
	}

	public static boolean inBoundingBox(Location loc, Vector cornerA, Vector cornerB)
	{
		if (loc.getX() <= Math.min(cornerA.getX(), cornerB.getX())) return false;
		if (loc.getX() >= Math.max(cornerA.getX(), cornerB.getX())) return false;

		if (cornerA.getY() != cornerB.getY())
		{
			if (loc.getY() <= Math.min(cornerA.getY(), cornerB.getY())) return false;
			if (loc.getY() >= Math.max(cornerA.getY(), cornerB.getY())) return false;
		}

		if (loc.getZ() <= Math.min(cornerA.getZ(), cornerB.getZ())) return false;
		if (loc.getZ() >= Math.max(cornerA.getZ(), cornerB.getZ())) return false;

		return true;
	}

	public static Vector cross(Vector a, Vector b)
	{
		double x = a.getY() * b.getZ() - a.getZ() * b.getY();
		double y = a.getZ() * b.getX() - a.getX() * b.getZ();
		double z = a.getX() * b.getY() - a.getY() * b.getX();

		return new Vector(x, y, z).normalize();
	}

	public static Vector getRight(Vector vec)
	{
		return cross(vec.clone().normalize(), new Vector(0, 1, 0));
	}

	public static Vector getLeft(Vector vec)
	{
		return getRight(vec).multiply(-1);
	}

	public static Vector getBehind(Vector vec)
	{
		return vec.clone().multiply(-1);
	}

	public static Vector getUp(Vector vec)
	{
		return getDown(vec).multiply(-1);
	}

	public static Vector getDown(Vector vec)
	{
		return cross(vec, getRight(vec));
	}

	public static Location getAverageLocation(List<Location> locs)
	{
		if (locs.isEmpty())
			return null;

		Vector vec = new Vector(0, 0, 0);
		double amount = 0;

		for (Location loc : locs)
		{
			vec.add(loc.toVector());
			amount++;
		}

		vec.multiply(1d / amount);

		return vec.toLocation(locs.get(0).getWorld());
	}

	public static Location getAverageBlockLocation(List<Block> locs)
	{
		if (locs.isEmpty())
			return null;

		Vector vec = new Vector(0, 0, 0);
		double amount = 0;

		for (Block loc : locs)
		{
			vec.add(loc.getLocation().toVector());
			amount++;
		}

		vec.multiply(1d / amount);

		return vec.toLocation(locs.get(0).getWorld());
	}

	public static Vector getAverageBump(Location source, List<Location> locs)
	{
		if (locs.isEmpty())
			return null;

		Vector vec = new Vector(0, 0, 0);
		double amount = 0;

		for (Location loc : locs)
		{
			vec.add(UtilAlg.getTrajectory(loc, source));
			amount++;
		}

		vec.multiply(1d / amount);

		return vec;
	}

	public static Entity findClosest(Entity mid, Collection<Entity> locs)
	{
		Entity bestLoc = null;
		double bestDist = 0;

		for (Entity loc : locs)
		{
			double dist = UtilMath.offsetSquared(mid, loc);

			if (bestLoc == null || dist < bestDist)
			{
				bestLoc = loc;
				bestDist = dist;
			}
		}

		return bestLoc;
	}

	public static Location findClosest(Location mid, Collection<Location> locs)
	{
		Location bestLoc = null;
		double bestDist = 0;

		for (Location loc : locs)
		{
			double dist = UtilMath.offsetSquared(mid, loc);

			if (bestLoc == null || dist < bestDist)
			{
				bestLoc = loc;
				bestDist = dist;
			}
		}

		return bestLoc;
	}

	public static Location findFurthest(Location mid, Collection<Location> locs)
	{
		Location bestLoc = null;
		double bestDist = 0;

		for (Location loc : locs)
		{
			double dist = UtilMath.offsetSquared(mid, loc);

			if (bestLoc == null || dist > bestDist)
			{
				bestLoc = loc;
				bestDist = dist;
			}
		}

		return bestLoc;
	}

	public static boolean isInPyramid(Vector a, Vector b, double angleLimit)
	{
		return (Math.abs(GetPitch(a) - GetPitch(b)) < angleLimit) && (Math.abs(GetYaw(a) - GetYaw(b)) < angleLimit);
	}

	public static boolean isTargetInPlayerPyramid(Player player, Player target, double angleLimit)
	{
		return isInPyramid(player.getLocation().getDirection(), UtilAlg.getTrajectory(player.getEyeLocation(), target.getEyeLocation()), angleLimit) ||
				isInPyramid(player.getLocation().getDirection(), UtilAlg.getTrajectory(player.getEyeLocation(), target.getLocation()), angleLimit);
	}

	public static Location getLocationAwayFromPlayers(List<Location> locations, List<Player> players)
	{
		return getLocationAwayFromOtherLocations(locations, players.stream()
				.map(Entity::getLocation)
				.collect(Collectors.toList()));
	}

	public static Location getLocationAwayFromOtherLocations(List<Location> locations, List<Location> players)
	{
		Location bestLocation = null;
		double bestDist = 0;

		for (Location location : locations)
		{
			double closest = -1;

			for (Location player : players)
			{
				//Different Worlds
				if (!player.getWorld().equals(location.getWorld()))
				{
					continue;
				}

				double dist = UtilMath.offsetSquared(player, location);

				if (closest == -1 || dist < closest)
				{
					closest = dist;
				}
			}

//			if (closest == -1)
//			{
//				continue;
//			}

			if (bestLocation == null || closest > bestDist)
			{
				bestLocation = location;
				bestDist = closest;
			}
		}

		return bestLocation;
	}

	public static Location getLocationNearPlayers(List<Location> locs, ArrayList<Player> players, ArrayList<Player> dontOverlap)
	{
		Location bestLoc = null;
		double bestDist = 0;

		for (Location loc : locs)
		{
			double closest = -1;

			boolean valid = true;

			//Dont spawn on other players
			for (Player player : dontOverlap)
			{
				if (!player.getWorld().equals(loc.getWorld()))
					continue;

				double dist = UtilMath.offsetSquared(player.getLocation(), loc);

				if (dist < 0.8)
				{
					valid = false;
					break;
				}
			}

			if (!valid)
				continue;

			//Find closest player
			for (Player player : players)
			{
				if (!player.getWorld().equals(loc.getWorld()))
					continue;

				double dist = UtilMath.offsetSquared(player.getLocation(), loc);

				if (closest == -1 || dist < closest)
				{
					closest = dist;
				}
			}

			if (closest == -1)
				continue;

			if (bestLoc == null || closest < bestDist)
			{
				bestLoc = loc;
				bestDist = closest;
			}
		}

		return bestLoc;
	}

	public static Vector calculateVelocity(Vector from, Vector to, double heightGain, Entity entity)
	{
		if (entity instanceof LivingEntity)
		{
			return calculateVelocity(from, to, heightGain, 1.15);
		}
		else
		{
			return calculateVelocity(from, to, heightGain, 0.115);
		}
	}

	public static Vector calculateVelocity(Vector from, Vector to, double heightGain)
	{
		return calculateVelocity(from, to, heightGain, 0.115);
	}

	public static Vector calculateVelocity(Vector from, Vector to, double heightGain, double gravity)
	{
		// Block locations
		int endGain = to.getBlockY() - from.getBlockY();

		double dx1 = to.getBlockX() - from.getBlockX();
		double dz1 = to.getBlockZ() - from.getBlockZ();

		double horizDist = Math.sqrt(dx1 * dx1 + dz1 * dz1);
		// Height gain
		double maxGain = heightGain > (endGain + heightGain) ? heightGain : (endGain + heightGain);
		// Solve quadratic equation for velocity
		double a = -horizDist * horizDist / (4 * maxGain);
		double b = horizDist;
		double c = -endGain;
		double slope = -b / (2 * a) - Math.sqrt(b * b - 4 * a * c) / (2 * a);
		// Vertical velocity
		double vy = Math.sqrt(maxGain * gravity);
		// Horizontal velocity
		double vh = vy / slope;
		// Calculate horizontal direction
		int dx = to.getBlockX() - from.getBlockX();
		int dz = to.getBlockZ() - from.getBlockZ();
		double mag = Math.sqrt(dx * dx + dz * dz);
		double dirx = dx / mag;
		double dirz = dz / mag;
		// Horizontal velocity components
		double vx = vh * dirx;
		double vz = vh * dirz;
		return new Vector(vx, vy, vz);
	}

	public static Location getNearestCornerLocation(Location near, Block block)
	{
		ArrayList<Location> corners = new ArrayList<Location>();

		corners.add(block.getLocation().clone());
		corners.add(block.getLocation().clone().add(.999, 0, 0));
		corners.add(block.getLocation().clone().add(.999, 0, .999));
		corners.add(block.getLocation().clone().add(0, 0, .999));

		corners.add(block.getLocation().clone().add(0, .999, 0));
		corners.add(block.getLocation().clone().add(.999, .999, 0));
		corners.add(block.getLocation().clone().add(.999, .999, .999));
		corners.add(block.getLocation().clone().add(0, .999, .999));

		return UtilAlg.findClosest(near, corners);
	}

	public static boolean isSimilar(Location a, Location b)
	{
		return a.getWorld() == b.getWorld() && a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ();
	}

	public static int randomMidpoint(int min, int max)
	{
		int variance = max - min;

		int value = UtilMath.r(variance);

		value += min;

		return value;
	}


	public static EulerAngle vectorToEuler(Vector vector)
	{
		//JUST MAKE SURE THE ARMOR STAND ISNT ROTATED.

		return new EulerAngle(
				Math.toRadians(UtilAlg.GetPitch(vector)),
				Math.toRadians(UtilAlg.GetYaw(vector)),
				0);
	}

	public static AxisAlignedBB toBoundingBox(Location a, Location b)
	{
		return new AxisAlignedBB(a.getX(), a.getY(), a.getZ(), b.getX(), b.getY(), b.getZ());
	}

	public static Location moveForward(Location location, double strength, float yaw, boolean reverse)
	{
		double x = location.getX();
		double z = location.getZ();

		double rad = Math.toRadians(yaw);

		x = reverse ? (x + strength * Math.sin(rad)) : (x - strength * Math.sin(rad));
		z = reverse ? (z - strength * Math.cos(rad)) : (z + strength * Math.cos(rad));

		return new Location(location.getWorld(), x, location.getY(), z, location.getYaw(), location.getPitch());
	}

	public static Location getRandomLocation(Location center, int radius)
	{
		Random r = new Random();
		int x = r.nextInt(radius * 2) - radius;
		int y = r.nextInt(radius * 2) - radius;
		int z = r.nextInt(radius * 2) - radius;
		return center.clone().add(x, y, z);
	}

	/**
	 * Gets a random location, with specific radius
	 *
	 * @param center  The center location
	 * @param radiusX The X radius
	 * @param radiusY The Y radius
	 * @param radiusZ The Z radius
	 * @return A random location in that range
	 */
	public static Location getRandomLocation(Location center, double radiusX, double radiusY, double radiusZ)
	{
		double minX = radiusX * -1, minY = radiusY * -1, minZ = radiusZ * -1;
		double x = minX + (UtilMath.random.nextDouble() * 2 * radiusX);
		double y = minY + (UtilMath.random.nextDouble() * 2 * radiusY);
		double z = minZ + (UtilMath.random.nextDouble() * 2 * radiusZ);
		return center.clone().add((radiusX == 0) ? 0 : x, (radiusY == 0) ? 0 : y, (radiusZ == 0) ? 0 : z);
	}

	public static Location getRandomLocation(Location center, double radius)
	{
		return getRandomLocation(center, radius, radius, radius);
	}

	public static Vector rotateAroundXAxis(Vector vec, double angle)
	{
		double y = vec.getY(), z = vec.getZ(), sin = Math.sin(angle), cos = Math.cos(angle);
		return vec.setY(y * cos - z * sin).setZ(y * sin + z * cos);
	}

	public static Vector rotateAroundYAxis(Vector vec, double angle)
	{
		double x = vec.getX(), z = vec.getZ(), sin = Math.sin(angle), cos = Math.cos(angle);
		return vec.setX(x * cos - z * sin).setZ(x * sin + z * cos);
	}

	public static Vector rotateAroundZAxis(Vector vec, double angle)
	{
		double x = vec.getX(), y = vec.getY(), sin = Math.sin(angle), cos = Math.cos(angle);
		return vec.setX(x * cos - y * sin).setZ(x * sin + y * cos);
	}

	/**
	 * Adjusts the yaw of a location to face the nearest location in the lookAt collection.
	 *
	 * @param location The location to adjust the yaw of
	 * @param lookAt The list of locations to look at
	 */
	public static void lookAtNearest(Location location, List<Location> lookAt)
	{
		location.setYaw(GetYaw(getTrajectory(location, findClosest(location, lookAt))));
	}
}
