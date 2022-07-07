package mineplex.core.task.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.task.Task;
import mineplex.core.task.TaskClient;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.ResultSetCallable;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class TaskRepository extends RepositoryBase
{
	private static String ADD_ACCOUNT_TASK = "INSERT INTO accountTasks (accountId, taskId) VALUES (?, ?);";
	
	private static String ADD_TASK = "INSERT INTO tasks (name) VALUES (?);";
	private static String RETRIEVE_TASKS = "SELECT id, name FROM tasks;";
	
	public TaskRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
	}
	
	public boolean addAccountTask(int accountId, int taskId)
	{
		return executeUpdate(ADD_ACCOUNT_TASK, new ColumnInt("accountId", accountId), new ColumnInt("taskId", taskId)) > 0;
	}
	
	public TaskClient loadClientInformation(ResultSet resultSet) throws SQLException
	{
		final TaskClient taskClient = new TaskClient();

		while (resultSet.next())
		{
			taskClient.TasksCompleted.add(resultSet.getInt(1));
		}
		
		return taskClient;
	}

	public List<Task> retrieveTasks()
	{
		final List<Task> tasks = new ArrayList<Task>();
		
		executeQuery(RETRIEVE_TASKS, new ResultSetCallable()
		{
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					tasks.add(new Task(resultSet.getInt(1), resultSet.getString(2)));
				}
			}
		});
		
		return tasks;
	}

	public void addTask(String task)
	{
		executeUpdate(ADD_TASK, new ColumnVarChar("name", 100, task));
	}
}