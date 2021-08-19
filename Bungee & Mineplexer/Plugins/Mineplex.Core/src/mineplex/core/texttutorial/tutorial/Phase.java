package mineplex.core.texttutorial.tutorial;

import org.bukkit.Location;

public class Phase
{
	private Location _location;
	private String _header;
	private String[] _text;

	public Phase(Location location, String header, String[] text)
	{
		_location = location;
		_header = header;
		_text = text;
	}

	public Location getLocation()
	{
		return _location;
	}

	public void setLocation(Location location)
	{
		_location = location;
	}

	public String getHeader()
	{
		return _header;
	}

	public void setHeader(String header)
	{
		_header = header;
	}

	public String[] getText()
	{
		return _text;
	}

	public void setText(String[] text)
	{
		_text = text;
	}
}
