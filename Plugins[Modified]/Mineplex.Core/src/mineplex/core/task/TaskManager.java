package mineplex.core.task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import mineplex.cache.player.PlayerCache;
import mineplex.core.MiniDbClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilTasks;
import mineplex.core.task.repository.TaskRepository;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TaskManager extends MiniDbClientPlugin<TaskClient>
{
	public static TaskManager Instance;
	
	private static Object _taskLock = new Object();
	private TaskRepository _repository;
	
	private NautHashMap<String, Integer> _tasks = new NautHashMap<String, Integer>();
	
	public TaskManager(JavaPlugin plugin, CoreClientManager clientManager)
	{
		super("Task Manager", plugin, clientManager);
		
		Instance = this;
		
		_repository = new TaskRepository(plugin);
		updateTasks();
	}

	private void updateTasks()
	{
		List<Task> tasks = _repository.retrieveTasks();
		
		synchronized (_taskLock)
		{
			for (Task task : tasks)
			{
				_tasks.put(task.Name, task.Id);
			}
		}
	}
	
	@Override
	protected TaskClient addPlayer(UUID uuid)
	{
		return new TaskClient();
	}
	
	public void addTaskForOfflinePlayer(Consumer<Boolean> callback, final UUID uuid, final String task)
	{
		Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable()
		{
			public void run()
			{
				boolean taskExists = false;
				
				synchronized (_taskLock)
				{
					taskExists = _tasks.containsKey(task);
				}

				if (!taskExists)
				{
					_repository.addTask(task);
					System.out.println("TaskManager Adding Task : " + task);
					
					updateTasks();
				}

				int accountId = PlayerCache.getInstance().getAccountId(uuid);

				if (accountId != -1)
				{
					UtilTasks.onMainThread(callback).accept(_repository.addAccountTask(accountId, getTaskId(task)));
				}
				else
				{
					ClientManager.loadAccountIdFromUUID(uuid, id ->
					{
						if (id > 0)
							UtilTasks.onMainThread(callback).accept(_repository.addAccountTask(accountId, getTaskId(task)));
						else
							UtilTasks.onMainThread(callback).accept(false);
					});
				}
			}
		});
	}
	
	public boolean hasCompletedTask(Player player, String taskName)
	{
		synchronized (_taskLock)
		{
			if (!_tasks.containsKey(taskName))
			{
				return false;
			}
			
			return Get(player).TasksCompleted.contains(_tasks.get(taskName));
		}
	}
	
	public void completedTask(final Callback<Boolean> callback, final Player player, final String taskName)
	{
		synchronized (_taskLock)
		{
			if (_tasks.containsKey(taskName))
			{
				Get(player).TasksCompleted.add(_tasks.get(taskName));
			}
		}
		
		addTaskForOfflinePlayer(success ->
		{
			if (!success)
			{
				System.out.println("Add task FAILED for " + player.getName());

				synchronized (_taskLock)
				{
					if (_tasks.containsKey(taskName))
					{
						Get(player).TasksCompleted.remove(_tasks.get(taskName));
					}
				}
			}

			if (callback != null)
			{
				callback.run(success);
			}
		}, player.getUniqueId(), taskName);
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		Set(uuid, _repository.loadClientInformation(resultSet));
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT taskId FROM accountTasks WHERE accountId = '" + accountId + "';";
	}

	public Integer getTaskId(String taskName)
	{
		synchronized (_taskLock)
		{
			return _tasks.get(taskName);
		}
	}
}
