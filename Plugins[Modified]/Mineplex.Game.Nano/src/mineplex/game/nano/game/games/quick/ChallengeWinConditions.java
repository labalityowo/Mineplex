package mineplex.game.nano.game.games.quick;

public class ChallengeWinConditions
{

	private boolean _lastOne, _lastThree, _timeoutWin;
	private boolean _timeoutAfterFirst;
	private int _timeout = 3;

	public ChallengeWinConditions setLastOne(boolean lastOne)
	{
		setTimeoutWin(true);
		_lastOne = lastOne;
		return this;
	}

	public boolean isLastOne()
	{
		return _lastOne;
	}

	public ChallengeWinConditions setLastThree(boolean lastThree)
	{
		setTimeoutWin(true);
		_lastThree = lastThree;
		return this;
	}

	public boolean isLastThree()
	{
		return _lastThree;
	}

	public ChallengeWinConditions setTimeoutWin(boolean timeoutWin)
	{
		_timeoutWin = timeoutWin;
		return this;
	}

	public boolean isTimeoutWin()
	{
		return _timeoutWin;
	}

	public ChallengeWinConditions setTimeoutAfterFirst(boolean firstHalfWin)
	{
		_timeoutAfterFirst = firstHalfWin;
		return this;
	}

	public ChallengeWinConditions setTimeoutAfterFirst(boolean firstHalfWin, int timeout)
	{
		_timeoutAfterFirst = firstHalfWin;
		_timeout = timeout;
		return this;
	}

	public boolean isTimeoutAfterFirst()
	{
		return _timeoutAfterFirst;
	}

	public int getTimeout()
	{
		return _timeout;
	}
}
