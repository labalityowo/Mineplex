package nautilus.game.arcade.game.team.selectors;

import java.util.List;

import nautilus.game.arcade.game.GameTeam;

public interface TeamSelector
{

	GameTeam getTeamToJoin(List<GameTeam> teams, int desiredAmount, int target, int total);

	boolean canJoinTeam(GameTeam team, int desiredAmount, int target, int total);

}
