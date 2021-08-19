package nautilus.game.arcade.game.games.castleassault.data;

public class KillStreakData
{
	private static final int[] REWARDED_STREAKS = {2, 4, 6, 8};
	private int _kills;
	private int _bestStreak;
	
	public KillStreakData()
	{
		_kills = 0;
		_bestStreak = 0;
	}
	
	public int getKills()
	{
		return _kills;
	}
	
	public int getBestStreak()
	{
		return Math.max(_bestStreak, _kills);
	}
	
	public boolean addKill(boolean hardLine)
	{
		_kills++;
		for (int streak : REWARDED_STREAKS)
		{
			if ((_kills + (hardLine ? 1 : 0)) == streak)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void reset()
	{
		if (_kills > _bestStreak)
		{
			_bestStreak = _kills;
		}
		_kills = 0;
	}
}