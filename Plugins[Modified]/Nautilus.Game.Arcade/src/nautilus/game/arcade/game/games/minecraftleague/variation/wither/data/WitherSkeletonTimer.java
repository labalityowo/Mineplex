package nautilus.game.arcade.game.games.minecraftleague.variation.wither.data;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import nautilus.game.arcade.game.games.minecraftleague.variation.ExtraScoreboardData;
import nautilus.game.arcade.scoreboard.GameScoreboard;

public class WitherSkeletonTimer extends ExtraScoreboardData
{
	private long _end;
	private int _frozen;
	
	public WitherSkeletonTimer(GameScoreboard sb)
	{
		super(sb);
		_end = 0;
		_frozen = -1;
	}
	
	public void setEnd(long end)
	{
		_end = end;
	}
	
	public void freezeTime(int seconds)
	{
		_frozen = seconds;
	}
	
	public void write()
	{
		Scoreboard.write(C.cYellowB + "Wither Skeleton Spawn");
		if (_frozen != -1)
		{
			if (_frozen == -2)
				Scoreboard.write("WITHER ALIVE");
			else
				Scoreboard.write(_frozen + " Seconds");
		}
		else
		{
			long seconds = 0;
			if (_end - System.currentTimeMillis() > 0)
				seconds = UtilTime.convert(_end - System.currentTimeMillis(), TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
			
			Scoreboard.write(UtilTime.MakeStr(UtilTime.convert(seconds, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)));
		}
	}
}
