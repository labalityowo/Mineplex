package nautilus.game.arcade.game.games.skyfall;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.team.NamedTeamsModule;
import nautilus.game.arcade.game.team.TeamRequestsModule;
import nautilus.game.arcade.game.team.selectors.FillToSelector;

/**
 * TeamSkyfall
 *
 * @author xXVevzZXx
 */
public class TeamSkyfall extends Skyfall
{
	private static final long BOOSTER_COOLDOWN_TIME = 1000*20; // 20 Seconds

	public TeamSkyfall(ArcadeManager manager) 
	{
		super(manager, GameType.SkyfallTeams);

		SpawnNearAllies = true;
		
		DamageTeamSelf = false;
		
		ShowTeammateMessage = true;
		
		HideTeamSheep = true;

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
		if (event.getType() != UpdateType.FAST)
			return;

		if (GetTeamList().isEmpty())
			return;

		Scoreboard.reset();

		if (IsLive())
		{
			Scoreboard.writeNewLine();
			Scoreboard.write(C.cGreen + C.Bold + "Time");
			Scoreboard.write(UtilTime.convertString(System.currentTimeMillis() - GetStateTime(), 0, TimeUnit.FIT));
			Scoreboard.writeNewLine();
		}

		Scoreboard.write(C.cYellow + C.Bold + "Teams");
		
		ArrayList<GameTeam> alive = new ArrayList<GameTeam>();
		for (GameTeam team : GetTeamList())
		{
			if (team.IsTeamAlive())
				alive.add(team);
		}

		if (GetPlayers(true).size() <= 7)
		{
			for (GameTeam team : GetTeamList())
			{
				for (Player player : team.GetPlayers(true))
				{
					Scoreboard.write(team.GetColor() + player.getName());
				}
			}
		}
		else if (alive.size() <= 7)
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
		
		if (IsLive() && !isDeathMatch())
		{
			Scoreboard.writeNewLine();
			Scoreboard.write(C.cGold + C.Bold + "Chest Refill");
			Scoreboard.write(C.cWhite + UtilTime.MakeStr((getChestsRefilled() + getChestRefillTime()) - System.currentTimeMillis()));
		}
		else if (isDeathMatch() && !isDeathMatchStarted())
		{
			Scoreboard.writeNewLine();
			Scoreboard.write(C.cRed + C.Bold + "Deathmatch");
			Scoreboard.write(F.time(UtilTime.MakeStr(isTeleportedDeathmatch() ? (getDeathmatchStartTime() + getDeathmatchStartingTime() + getDeathmatchWaitTime()) - System.currentTimeMillis() : (getDeathmatchStartTime() + getDeathmatchStartingTime()) - System.currentTimeMillis())));
		}
		else if (isDeathMatchStarted())
		{
			Scoreboard.writeNewLine();
			Scoreboard.write(C.cRed + C.Bold + "Game End");
			Scoreboard.write(UtilTime.convertString(Math.max(0, (GetStateTime() + GameTimeout) - System.currentTimeMillis()), 0, TimeUnit.FIT));
		}

		Scoreboard.draw();
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		ArrayList<GameTeam> teamsAlive = new ArrayList<GameTeam>();

		for (GameTeam team : GetTeamList())
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
	
	@EventHandler
	@Override
	public void ringBoost(PlayerBoostRingEvent event)
	{	
		if (IsAlive(event.getPlayer()))
			event.getRing().disableLater(3000, BOOSTER_COOLDOWN_TIME, Material.STAINED_CLAY, (byte) 14, true);
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
