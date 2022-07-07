package mineplex.core.common.geom;

import javax.annotation.Nonnull;
import java.util.Comparator;

import org.bukkit.Location;

public class Point2D implements Comparable<Point2D>
{
	private static final Comparator<Point2D> COMPARE_BY_Y_THEN_X =
			Comparator.comparingDouble(Point2D::getY).thenComparingDouble(Point2D::getX);
	private final double _x;
	private final double _y;

	private Point2D(double x, double y)
	{
		_x = x;
		_y = y;
	}

	public static Point2D of(double x, double y)
	{
		return new Point2D(x, y);
	}

	public static Point2D of(Location location)
	{
		return new Point2D(location.getX(), location.getZ());
	}

	public double getX()
	{
		return _x;
	}

	public double getY()
	{
		return _y;
	}

	public Comparator<Point2D> polarOrder()
	{
		return (p2, p3) ->
		{
			double dx1 = p2._x - _x;
			double dy1 = p2._y - _y;
			double dx2 = p3._x - _x;
			double dy2 = p3._y - _y;

			if      (dy1 >= 0 && dy2 < 0) return -1; // p2 above; p3 below
			else if (dy2 >= 0 && dy1 < 0) return  1; // p2 below; p3 above
			else if (dy1 == 0 && dy2 == 0) {         // 3-collinear and horizontal
				if      (dx1 >= 0 && dx2 < 0) return -1; // p2 right; p3 left
				else if (dx2 >= 0 && dx1 < 0) return  1; // p2 left ; p3 right
				else                          return  0; // all the same point
			}
			else return -ccw(Point2D.this, p2, p3); // both above or below
		};
	}

	public static int ccw(Point2D a, Point2D b, Point2D c) {
		double area2 = (b._x-a._x)*(c._y-a._y) - (b._y-a._y)*(c._x-a._x);
		if      (area2 < 0) return -1;
		else if (area2 > 0) return  1;
		else                return  0;
	}

	@Override
	public int compareTo(@Nonnull Point2D that)
	{
		return COMPARE_BY_Y_THEN_X.compare(this, that);
	}

	@Override
	public String toString()
	{
		return "Point2D{_x=" + _x + ",_y=" + _y + "}";
	}
}
