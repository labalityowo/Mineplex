package nautilus.game.arcade.game.games.uhc;

import java.util.concurrent.TimeUnit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;

public class UHCSoloSpeed extends UHCSolo
{

	public UHCSoloSpeed(ArcadeManager manager)
	{
		super(manager, GameType.UHCSoloSpeed, true);
		
		MINING_TIME = (int) TimeUnit.MINUTES.toMillis(20);
	}
	
	@Override
	public String GetMode()
	{
		return "UHC Solo Speed";
	}
	
}
