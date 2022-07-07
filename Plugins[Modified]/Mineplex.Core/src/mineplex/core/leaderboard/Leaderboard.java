package mineplex.core.leaderboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import mineplex.core.common.util.C;
import mineplex.core.leaderboard.LeaderboardRepository.LeaderboardSQLType;

public class Leaderboard
{
	private static final String NULL_ENTRY = C.cGray + " - ";

	private final LeaderboardSQLType _type;
	private final int _start, _size;
	private final int[] _statIds;
	private final String[] _statNames;
	private final Map<String, Long> _entries;
	private final List<String> _formattedEntries;

	public Leaderboard(LeaderboardSQLType type, String... statNames)
	{
		this(type, 10, statNames);
	}

	public Leaderboard(LeaderboardSQLType type, int size, String... statNames)
	{
		this(type, 0, size, statNames);
	}

	public Leaderboard(LeaderboardSQLType type, int start, int size, String... statNames)
	{
		_type = type;
		_start = start;
		_size = size;
		_statIds = new int[statNames.length];
		_statNames = statNames;
		_entries = new HashMap<>(size);
		_formattedEntries = new ArrayList<>(size);
	}

	public void update(Map<String, Long> entries)
	{
		_entries.clear();
		_entries.putAll(entries);
		_formattedEntries.clear();

		AtomicInteger place = new AtomicInteger(_start);

		entries.forEach((name, value) -> _formattedEntries.add(C.cAqua + "#" + place.incrementAndGet() + C.cGray + " - " + C.cYellow + name + C.cGray + " - " + C.cYellow + value));

		while (place.getAndIncrement() < _size)
		{
			_formattedEntries.add(NULL_ENTRY);
		}
	}

	public LeaderboardSQLType getType()
	{
		return _type;
	}

	public int getStart()
	{
		return _start;
	}

	public int getSize()
	{
		return _size;
	}

	synchronized void setStatId(int index, int id)
	{
		if (_statIds.length > index && index >= 0)
		{
			_statIds[index] = id;
		}
	}

	synchronized int[] getStatIds()
	{
		return _statIds;
	}

	String[] getStatNames()
	{
		return _statNames;
	}

	public Map<String, Long> getEntries()
	{
		return _entries;
	}

	public List<String> getFormattedEntries()
	{
		return _formattedEntries;
	}
}