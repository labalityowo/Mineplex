package mineplex.core.delayedtask;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class DelayedTaskClient
{
	public Map<String, Task> Tasks = new HashMap<>();
	
	private Player _player;
	
	public DelayedTaskClient(Player player)
	{
		_player = player;
	}
	
	public void insert(Task task)
	{
		Tasks.put(task.getName(), task);
	}
	
	public long getTimeLeft(String task)
	{
		if (!Tasks.containsKey(task)) return -1;
		
		return getEndTime(task) - System.currentTimeMillis();
	}
	
	public long getEndTime(String task)
	{
		if (!Tasks.containsKey(task)) return -1;
		
		return Tasks.get(task).getEndTime();
	}
	
	public long getStartTime(String task)
	{
		if (!Tasks.containsKey(task)) return -1;
		
		return Tasks.get(task).getStartTime();
	}

	public void cleanup()
	{
		if (Tasks == null)
		{
			Tasks = new HashMap<>();
		}
		
		Tasks.clear();
		Tasks = null;
	}
	
	public void cleanup(String task)
	{
		Tasks.remove(task);
	}

	public void tick()
	{
		if (Tasks == null)
		{
			Tasks = new HashMap<>();
		}
		
		for (Task task : Tasks.values())
		{
			if (task.getTick() != null)
			{
				task.tick();
			}
		}
	}

	public Player getPlayer()
	{
		return _player;
	}

}
