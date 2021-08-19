package mineplex.game.nano.game.roomed;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Room
{

	private final Player _player;
	private final Location _center;
	private final Map<String, Location> _dataPoints;

	public Room(Player player, Location center, Map<String, Location> dataPoints)
	{
		_player = player;
		_center = center;
		_dataPoints = dataPoints;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public Location getCenter()
	{
		return _center;
	}

	public Map<String, Location> getDataPoints()
	{
		return _dataPoints;
	}
}
