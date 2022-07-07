package mineplex.core.common.util;

import org.bukkit.Location;

public class RadarData 
{
	public Location Loc;
	public String Text;
	
	private double _bearing = 0;
	
	public RadarData(Location loc, String text)
	{
		Loc = loc;
		Text = text;
	}

	public void print() 
	{
		System.out.println(Text + ": " + _bearing);
	}

	public void setBearing(double d)
	{
		while (d < -180)
			d += 360;
		
		while (d > 180)
			d -= 360;
		
		_bearing = d;
	}
	
	public double getBearing()
	{
		return _bearing;
	}
}
