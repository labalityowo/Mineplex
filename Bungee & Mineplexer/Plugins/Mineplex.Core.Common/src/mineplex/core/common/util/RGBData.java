package mineplex.core.common.util;

import org.bukkit.util.Vector;

public class RGBData
{
	private double _red;
	private double _green;
	private double _blue;
	
	public RGBData(int red, int green, int blue)
	{
		_red = UtilMath.clamp(((double) red) / 255.d, 0, 1);
		_green = UtilMath.clamp(((double) green) / 255.d, 0, 1);
		_blue =  UtilMath.clamp(((double) blue) / 255.d, 0, 1);
	}
	
	public int getFullRed()
	{
		return (int) (_red * 255);
	}
	
	public int getFullGreen()
	{
		return (int) (_green * 255);
	}
	
	public int getFullBlue()
	{
		return (int) (_blue * 255);
	}
	
	public double getRed()
	{
		return _red;
	}
	
	public double getGreen()
	{
		return _green;
	}
	
	public double getBlue()
	{
		return _blue;
	}
	
	public String toString()
	{
		return "RGB["
			 + "red=" + (int) (_red * 255) + ", "
			 + "green=" + (int) (_green * 255) + ", "
			 + "blue=" + (int) (_blue * 255) + "]";
	}

	public Vector ToVector()
	{
		return new Vector(Math.max(0.001, _red), _green, _blue);
	}

	public RGBData Darken()
	{
		return new RGBData(getFullRed() - 25, getFullGreen() - 25, getFullBlue() - 25);
	}
	
	public RGBData Lighten()
	{
		return new RGBData(getFullRed() + 25, getFullGreen() + 25, getFullBlue() + 25);
	}
}
