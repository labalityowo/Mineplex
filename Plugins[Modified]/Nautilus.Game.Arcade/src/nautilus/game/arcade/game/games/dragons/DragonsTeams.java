package nautilus.game.arcade.game.games.dragons;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.modules.TeamArmorModule;

public class DragonsTeams extends Dragons
{

	private final Map<GameTeam, Integer> _scores;

	public DragonsTeams(ArcadeManager manager)
	{
		super(manager, GameType.Dragons, new String[]
				{
						"You have angered the Dragons!",
						"Survive as best you can!!!",
						"Team with longest time survived wins!"
				});

		_scores = new HashMap<>();

		new TeamArmorModule()
				.giveTeamArmor()
				.giveHotbarItem()
				.register(this);
	}

	@Override
	@EventHandler
	public void CustomTeamGeneration(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
		{
			return;
		}

		GameTeam team = GetTeamList().get(0);
		GetTeamList().clear();

		List<Location> spawns = team.GetSpawns();

		AddTeam(new GameTeam(this, "Red", ChatColor.RED, spawns));
		AddTeam(new GameTeam(this, "Yellow", ChatColor.YELLOW, spawns));
		AddTeam(new GameTeam(this, "Green", ChatColor.GREEN, spawns));
		AddTeam(new GameTeam(this, "Blue", ChatColor.AQUA, spawns));
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		GetTeamList().forEach(team -> _scores.put(team, 0));
	}

	@EventHandler
	public void updateScores(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !IsLive())
		{
			return;
		}

		for (GameTeam team : GetTeamList())
		{
			List<Player> alive = team.GetPlayers(true);

			if (alive.isEmpty())
			{
				continue;
			}

			_scores.put(team, _scores.get(team) + alive.size());
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !InProgress())
		{
			return;
		}

		Scoreboard.writeNewLine();

		_scores.forEach((team, score) ->
		{
			Scoreboard.write(team.GetFormattedName());
			Scoreboard.write(score + " Points");
			Scoreboard.writeNewLine();
		});

		Scoreboard.draw();
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		for (GameTeam team : _scores.keySet())
		{
			if (team.IsTeamAlive())
			{
				return;
			}
		}

		List<GameTeam> placements = _scores.entrySet().stream()
				// Reversed natural ordering
				.sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
				.map(Entry::getKey)
				.collect(Collectors.toList());

		if (placements.isEmpty())
		{
			return;
		}

		GameTeam winner = placements.get(0);

		GetPlayers(false).forEach(player ->
		{
			if (!player.isOnline())
			{
				return;
			}

			AddGems(player, 10, "Participation", false, false);

			if (winner.HasPlayer(player))
			{
				AddGems(player, 20, "Winning Team", false, false);
			}
		});

		AnnounceEnd(winner);
		SetState(GameState.End);
	}

	@Override
	public List<Player> getWinners()
	{
		if (WinnerTeam == null)
		{
			return Collections.emptyList();
		}

		return WinnerTeam.GetPlayers(true);
	}

	@Override
	public List<Player> getLosers()
	{
		if (WinnerTeam == null)
		{
			return Collections.emptyList();
		}

		List<Player> losers = GetPlayers(false);
		losers.removeAll(getWinners());

		return losers;
	}

	@Override
	public String GetMode()
	{
		return "Teams Mode";
	}
}
