package nautilus.game.arcade.game.games.cakewars.modes;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.team.TeamRequestsModule;
import nautilus.game.arcade.game.team.selectors.FillToSelector;

public class CakeWarsDuos extends CakeWars
{

	public CakeWarsDuos(ArcadeManager manager)
	{
		super(manager, GameType.CakeWarsDuos);

		HideTeamSheep = true;
		ShowTeammateMessage = true;
		_teamSelector = new FillToSelector(this, 2);

		new TeamRequestsModule()
				.register(this);
	}

	@Override
	public String GetMode()
	{
		return "Duos";
	}
}
