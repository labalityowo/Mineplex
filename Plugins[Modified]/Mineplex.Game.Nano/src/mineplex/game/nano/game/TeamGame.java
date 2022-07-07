package mineplex.game.nano.game;

import java.util.ArrayList;

import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.components.end.GameEndComponent.AnnouncementType;
import mineplex.game.nano.game.components.team.GameTeam;

public abstract class TeamGame extends Game
{

	public TeamGame(NanoManager manager, GameType gameType, String[] description)
	{
		super(manager, gameType, description);

		_endComponent.setAnnouncementType(AnnouncementType.TEAM);
	}

	@Override
	public boolean endGame()
	{
		GameTeam aliveTeam = null;
		int teamsAlive = 0;

		for (GameTeam team : getTeams())
		{
			if (team.getAlivePlayers().isEmpty())
			{
				continue;
			}

			aliveTeam = team;
			teamsAlive++;
		}

		if (teamsAlive == 0)
		{
			return true;
		}
		else if (teamsAlive == 1)
		{
			setWinningTeam(aliveTeam);
			return true;
		}

		return false;
	}

	@Override
	protected GamePlacements createPlacements()
	{
		GameTeam winningTeam = getWinningTeam();

		if (winningTeam == null)
		{
			return null;
		}


		return GamePlacements.fromTeamPlacements(new ArrayList<>(winningTeam.getAllPlayers()));
	}
}
