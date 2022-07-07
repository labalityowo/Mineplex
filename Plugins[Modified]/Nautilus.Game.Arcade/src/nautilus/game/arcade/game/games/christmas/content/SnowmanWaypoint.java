package nautilus.game.arcade.game.games.christmas.content;

import org.bukkit.Location;

public class SnowmanWaypoint
{
	public Location Last;
	public Location Target;
	public CardinalDirection Direction = CardinalDirection.NULL;
	public long Time;
	
	public SnowmanWaypoint(Location last)
	{
		Last = last;
		Target = null;
		Time = System.currentTimeMillis();
	}
	
	public static enum CardinalDirection
	{
		NORTH, SOUTH, EAST, WEST, NULL // such order much not care
	}
}
