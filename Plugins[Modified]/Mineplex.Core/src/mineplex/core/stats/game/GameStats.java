package mineplex.core.stats.game;

import mineplex.core.game.GameDisplay;
import mineplex.serverdata.Region;

import java.util.HashMap;
import java.util.Map;

public class GameStats
{

	private final int _gameId;
	private final Region _region;
	private final GameDisplay _gameType;

	private int _mapId;
	private long _startTime;
	private long _endTime;

	private final Map<Integer, Map<String, Long>> _stats;

	public GameStats(int gameId, Region region, GameDisplay display)
	{
		_gameId = gameId;
		_region = region;
		_gameType = display;
		_stats = new HashMap<>();
	}

	public int getGameId()
	{
		return _gameId;
	}

	public Region getRegion()
	{
		return _region;
	}

	public GameDisplay getGameType()
	{
		return _gameType;
	}

	public void setMapId(int mapId)
	{
		_mapId = mapId;
	}

	public int getMapId()
	{
		return _mapId;
	}

	public void setStartTime(long startTime)
	{
		_startTime = startTime;
	}

	public long getStartTime()
	{
		return _startTime;
	}

	public void setEndTime(long endTime)
	{
		_endTime = endTime;
	}

	public long getEndTime()
	{
		return _endTime;
	}

	public Map<Integer, Map<String, Long>> getStats()
	{
		return _stats;
	}

	public boolean isValid()
	{
		return _region != null && _gameType != null && _mapId != 0 && _startTime != 0 && _endTime != 0;
	}
}
