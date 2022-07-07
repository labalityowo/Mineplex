package mineplex.hub.modules.mavericks.basketball;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Data class to store information about the last throw of a ball
 */
public class ThrowData
{
	private BasketballTeam _team;
	private Location _location;
	
	public ThrowData(Player thrower, BasketballTeam team)
	{
		_team = team;
		_location = thrower.getEyeLocation();
	}
	
	/**
	 * Request the player who threw the ball
	 * @return The player who threw the ball
	 */
	public BasketballTeam getThrower()
	{
		return _team;
	}
	
	/**
	 * Requests the location where the ball was thrown from
	 * @return The origin location of the throw
	 */
	public Location getThrowOrigin()
	{
		return _location;
	}
}