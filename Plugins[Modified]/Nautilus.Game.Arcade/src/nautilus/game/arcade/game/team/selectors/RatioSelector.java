package nautilus.game.arcade.game.team.selectors;

import java.util.List;
import java.util.stream.Collectors;

import mineplex.core.common.util.UtilAlg;

import nautilus.game.arcade.game.GameTeam;

public class RatioSelector implements TeamSelector
{

	private final GameTeam _team;
	private final double _ratio;

	public RatioSelector(GameTeam team, double ratio)
	{
		_team = team;
		_ratio = ratio;
	}

	@Override
	public GameTeam getTeamToJoin(List<GameTeam> teams, int desiredAmount, int target, int total)
	{
		if (canJoinTeam(_team, desiredAmount, target, total))
		{
			return _team;
		}

		return UtilAlg.Random(teams.stream()
				.filter(team -> !team.equals(_team))
				.collect(Collectors.toList()));
	}

	@Override
	public boolean canJoinTeam(GameTeam team, int desiredAmount, int target, int total)
	{
		return !team.equals(_team) || _team.GetSize() + desiredAmount <= Math.max(_ratio * total, 1);
	}
}
