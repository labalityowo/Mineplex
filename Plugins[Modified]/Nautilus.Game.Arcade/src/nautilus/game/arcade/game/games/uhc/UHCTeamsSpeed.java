package nautilus.game.arcade.game.games.uhc;

import java.util.concurrent.TimeUnit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;

public class UHCTeamsSpeed extends UHCTeams
{

	public UHCTeamsSpeed(ArcadeManager manager)
	{
		super(manager, GameType.UHCTeamsSpeed, true);
		
		MINING_TIME = (int) TimeUnit.MINUTES.toMillis(20);
	}
	
	@Override
	public String GetMode()
	{
		return "UHC Teams Speed";
	}
	
}
