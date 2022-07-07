package mineplex.core.common.shape;

import org.bukkit.util.Vector;

/**
 * A simple sphere defined using vector points extending {@link Shape}
 */

public class ShapeSphere extends Shape
{

	/**
	 * Define a sphere with radius r that is not hollow and using default density {@link Shape#DefaultDensity}
	 * @param r Radius for the sphere
	 */
	public ShapeSphere(double r)
	{
		this(r,r,r);
	}
	
	/**
	 * A sphere with different radiuses on different planes that is not hollow and using default density {@link Shape#DefaultDensity}
	 * @param x Radius in x direction
	 * @param y Radius in y direction
	 * @param z Radius in z direction
	 */
	public ShapeSphere(double x, double y, double z)
	{
		this(x, y, z, false, DEFAULT_DENSITY);
	}
	
	/**
	 * A sphere with different radiuses on different planes using default density {@link Shape#DefaultDensity}
	 * @param x Radius in x direction
	 * @param y Radius in y direction
	 * @param z Radius in z direction
	 * @param hollow If the sphere should be hollow or not
	 */
	public ShapeSphere(double x, double y, double z, boolean hollow)
	{
		this(x, y, z, hollow, DEFAULT_DENSITY);
	}
	
	/**
	 * A sphere with different radiuses on different planes
	 * @param x Radius in x direction
	 * @param y Radius in y direction
	 * @param z Radius in z direction
	 * @param hollow If the sphere should be hollow or not
	 * @param density Density between points
	 */
	public ShapeSphere(double x, double y, double z, boolean hollow, double density)
	{
		for(double px = -x; px <= x; x += Shape.getRoundFactor(2*x, density))
		{
			for(double py = -y; py <= y; y += Shape.getRoundFactor(2*y, density))
			{
				for(double pz = -z; pz <= z; z += Shape.getRoundFactor(2*z, density))
				{
					if(		 hollow && px*px/x*x + py*py/y*y + pz*pz/z*z == 1) _points.add(new Vector(px,py,pz));
					else if(!hollow && px*px/x*x + py*py/y*y + pz*pz/z*z <= 1) _points.add(new Vector(px,py,pz));
				}
			}
		}
	}

}
