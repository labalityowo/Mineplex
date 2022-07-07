package mineplex.game.clans.tutorial.objective;

public class UnorderedObjectiveData extends ObjectiveData
{
	private boolean[] _tasks;

	public UnorderedObjectiveData(int taskSize)
	{
		_tasks = new boolean[taskSize];
	}

	public boolean isComplete()
	{
		boolean complete = true;
		for (int i = 0; i < _tasks.length; i++)
		{
			complete = complete && _tasks[i];
		}
		return complete;
	}

	public void setComplete(int taskId)
	{
		_tasks[taskId] = true;
	}

	public boolean isComplete(int taskId)
	{
		return _tasks[taskId];
	}

	public int getFirstIncompleteIndex()
	{
		for (int i = 0; i < _tasks.length; i++)
		{
			if (_tasks[i] == false)
				return i;
		}

		return -1;
	}

	public boolean[] getTasks()
	{
		return _tasks;
	}

}
