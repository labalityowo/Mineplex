package nautilus.game.arcade.game.games.survivalgames;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.team.NamedTeamsModule;
import nautilus.game.arcade.game.team.TeamRequestsModule;
import nautilus.game.arcade.game.team.selectors.FillToSelector;

public class SurvivalGamesNewTeams extends SurvivalGamesNew
{

	private static final String[] DESCRIPTION =
			{
					"Search for chests to find loot",
					"Slaughter your opponents",
					"Stay away from the borders!",
					"Last tribute alive wins!"
			};

	public SurvivalGamesNewTeams(ArcadeManager manager)
	{
		super(manager, GameType.SurvivalGamesTeams, DESCRIPTION);

		ShowTeammateMessage = true;

		SpawnNearAllies = true;
		SpawnNearEnemies = true;

		DamageTeamSelf = false;

		_teamSelector = new FillToSelector(this, 2);

		new NamedTeamsModule()
				.register(this);

		new TeamRequestsModule()
				.register(this);
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		List<GameTeam> teamsAlive = GetTeamList().stream()
				.filter(GameTeam::IsTeamAlive)
				.collect(Collectors.toList());

		if (teamsAlive.size() < 2)
		{
			for (GameTeam team : GetTeamList())
			{
				if (WinnerTeam != null && team.equals(WinnerTeam))
				{
					for (Player player : team.GetPlayers(false))
					{
						AddGems(player, 10, "Winning Team", false, false);
					}
				}

				for (Player player : team.GetPlayers(false))
				{
					if (player.isOnline())
					{
						AddGems(player, 10, "Participation", false, false);
					}
				}
			}

			if (!teamsAlive.isEmpty())
			{
				AnnounceEnd(teamsAlive.get(0));
			}

			SetState(GameState.End);
		}
	}

	@Override
	public List<Player> getWinners()
	{
		return WinnerTeam == null ? null : WinnerTeam.GetPlayers(false);
	}

	@Override
	public List<Player> getLosers()
	{
		List<Player> winners = getWinners();

		if (winners == null)
		{
			return null;
		}

		return GetPlayers(false).stream()
				.filter(player -> !winners.contains(player))
				.collect(Collectors.toList());
	}

	@Override
	public String GetMode()
	{
		return "Team Mode";
	}
}
