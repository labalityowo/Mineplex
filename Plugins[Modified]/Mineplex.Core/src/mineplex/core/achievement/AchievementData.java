package mineplex.core.achievement;

public class AchievementData
{
	private int _level;
	private long _expRemainder;
	private long _expNextLevel;
	
	public AchievementData(int level, long expRemainder, long expNextLevel)
	{
		_level = level;
		_expRemainder = expRemainder;
		_expNextLevel = expNextLevel;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public long getExpRemainder()
	{
		return _expRemainder;
	}
	
	public long getExpNextLevel()
	{
		return _expNextLevel;
	}
}
