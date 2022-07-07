package mineplex.core.common.geom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Polygon2D
{
	private final List<Point2D> _points;

	private Polygon2D(List<Point2D> points)
	{
		_points = points;
		// Ensure points[points.size-1] = points[0]
		if (!_points.get(0).equals(points.get(_points.size()-1)))
		{
			_points.add(points.get(0));
		}
	}

	public boolean contains(Point2D point)
	{
		boolean result = false;
		for (int i = 0, j = _points.size() - 1; i < _points.size(); j = i++)
		{
			if ((_points.get(i).getY() > point.getY()) != (_points.get(j).getY() > point.getY()) &&
					(point.getX() < (_points.get(j).getX() - _points.get(i).getX()) * (point.getY() - _points.get(i).getY()) / (_points.get(j).getY() - _points.get(i).getY()) + _points.get(i).getX()))
			{
				result = !result;
			}
		}
		return result;
	}

	public static Polygon2D fromUnorderedPoints(List<Point2D> points)
	{
		Stack<Point2D> hull = new Stack<>();

		Collections.sort(points);
		points.subList(1, points.size()).sort(points.get(0).polarOrder());

		hull.push(points.get(0));
		hull.push(points.get(1));

		// find first extreme point (not collinear with first and second elements)
		int extreme;
		for (extreme = 2; extreme < points.size(); extreme++)
			if (Point2D.ccw(points.get(0), points.get(1), points.get(extreme)) != 0) break;

		for (int i = extreme; i < points.size(); i++)
		{
			Point2D top = hull.pop();
			while (Point2D.ccw(hull.peek(), top, points.get(i)) <= 0) {
				top = hull.pop();
			}
			hull.push(top);
			hull.push(points.get(i));
		}

		return new Polygon2D(new ArrayList<>(hull));
	}

	public static Polygon2D fromPoints(List<Point2D> points)
	{
		return new Polygon2D(new ArrayList<>(points));
	}
}
