package mineplex.hub.parkour.data;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.hub.parkour.ParkourData;
import mineplex.hub.parkour.ParkourManager;

public class SprintingParkourData extends ParkourData
{

	private static final double MIN_MOVE_DISTANCE_SQUARED = 0.6;

	private final ParkourManager _manager;
	private final Map<Player, Location> _lastLocation;
	private final int _minY;

	public SprintingParkourData(ParkourManager manager, String name, String[] description, int difficulty)
	{
		super(manager, name, description, difficulty);

		_manager = manager;
		_lastLocation = new HashMap<>();
		_minY = _reset.getBlockY() + 2;
	}

	@Override
	public void onStart(Player player)
	{
		_lastLocation.put(player, player.getLocation());
	}

	@Override
	public void onEnd(Player player)
	{
		_lastLocation.remove(player);
	}

	@Override
	protected boolean hasFailed(Player player)
	{
		Location location = player.getLocation();

		if (location.getY() > _minY && UtilMath.offsetSquared(location, _lastLocation.get(player)) < MIN_MOVE_DISTANCE_SQUARED)
		{
			player.sendMessage(F.main(_manager.getName(), "You cannot stop running on the " + F.name(getName()) + " Parkour."));
			return true;
		}

		_lastLocation.put(player, location);
		return super.hasFailed(player);
	}
}
