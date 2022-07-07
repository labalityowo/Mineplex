package mineplex.hub.news;

import org.bukkit.ChatColor;

public class NewsElement
{

	private final int _id;
	private final String _value;

	NewsElement(int id, String value)
	{
		_id = id;
		_value = ChatColor.translateAlternateColorCodes('&', value);
	}

	public int getId()
	{
		return _id;
	}

	public String getValue()
	{
		return _value;
	}
}
