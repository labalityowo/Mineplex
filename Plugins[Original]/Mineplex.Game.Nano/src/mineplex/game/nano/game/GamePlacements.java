package mineplex.game.nano.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

public class GamePlacements
{

	public static GamePlacements fromTeamPlacements(List<Player> players)
	{
		return new GamePlacements(players.stream()
				.map(Collections::singletonList)
				.collect(Collectors.toList()));
	}

	public static GamePlacements fromPlayerScore(Map<Player, Integer> entries)
	{
		Map<Integer, List<Player>> sorted = new TreeMap<>(Comparator.reverseOrder());

		entries.forEach((player, score) -> sorted.computeIfAbsent(score, k -> new ArrayList<>()).add(player));

		return new GamePlacements(new ArrayList<>(sorted.values()));
	}

	private final List<List<Player>> _placements;

	private GamePlacements(List<List<Player>> placements)
	{
		_placements = placements;
	}

	public List<Player> getPlayersAtPlace(int position)
	{
		if (position < 0 || position >= _placements.size())
		{
			return null;
		}

		return _placements.get(position);
	}

	public List<Player> getWinners()
	{
		return getPlayersAtPlace(0);
	}

	public boolean hasPlacements()
	{
		return !_placements.isEmpty();
	}
}
