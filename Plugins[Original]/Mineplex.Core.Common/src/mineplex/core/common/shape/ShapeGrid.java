package mineplex.core.common.shape;

import org.bukkit.util.Vector;

/**
 * A simple grid shape which uses string inputs to define points
 */

public class ShapeGrid extends Shape
{
	
	/**
	 * Each string in the array represents a layer on the XY-plane, meaning the layers moves towards positive Z.
	 * Each line in the string represents a line on parallel with the X-axis, where the first line is on the top of the shape.
	 * Use '#' for each point and anything else for each "non-point".
	 * The finished shape will then be centered afterwards.
	 */
	public ShapeGrid(String... input)
	{
		this(DEFAULT_DENSITY, '#', input);
	}

	/**
	 * Each string in the array represents a layer on the XY-plane, meaning the layers moves towards positive Z.
	 * Each line in the string represents a line on parallel with the X-axis, where the first line is on the top of the shape.
	 * Use the <code>read</code> char for each point and anything else for each "non-point".
	 * The finished shape will then be centered afterwards.
	 */
	public ShapeGrid(char read, String...input)
	{
		this(DEFAULT_DENSITY, read, input);
	}

	/**
	 * Each string in the array represents a layer on the XY-plane, meaning the layers moves towards positive Z.
	 * Each line in the string represents a line on parallel with the X-axis.
	 * Use the <code>read</code> char for each point and anything else for each "non-point".
	 * The finished shape will then be centered afterwards.
	 */
	public ShapeGrid(double density, char read, String...input)
	{
		int lx = 0;
		int ly = 0;
		int lz = 0;
		for(int y = 0; y < input.length; y++)
		{
			String[] lines = input[y].split("\n");
			for(int z = 0; z < lines.length; z++)
			{
				String line = lines[z];
				for(int x = 0; x < line.length(); x++)
				{
					if(line.charAt(x) == read) addPoint(new Vector(-x,-y+input.length,-z).multiply(density));
					if(x > lx) lx = x;
					if(-y+input.length > ly) ly = -y+input.length;
					if(z > lz) lz= z;
				}
			}
		}
		
		add(new Vector(-lx,ly,-lz).multiply(-0.5*density));
	}

}
