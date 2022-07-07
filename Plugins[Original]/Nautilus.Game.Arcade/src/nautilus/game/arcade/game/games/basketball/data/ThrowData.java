package nautilus.game.arcade.game.games.basketball.data;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Data class to store information about the last throw of a ball
 */
public class ThrowData
{
	private Player _player;
	private Location _location;
	
	public ThrowData(Player thrower)
	{
		_player = thrower;
		_location = thrower.getEyeLocation();
	}
	
	/**
	 * Request the player who threw the ball
	 * @return The player who threw the ball
	 */
	public Player getThrower()
	{
		return _player;
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
