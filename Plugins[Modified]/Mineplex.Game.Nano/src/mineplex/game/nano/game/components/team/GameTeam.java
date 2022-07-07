package mineplex.game.nano.game.components.team;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilServer;
import mineplex.core.world.MineplexWorld;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.event.PlayerStateChangeEvent;

public class GameTeam
{

	private final Game _game;
	private final String _name;
	private final ChatColor _chatColour;
	private final Color _colour;
	private final DyeColor _dyeColour;
	private final List<Location> _spawns;

	private final Map<Player, Boolean> _players;
	private final LinkedList<Player> _places;

	private int _overflowIndex;

	public GameTeam(Game game, String name, ChatColor chatColour, Color colour, DyeColor dyeColour, MineplexWorld mineplexWorld)
	{
		this(game, name, chatColour, colour, dyeColour, mineplexWorld.getGoldLocations(name));
	}

	public GameTeam(Game game, String name, ChatColor chatColour, Color colour, DyeColor dyeColour, List<Location> spawns)
	{
		_game = game;
		_name = name;
		_chatColour = chatColour;
		_colour = colour;
		_dyeColour = dyeColour;
		_spawns = spawns;
		_players = new HashMap<>();
		_places = new LinkedList<>();

		Collections.shuffle(spawns);
	}

	public String getName()
	{
		return _name;
	}

	public ChatColor getChatColour()
	{
		return _chatColour;
	}

	public Color getColour()
	{
		return _colour;
	}

	public DyeColor getDyeColour()
	{
		return _dyeColour;
	}

	public byte getWoolData()
	{
		return _dyeColour.getWoolData();
	}

	public Location getSpawn()
	{
		// If only 1 spawn, skip the effort and just return it
		if (getSpawns().size() == 1)
		{
			return getSpawns().get(0);
		}

		// Players are being teleported in and there are more players than spawns already in
		// If this is the case just start placing players randomly otherwise they'll all end up
		// at the same spawn.
		if (!_game.isLive() && getAllPlayers().size() > getSpawns().size())
		{
			_overflowIndex = (_overflowIndex + 1) % getSpawns().size();
			return getSpawns().get(_overflowIndex);
		}

		// Try and spawn the player as far away as possible from other players
		return UtilAlg.getLocationAwayFromPlayers(getSpawns(), _game.getAlivePlayers());
	}

	public List<Location> getSpawns()
	{
		return _spawns;
	}

	public void setPlayerAlive(Player player, boolean alive)
	{
		_players.put(player, alive);

		if (alive)
		{
			_places.remove(player);
		}
		else
		{
			_places.addFirst(player);
		}

		UtilServer.CallEvent(new PlayerStateChangeEvent(player, this, alive));
	}

	public void removePlayer(Player player)
	{
		_players.remove(player);
	}

	public Collection<Player> getAllPlayers()
	{
		return _players.keySet();
	}

	public boolean hasPlayer(Player player)
	{
		return _players.containsKey(player);
	}

	public List<Player> getAlivePlayers()
	{
		return _players.entrySet().stream()
				.filter(Entry::getValue)
				.map(Entry::getKey)
				.collect(Collectors.toList());
	}

	public boolean isAlive(Player player)
	{
		return _players.getOrDefault(player, false);
	}

	public void addPlacementTop(Player player)
	{
		_places.addFirst(player);
	}

	public void addPlacementBottom(Player player)
	{
		_places.addLast(player);
	}

	public List<Player> getPlaces(boolean includeAlive)
	{
		if (includeAlive)
		{
			LinkedList<Player> places = new LinkedList<>(_places);
			getAlivePlayers().forEach(places::addFirst);
			return places;
		}

		return _places;
	}

	public List<Player> getActualPlaces()
	{
		return _places;
	}
}
