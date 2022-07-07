package nautilus.game.arcade.game;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.PlayerStateChangeEvent;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class TeamGame extends Game
{
	protected ArrayList<GameTeam> _places = new ArrayList<GameTeam>();

	public TeamGame(ArcadeManager manager, GameType gameType, Kit[] kits, String[] gameDesc)
	{
		super(manager, gameType, kits, gameDesc);
	}

	@EventHandler
	public void EndStateChange(PlayerStateChangeEvent event)
	{
		GameTeam team = this.GetTeam(event.GetPlayer());
		if (team == null)
			return;

		if (event.GetState() == PlayerState.OUT)
			if (!team.IsTeamAlive())
				_places.add(0, team);

			else
				_places.remove(team);
	}

	public ArrayList<GameTeam> GetPlaces()
	{
		return _places;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void PlayerQuit(PlayerQuitEvent event)
	{
		if (!InProgress())
			return;

		Player player = event.getPlayer();

		GameTeam team = GetTeam(player);
		if (team == null) return;

		if (!team.IsAlive(player))
			return;

		team.RemovePlayer(player);
	}

	public void EndCheck()
	{
		if (!IsLive())
			return;

		ArrayList<GameTeam> teamsAlive = new ArrayList<GameTeam>();

		for (GameTeam team : this.GetTeamList())
			if (team.GetPlayers(true).size() > 0)
				teamsAlive.add(team);

		if (teamsAlive.size() <= 1)
		{
			//Announce
			if (teamsAlive.size() > 0)
				AnnounceEnd(teamsAlive.get(0));

			for (GameTeam team : GetTeamList())
			{
				if (WinnerTeam != null && team.equals(WinnerTeam))
				{
					for (Player player : team.GetPlayers(false))
						AddGems(player, 10, "Winning Team", false, false);
				}

				for (Player player : team.GetPlayers(false))
					if (player.isOnline())
						AddGems(player, 10, "Participation", false, false);
			}

			//End
			SetState(GameState.End);
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event != null && event.getType() != UpdateType.FAST)
			return;

		Scoreboard.reset();

		for (GameTeam team : this.GetTeamList())
		{
			//Display Individual Players
			if (this.GetPlayers(true).size() < 13)
			{
				if (!team.IsTeamAlive())
					continue;

				Scoreboard.writeNewLine();

				for (Player player : team.GetPlayers(true))
				{
					Scoreboard.write(team.GetColor() + player.getName());
				}
			}

			//Display Players Alive
			else
			{
				Scoreboard.writeNewLine();

				Scoreboard.write(team.GetColor() + team.GetName());
				Scoreboard.write(team.GetPlayers(true).size() + "" + team.GetColor() + " Alive");
			}
		}

		Scoreboard.draw();
	}

	@Override
	public List<Player> getWinners()
	{
		if (WinnerTeam == null)
			return null;

		return WinnerTeam.GetPlayers(false);
	}

	@Override
	public List<Player> getLosers()
	{
		if (WinnerTeam == null)
			return null;

		List<Player> players = new ArrayList<>();

		for (GameTeam team : GetTeamList())
		{
			if (team != WinnerTeam)
				players.addAll(team.GetPlayers(false));
		}

		return players;
	}
}
