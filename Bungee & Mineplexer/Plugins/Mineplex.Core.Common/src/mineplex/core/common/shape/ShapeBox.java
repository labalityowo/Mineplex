package mineplex.core.common.shape;

import org.bukkit.util.Vector;

/**
 * An extension of {@link Shape} creating a simple box
 */

public class ShapeBox extends Shape
{
	
	/**
	 * Define a parallelepiped using three vectors using default density {@link Shape#DefaultDensity} and is not hollow
	 * @param localx The first vector to use as local x direction, does not have to align to global x direction
	 * @param localy The second vector to use as local y direction, does not have to align to global y direction 
	 * @param localz The third vector to use as local z direction, does not have to align to global z direction
	 */

	public ShapeBox(Vector localx, Vector localy, Vector localz)
	{
		this(localx, localy, localz, false, DEFAULT_DENSITY);
	}
	
	/**
	 * Define a parallelepiped using three vectors using default density {@link Shape#DefaultDensity}
	 * @param localx The first vector to use as local x direction, does not have to align to global x direction
	 * @param localy The second vector to use as local y direction, does not have to align to global y direction 
	 * @param localz The third vector to use as local z direction, does not have to align to global z direction
	 * @param hollow If the parallelepiped box should be hollow or not
	 */
	
	public ShapeBox(Vector localx, Vector localy, Vector localz, boolean hollow)
	{
		this(localx, localy, localz, hollow, DEFAULT_DENSITY);
	}

	/**
	 * Define a parallelepiped using three vectors 
	 * @param localx The first vector to use as local x direction, does not have to align to global x direction
	 * @param localy The second vector to use as local y direction, does not have to align to global y direction 
	 * @param localz The third vector to use as local z direction, does not have to align to global z direction
	 * @param hollow If the parallelepiped box should be hollow or not
	 * @param density The density of the vector points
	 */
	public ShapeBox(Vector localx, Vector localy, Vector localz, boolean hollow, double density)
	{
		Vector x = Shape.getVectorFactor(localx, density);
		Vector y = Shape.getVectorFactor(localx, density);
		Vector z = Shape.getVectorFactor(localx, density);
		
		int xm = (int) Math.sqrt(localx.lengthSquared()/x.lengthSquared());
		int ym = (int) Math.sqrt(localy.lengthSquared()/y.lengthSquared());
		int zm = (int) Math.sqrt(localz.lengthSquared()/z.lengthSquared());
		
		for(int ix = 0; ix < xm; ix++)
		{
			for(int iy = 0; iy < ym; iy++)
			{
				for(int iz = 0; iz < zm; iz++)
				{
					if(hollow)
					{
						if(!(ix == 0 || ix == xm-1 || iy == 0 || iy == ym-1 || iz == 0 || iz == zm-1)) continue;
					} 
					_points.add(x.clone().multiply(ix).add(y.clone().multiply(iy)).add(z.clone().multiply(iz)));
				}
			}
		}
	}

}
