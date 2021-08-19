package nautilus.game.arcade.game.games.monstermaze;

import org.bukkit.Location;

public class MazeMobWaypoint
{
	public Location Last;
	public Location Target;
	public CardinalDirection Direction = CardinalDirection.NULL;
	
	public MazeMobWaypoint(Location last)
	{
		Last = last;
		Target = null;
	}
	
	public enum CardinalDirection
	{
		NORTH, SOUTH, EAST, WEST, NULL // such order much not care
	}
}
