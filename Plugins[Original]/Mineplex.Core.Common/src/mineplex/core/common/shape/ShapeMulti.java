package mineplex.core.common.shape;

import java.util.Collection;

import org.bukkit.util.Vector;

/**
 * A bag collection of several shapes. This will add all the points from the given shapes into a new shape
 */

public class ShapeMulti extends Shape
{

	/**
	 * @param shapes Shapes which points will be added to this instance of a shape
	 */
	public ShapeMulti(Collection<Shape> shapes)
	{
		for(Shape shape : shapes) addShape(shape);
	}

	/**
	 * @param shapes Shapes which points will be added to this instance of a shape
	 */
	public ShapeMulti(Shape... shapes)
	{
		for(Shape shape : shapes) addShape(shape);
	}
	
	/**
	 * Add all the points from the given shape to this shape
	 * @param shape
	 */
	public void addShape(Shape shape) {
		for(Vector v : shape._points) add(v);
	}

}
