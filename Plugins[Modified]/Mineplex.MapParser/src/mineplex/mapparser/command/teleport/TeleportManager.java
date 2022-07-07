package mineplex.mapparser.command.teleport;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.Pair;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;

public class TeleportManager
{
	private MapParser _plugin;
	private Map<Player, LinkedList<Pair<Vector, String>>> _teleportHistory;

	public TeleportManager(MapParser plugin)
	{
		_plugin = plugin;
		_teleportHistory = new HashMap<>();
	}

	private void addToHistory(Player player, Location location)
	{
		_teleportHistory.computeIfAbsent(player, key -> new LinkedList<>()).addFirst(Pair.create(new Vector(location.getX(), location.getY(), location.getZ()), location.getWorld().getName()));
	}

	public LinkedList<Pair<Vector, String>> getTeleportHistory(Player player)
	{
		return _teleportHistory.get(player);
	}

	public void teleportPlayer(Player player, Location destination)
	{
		addToHistory(player, player.getLocation().clone());
		player.teleport(destination);
	}

	public void teleportPlayer(Player player, Player destination)
	{
		teleportPlayer(player, destination.getLocation());
	}

	public boolean canTeleportTo(Player player, Location target)
	{
		if (target.getWorld().getName().equals("world") || target.getWorld().getName().equals("world_lobby"))
		{
			return true;
		}

		MapData data = getPlugin().getData(target.getWorld().getName());

		return data.CanJoin(player);
	}

	public MapParser getPlugin()
	{
		return _plugin;
	}
}
