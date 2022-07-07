package nautilus.game.arcade.game.games.skywars;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.skywars.trackers.TNTStatTracker;
import nautilus.game.arcade.game.team.NamedTeamsModule;
import nautilus.game.arcade.game.team.TeamRequestsModule;
import nautilus.game.arcade.game.team.selectors.FillToSelector;
import nautilus.game.arcade.stats.DeathBomberStatTracker;
import nautilus.game.arcade.stats.WinWithoutOpeningChestStatTracker;
import nautilus.game.arcade.stats.WinWithoutWearingArmorStatTracker;

public class TeamSkywars extends Skywars
{
	
	@SuppressWarnings("unchecked")
	public TeamSkywars(ArcadeManager manager)
	{
		this(manager, GameType.SkywarsTeams);

		registerStatTrackers
				(
						new TNTStatTracker(this),
						new DeathBomberStatTracker(this, 3), //TNT Kills
						new WinWithoutOpeningChestStatTracker(this),
						new WinWithoutWearingArmorStatTracker(this)
				);

		registerChatStats
				(
						Kills,
						Deaths,
						KDRatio,
						BlankLine,
						Assists,
						DamageTaken,
						DamageDealt
				);
	}
	
	public TeamSkywars(ArcadeManager manager, GameType type)
	{
		super(manager, type, 
				 new String[]
							{
					"Free for all battle in the sky!", 
					"Craft or loot gear for combat",
					"Last team alive wins!"
							});

		SpawnNearAllies = true;
		
		DamageTeamSelf = false;

		ShowTeammateMessage = true;

		_teamSelector = new FillToSelector(this, 2);

		new NamedTeamsModule()
				.setTeamPerSpawn(true)
				.register(this);

		new TeamRequestsModule()
				.register(this);
	}
	
	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !InProgress())
		{
			return;
		}

		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellowB + "Teams");
		
		List<GameTeam> alive = new ArrayList<>();
		for (GameTeam team : GetTeamList())
		{
			if (team.IsTeamAlive())
			{
				alive.add(team);
			}
		}
		
		if (GetPlayers(true).size() <= 10)
		{
			for (GameTeam team : GetTeamList())
			{
				for (Player player : team.GetPlayers(true))
				{
					Scoreboard.write(team.GetColor() + player.getName());
				}
			}
		}
		else if (alive.size() <= 4)
		{
			for (GameTeam team : alive)
			{
				Scoreboard.write(C.cWhite + team.GetPlayers(true).size() + " " + team.GetColor() + team.GetName());
			}
		}
		else
		{
			Scoreboard.write(C.cWhite + alive.size() + " Alive");
		}

		writeScoreboard();

		Scoreboard.draw();
	}

	@Override
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

	@Override
	public String GetMode()
	{
		return "Team Mode";
	}
}
