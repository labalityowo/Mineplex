package nautilus.game.arcade.game.games.mineware.challenge;

import org.bukkit.Location;

/**
 * This class contains a list of fields that are used as settings for the challenge class.
 */
public class ChallengeSettings
{
	private Challenge _challenge;
	private int _minPlayers = 1;
	private int _maxPlayers = 250;
	private int _maxCompletedCount;
	private long _startTime;
	private long _duration = 60000;

	private boolean _useMapHeight;
	private boolean _lockInventory;
	private int _lockedSlot = 0;
	private boolean _teamBased;
	private boolean _canCrumble;
	private boolean _hideTimerRanOutMessage;

	private double _centerX;
	private double _centerY;
	private double _centerZ;

	public ChallengeSettings(Challenge challenge)
	{
		_challenge = challenge;
	}

	public Challenge getChallenge()
	{
		return _challenge;
	}

	public void setMinPlayers(int minPlayers)
	{
		_minPlayers = minPlayers;
	}

	public int getMinPlayers()
	{
		return _minPlayers;
	}

	public void setMaxPlayers(int maxPlayers)
	{
		_maxPlayers = maxPlayers;
	}

	public int getMaxPlayers()
	{
		return _maxPlayers;
	}

	public Location getMapCenter()
	{
		return new Location(_challenge.getHost().WorldData.World, _centerX, _centerY, _centerZ);
	}

	public void setMaxCompletedCount(int maxCompletedCount)
	{
		_maxCompletedCount = maxCompletedCount;
	}

	public int getMaxCompletedCount()
	{
		return _maxCompletedCount;
	}

	public void setStartTime(long startTime)
	{
		_startTime = startTime;
	}

	public long getStartTime()
	{
		return _startTime;
	}

	public void setDuration(long duration)
	{
		_duration = duration;
	}

	public long getDuration()
	{
		return _duration;
	}

	public void setUseMapHeight()
	{
		_useMapHeight = true;
		_centerX = -1;
		_centerY = 62;
		_centerZ = 10;
	}

	public boolean canUseMapHeight()
	{
		return _useMapHeight;
	}

	public void setLockInventory(int lockedSlot)
	{
		_lockInventory = true;
		_lockedSlot = lockedSlot;
	}

	public boolean isInventoryLocked()
	{
		return _lockInventory;
	}

	public int getLockedSlot()
	{
		return _lockedSlot;
	}

	public void setTeamBased()
	{
		_teamBased = true;
	}

	public boolean isTeamBased()
	{
		return _teamBased;
	}

	public void setCanCruble()
	{
		_canCrumble = true;
	}

	public boolean canCrumble()
	{
		return _canCrumble;
	}

	public void hideTimerRanOutMessage()
	{
		_hideTimerRanOutMessage = true;
	}

	public boolean shouldHideTimerRanOutMessage()
	{
		return _hideTimerRanOutMessage;
	}
}
