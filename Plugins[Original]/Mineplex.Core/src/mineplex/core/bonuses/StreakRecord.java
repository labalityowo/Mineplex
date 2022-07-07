package mineplex.core.bonuses;

public class StreakRecord
{
	private String _playerName;
	private int _streak;

	public StreakRecord(String playerName, int streak)
	{
		_playerName = playerName;
		_streak = streak;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	public int getStreak()
	{
		return _streak;
	}
}
