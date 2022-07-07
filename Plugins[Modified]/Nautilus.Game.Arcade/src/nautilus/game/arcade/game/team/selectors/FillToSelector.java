package nautilus.game.arcade.game.team.selectors;

import java.util.Comparator;
import java.util.List;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;

public class FillToSelector extends EvenTeamSelector
{

	public FillToSelector(Game game, int amount)
	{
		game.getTeamModule().setPlayersPerTeam(amount);
	}

	@Override
	public GameTeam getTeamToJoin(List<GameTeam> teams, int desiredAmount, int target, int total)
	{
		// Get the biggest team that can fit our desired amount
		GameTeam primaryChoice = teams.stream()
				.filter(team -> canJoinTeam(team, desiredAmount, target, total))
				.max(Comparator.comparingInt(GameTeam::GetSize))
				.orElse(null);

		if (primaryChoice != null)
		{
			return primaryChoice;
		}

		// This would happen if all teams are full and we are overflowing
		// Just get the emptiest team.
		return super.getTeamToJoin(teams, desiredAmount, target, total);
	}
}
