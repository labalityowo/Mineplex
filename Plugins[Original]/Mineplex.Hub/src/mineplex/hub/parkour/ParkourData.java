package mineplex.hub.parkour;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.world.MineplexWorld;

public class ParkourData implements Listener
{

	private static final int MAX_FALL_DISTANCE = 7;
	private static final double CHECKPOINT_PERCENTAGE = 0.8;

	protected final ParkourManager _manager;
	private final String _name, _key;
	private final String[] _description;
	private final int _difficulty;
	private final List<Location> _checkpoints;
	protected final Location _reset;
	private final Location _teleport, _cornerA, _cornerB;

	public ParkourData(ParkourManager manager, String name, String[] description, int difficulty)
	{
		this(manager, name, name.toUpperCase(), description, difficulty);
	}

	public ParkourData(ParkourManager manager, String name, String key, String[] description, int difficulty)
	{
		_manager = manager;
		_name = name;
		_key = key;
		_description = description;
		_difficulty = difficulty;

		MineplexWorld worldData = manager.getHubManager().getWorldData();
		List<Location> corners = worldData.getSpongeLocations(key + " BORDER");
		_checkpoints = worldData.getSpongeLocations(key + " CHECK");
		_reset = worldData.getSpongeLocation(key + " RESET");
		UtilAlg.lookAtNearest(_reset, manager.getHubManager().getLookAt());
		_teleport = worldData.getSpongeLocation(key + " TELEPORT");
		UtilAlg.lookAtNearest(_teleport, manager.getHubManager().getLookAt());

		_cornerA = corners.get(0);
		_cornerA.setY(0);
		_cornerB = corners.get(1);
		_cornerB.setY(_cornerB.getWorld().getMaxHeight());

		UtilServer.RegisterEvents(this);
	}

	public void onStart(Player player)
	{
	}

	public void onEnd(Player player)
	{
	}

	@EventHandler
	public void updateFail(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (Entry<Player, ParkourAttempt> entry : _manager.getActivePlayers(this))
		{
			Player player = entry.getKey();

			if (hasFailed(player))
			{
				reset(player);
				_manager.startParkour(player, this);
			}
		}
	}

	public void reset(Player player)
	{
		player.teleport(_reset);
	}

	protected boolean hasFailed(Player player)
	{
		return player.getFallDistance() > MAX_FALL_DISTANCE || UtilEnt.isInWater(player);
	}

	public String getName()
	{
		return _name;
	}

	public String getKey()
	{
		return _key;
	}

	public String[] getDescription()
	{
		return _description;
	}

	public int getDifficulty()
	{
		return _difficulty;
	}

	public Location getTeleport()
	{
		return _teleport;
	}

	public boolean cheatCheck(ParkourAttempt attempt)
	{
		return attempt.getCheckpoints().size() > (_checkpoints.size() * CHECKPOINT_PERCENTAGE);
	}

	public boolean isInArea(Location location)
	{
		return UtilAlg.inBoundingBox(location, _cornerA, _cornerB);
	}

	public List<Location> getCheckpoints()
	{
		return _checkpoints;
	}
}
