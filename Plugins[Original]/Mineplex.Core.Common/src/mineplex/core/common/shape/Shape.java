package mineplex.core.common.shape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;

/**
 * A simple 3D vector stored shape
 */

public class Shape
{

	protected final static double DEFAULT_DENSITY = 1;

	protected HashSet<Vector> _points = new HashSet<>();

	public Shape(){}

	public Shape(Collection<Vector> points){
		this._points.addAll(points);
	}

	/**
	 * Rotate this shape along the X-axis
	 * @param radians Radians to rotate the shape
	 */
	public void rotateOnXAxis(double radians)
	{
		for(Vector v : _points)
		{
			UtilAlg.rotateAroundXAxis(v, radians);
		}
	}

	/**
	 * Rotate this shape along the Y-axis
	 * @param radians Radians to rotate the shape
	 */
	public void rotateOnYAxis(double radians)
	{
		for(Vector v : _points)
		{
			UtilAlg.rotateAroundYAxis(v, radians);
		}
	}

	/**
	 * Rotate this shape along the Z-axis
	 * @param radians Radians to rotate the shape
	 */
	public void rotateOnZAxis(double radians)
	{
		for(Vector v : _points)
		{
			UtilAlg.rotateAroundZAxis(v, radians);
		}

	}

	/**
	 * Offsets all the points based on the given vector
	 * @param v
	 */
	public void add(Vector v)
	{
		for(Vector p : _points) p.add(v);
	}

	public void addPoint(Vector v)
	{
		_points.add(v);
	}

	public boolean removePoint(Vector v)
	{
		return _points.remove(v);
	}

	public Set<Vector> getPoints()
	{
		return new HashSet<>(_points);
	}

	/**
	 * Multiply all the points by m.
	 * If m > 1 then the shape will become larger.
	 * If m < 1 then the shape will become smaller.
	 * If m = 1 then the shape will stay the same.
	 * If m < 0 then the shape will become inverted.
	 * @param m
	 */
	public void multiply(double m)
	{
		for(Vector v : _points) v.multiply(m);
	}

	public Shape clone() {
		List<Vector> list = new ArrayList<>();
		for(Vector p : _points)
		{
			list.add(p.clone());
		}
		return new Shape(list);
	}

	public Vector getMidPoint()
	{
		return getMaxAABBCorner().subtract(getMinAABBCorner()).multiply(0.5);
	}

	public Vector getMaxAABBCorner()
	{
		Vector max = null;
		for(Vector v : _points)
		{
			if(max == null)
			{
				max = v.clone();
				continue;
			}
			if(v.getX() > max.getX()) max.setX(v.getX());
			if(v.getY() > max.getY()) max.setY(v.getY());
			if(v.getZ() > max.getZ()) max.setZ(v.getZ());
		}
		return max;
	}

	public Vector getMinAABBCorner()
	{
		Vector min = null;
		for(Vector v : _points)
		{
			if(min == null)
			{
				min = v.clone();
				continue;
			}
			if(v.getX() < min.getX()) min.setX(v.getX());
			if(v.getY() < min.getY()) min.setY(v.getY());
			if(v.getZ() < min.getZ()) min.setZ(v.getZ());
		}
		return min;
	}

	/**
	 * Get the closest length which will be a factor of the provided length, but not longer then max
	 * E.g. You want to split a length of 9 into even peaces, but the peaces should not be longer than the max 5, then this will
	 * return 4.5, as 4.5 goes 2 times to make up precisely 9.
	 * @param length The length which the returned factor should fit into
	 * @param max The max distance of the returned length
	 * @return The closest to length to be a factor of the provided length which is <= max
	 */

	public static double getRoundFactor(double length, double max)
	{
		return length/Math.ceil(length/max);
	}

	/**
	 * Get the closest RoundFactor length applied to a vector, using the vector as the max length. The returned vector is a cloned
	 * parallel vector to the provided length
	 * @param length The vector used as length and direction
	 * @param maxLength The max length of the new returned vector
	 * @return Returns a parallel vector to the given length vector which is also a factor of the provided vector, but not longer
	 * then maxLength
	 *
	 * @see #getRoundFactor(double, double)
	 */
	public static Vector getVectorFactor(Vector length, double maxLength)
	{
		return length.clone().multiply(getRoundFactor(length.length(), maxLength));
	}


}
