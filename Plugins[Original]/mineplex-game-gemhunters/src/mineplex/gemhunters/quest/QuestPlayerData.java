package mineplex.gemhunters.quest;

import java.util.ArrayList;
import java.util.List;

public class QuestPlayerData
{

	private final List<Integer> _possibleQuests;
	private final List<Integer> _activeQuests;
	private final List<Integer> _completedQuests;

	private long _lastClear;

	public QuestPlayerData()
	{
		_possibleQuests = new ArrayList<>();
		_activeQuests = new ArrayList<>();
		_completedQuests = new ArrayList<>();

		_lastClear = System.currentTimeMillis();
	}

	public void clear()
	{
		clear(false);
	}

	public void clear(boolean active)
	{
		_possibleQuests.clear();
		_completedQuests.clear();

		if (active)
		{
			_activeQuests.clear();
			_lastClear = 0;
		}
		else
		{
			_lastClear = System.currentTimeMillis();
		}
	}

	public List<Integer> getPossibleQuests()
	{
		return _possibleQuests;
	}

	public List<Integer> getActiveQuests()
	{
		return _activeQuests;
	}

	public List<Integer> getCompletedQuests()
	{
		return _completedQuests;
	}

	public long getLastClear()
	{
		return _lastClear;
	}

}
