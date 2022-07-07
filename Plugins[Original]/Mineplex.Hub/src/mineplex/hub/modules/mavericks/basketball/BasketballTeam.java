package mineplex.hub.modules.mavericks.basketball;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Enum containing all teams for hub basketball
 */
public enum BasketballTeam
{
	RED(Color.RED, ChatColor.RED, "Red", DataLoc.RED_SPAWNS),
	BLUE(Color.AQUA, ChatColor.AQUA, "Blue", DataLoc.BLUE_SPAWNS)
	;
	
	private Color _color;
	private ChatColor _cColor;
	private String _name;
	private DataLoc _spawns;
	
	private BasketballTeam(Color color, ChatColor cColor, String name, DataLoc spawns)
	{
		_color = color;
		_cColor = cColor;
		_name = name;
		_spawns = spawns;
	}
	
	/**
	 * Fetches the color of this team's uniform
	 * @return The color of this team's uniform
	 */
	public Color getColor()
	{
		return _color;
	}
	
	/**
	 * Fetches the color of this team in chat
	 * @return The color of this team in chat
	 */
	public ChatColor getChatColor()
	{
		return _cColor;
	}
	
	/**
	 * Fetches the display name of this team
	 * @return The display name of this team
	 */
	public String getName()
	{
		return _cColor + _name + " Team";
	}
	
	/**
	 * Fetches all possible spawns for this team
	 * @param world The world to create the locations in
	 * @return All possible spawns for this team
	 */
	public Location[] getSpawns(World world)
	{
		return _spawns.getLocations(world);
	}
	
	/**
	 * Fetches the team matching a color
	 * @param color The color to match a team to
	 * @return The team matching the color, or null if one is not found
	 */
	public static BasketballTeam getFromColor(Color color)
	{
		for (BasketballTeam team : BasketballTeam.values())
		{
			if (team.getColor().asRGB() == color.asRGB())
			{
				return team;
			}
		}
		
		return null;
	}
}