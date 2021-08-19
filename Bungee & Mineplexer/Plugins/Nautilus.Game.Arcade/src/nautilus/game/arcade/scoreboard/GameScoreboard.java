package nautilus.game.arcade.scoreboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.scoreboard.WritableMineplexScoreboard;
import mineplex.core.titles.tracks.custom.ScrollAnimation;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;

public class GameScoreboard extends WritableMineplexScoreboard
{

	private static final String[] TITLE = new ScrollAnimation("  MINEPLEX  ")
			.withPrimaryColour(ChatColor.GOLD)
			.withSecondaryColour(ChatColor.YELLOW)
			.withTertiaryColour(ChatColor.WHITE)
			.bold()
			.build();

	private final Game _game;

	private int _shineIndex;

	public GameScoreboard(Game game)
	{
		this(game, null);
	}

	public GameScoreboard(Game game, Player player)
	{
		super(player);

		_game = game;
		setSidebarName(TITLE[0]);
	}
	
	@Override
	public void draw()
	{
		if (_bufferedLines.size() > 15)
		{
			while (_bufferedLines.size() > 15)
			{
				_bufferedLines.remove(_bufferedLines.size() - 1);
			}
		}
		super.draw();
	}

	public Scoreboard getScoreboard()
	{
		return getHandle();
	}

	public void updateTitle()
	{
		setSidebarName(TITLE[_shineIndex]);

		if (++_shineIndex == TITLE.length)
		{
			_shineIndex = 0;
		}
	}

	public void setPlayerTeam(Player player, GameTeam gameTeam)
	{
		String teamId = _game.getArcadeManager().GetLobby().getTeamId(gameTeam, player);

		if (getHandle().getTeam(teamId) == null)
		{
			Team targetTeam = getHandle().registerNewTeam(teamId);

			if (gameTeam.GetDisplaytag())
			{
				targetTeam.setPrefix(gameTeam.GetColor() + C.Bold + gameTeam.GetName() + gameTeam.GetColor() + " ");
				targetTeam.setSuffix(C.Reset);
			}
			else
			{
				targetTeam.setPrefix(gameTeam.GetColor() + "");
				targetTeam.setSuffix(C.Reset);
			}
		}

		setPlayerTeam(player, teamId);
	}

	public void setSpectating(Player player)
	{
		if (getHandle().getTeam("SPEC") == null)
		{
			getHandle().registerNewTeam("SPEC").setPrefix(C.cGray);
		}
		setPlayerTeam(player, "SPEC");
	}

	private void setPlayerTeam(Player player, String teamId)
	{
		for (Team team : getHandle().getTeams())
			team.removeEntry(player.getName());

		getHandle().getTeam(teamId).addEntry(player.getName());
	}

	public <T> void writeGroup(Collection<T> players, Function<T, Pair<String, Integer>> score, boolean prependScore)
	{
		Map<T, Integer> scores = new HashMap<>();
		Map<T, String> names = new HashMap<>();

		for (T player : players)
		{
			Pair<String, Integer> result = score.apply(player);
			if (result == null) continue;
			scores.put(player, result.getRight());
			names.put(player, result.getLeft());
		}

		scores = sortByValue(scores);

		for (Map.Entry<T, Integer> entry : scores.entrySet())
		{
			String line = names.get(entry.getKey());
			if (prependScore)
			{
				line = entry.getValue() + " " + line;
			}
			write(line);
		}
	}

	private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map)
	{
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		list.sort((o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}