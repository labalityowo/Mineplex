package nautilus.game.arcade.game.team.selectors;

import java.util.Comparator;
import java.util.List;

import nautilus.game.arcade.game.GameTeam;

public class EvenTeamSelector implements TeamSelector
{

	@Override
	public GameTeam getTeamToJoin(List<GameTeam> teams, int desiredAmount, int target, int total)
	{
		return teams.stream()
				.min(Comparator.comparingInt(GameTeam::GetSize))
				.orElse(null);
	}

	@Override
	public boolean canJoinTeam(GameTeam team, int desiredAmount, int target, int total)
	{
		return team.GetSize() + desiredAmount <= target;
	}
}
