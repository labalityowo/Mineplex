package nautilus.game.arcade.game.games.moba.progression;

public class MobaLevelData
{

	private final int _exp;
	private final int _level;
	private final int _thisLevel;
	private final int _nextLevel;

	public MobaLevelData(long exp)
	{
		_exp = (int) exp;
		_level = MobaProgression.getLevel(exp);
		_thisLevel = MobaProgression.getExpFor(_level);
		_nextLevel = MobaProgression.getExpFor(_level + 1);
	}

	public int getExp()
	{
		return _exp;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getDisplayLevel()
	{
		return _level + 1;
	}

	public int getExpThisLevel()
	{
		return _thisLevel;
	}

	public int getExpJustThisLevel()
	{
		return _nextLevel - _thisLevel;
	}

	public int getExpLevelProgress()
	{
		return _exp - _thisLevel;
	}

	public int getExpReminder()
	{
		return _nextLevel - _exp;
	}

	public double getPercentageComplete()
	{
		return (double) (getExpLevelProgress()) / (double) (getExpJustThisLevel());
	}
}
