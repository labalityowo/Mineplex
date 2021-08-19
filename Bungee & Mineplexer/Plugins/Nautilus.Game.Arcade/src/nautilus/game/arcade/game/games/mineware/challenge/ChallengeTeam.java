package nautilus.game.arcade.game.games.mineware.challenge;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;

/**
 * This class holds a list of players under a common group name, that represents a team.
 */
public class ChallengeTeam
{
	private String _name;
	private Set<Player> _players = new LinkedHashSet<>();

	public ChallengeTeam(String name)
	{
		_name = name;
	}

	public String getName()
	{
		return _name;
	}

	public void add(Player player)
	{
		_players.add(player);
	}

	public void remove(Player player)
	{
		_players.remove(player);
	}

	public void reset()
	{
		_players.clear();
	}

	public boolean isMember(Player player)
	{
		return _players.contains(player);
	}

	public int getSize()
	{
		return _players.size();
	}

	public List<Player> getPlayers()
	{
		return new ArrayList<>(_players);
	}
}
