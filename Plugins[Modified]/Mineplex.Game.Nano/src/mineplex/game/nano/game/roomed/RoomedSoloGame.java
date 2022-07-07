package mineplex.game.nano.game.roomed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scoreboard.NameTagVisibility;

import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.UtilSchematic;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.ScoredSoloGame;
import mineplex.game.nano.game.event.PlayerGameApplyEvent;

public abstract class RoomedSoloGame<T extends Room> extends ScoredSoloGame
{

	private static final int MAX_XZ = 5;

	protected final Map<Player, T> _rooms;

	private Location _spawn, _relative;
	private Schematic _schematic;
	private int _x = -MAX_XZ, _z = -MAX_XZ;

	public RoomedSoloGame(NanoManager manager, GameType gameType, String[] description)
	{
		super(manager, gameType, description);

		_rooms = new HashMap<>();

		_scoreboardComponent.setSetupSettingsConsumer((player, team, scoreboardTeam) -> scoreboardTeam.setNameTagVisibility(NameTagVisibility.NEVER));
	}

	@Override
	protected void parseData()
	{
		_spawn = _playersTeam.getSpawn();
		_relative = _mineplexWorld.getMin().clone();
		_schematic = UtilSchematic.createSchematic(_mineplexWorld.getMin(), _mineplexWorld.getMax());

		Location min = _mineplexWorld.getMin(), max = _mineplexWorld.getMax();

		min.setX(-256);
		min.setZ(-256);
		max.setX(256);
		max.setZ(256);
	}

	@Override
	public void disable()
	{
		_schematic = null;
	}

	protected abstract T addPlayer(Player player, Location location, Map<String, Location> localPoints);

	@EventHandler
	public void respawn(PlayerGameApplyEvent event)
	{
		Player player = event.getPlayer();
		Room room = createRoom(player);

		event.setRespawnLocation(event.getRespawnLocation().add(room.getCenter()));
	}

	private T createRoom(Player player)
	{
		if (++_x > MAX_XZ)
		{
			_x = -MAX_XZ;
			_z++;
		}

		double xAdd = _x * _schematic.getLength(), zAdd = _z * _schematic.getWidth();

		getManager().runSyncLater(() -> _schematic.paste(_relative.clone().add(xAdd, 0, zAdd), true, false, false), 1);

		Map<String, List<Location>> globalPoints = _mineplexWorld.getIronLocations();
		Map<String, Location> localPoints = new HashMap<>(globalPoints.size());

		globalPoints.forEach((key, locations) -> localPoints.put(key, locations.get(0).clone().add(xAdd, 0, zAdd)));

		T room = addPlayer(player, _spawn.clone().add(xAdd, 0, zAdd), localPoints);
		_rooms.put(player, room);

		return room;
	}
}
