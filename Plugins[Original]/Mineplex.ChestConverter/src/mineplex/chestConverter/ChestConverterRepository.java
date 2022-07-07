package mineplex.chestConverter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class ChestConverterRepository
{
	private String _connectionString = "jdbc:mysql://db.mineplex.com:3306/Account?allowMultiQueries=true";
	private String _userName = "root";
	private String _password = "tAbechAk3wR7tuTh";
	
	private static String ADD_ACCOUNT_TASK = "INSERT INTO accountTasks (accountId, taskId) VALUES (?, ?);";
	private static String RETRIEVE_TASKS = "SELECT id, name FROM tasks;";
	
	private static Connection _connection;
	
	public ChestConverterRepository()
	{
		PreparedStatement preparedStatement = null;
		
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			
			if (_connection == null || _connection.isClosed())
				_connection = DriverManager.getConnection(_connectionString, _userName, _password);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		finally
		{
			if (preparedStatement != null)
			{
				try
				{
					preparedStatement.close();
				} 
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public HashMap<String, Integer> getTaskList()
	{
		HashMap<String, Integer> tasks = new HashMap<String, Integer>();
		PreparedStatement preparedStatement = null;
		
		try
		{
			if (_connection == null || _connection.isClosed())
				_connection = DriverManager.getConnection(_connectionString, _userName, _password);

			preparedStatement = _connection.prepareStatement(RETRIEVE_TASKS);
			preparedStatement.execute();
			
			ResultSet resultSet = preparedStatement.getResultSet();
			
			while (resultSet.next())
			{
				tasks.put(resultSet.getString(2), resultSet.getInt(1));
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		finally
		{
			if (preparedStatement != null)
			{
				try
				{
					preparedStatement.close();
				} 
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return tasks;
	}
	
	public List<AccountTask> getTasks(int lastId, int count) throws Exception
	{
		return new JsonWebCall("http://accounts.mineplex.com/PlayerAccount/GetTasksByCount").Execute(new com.google.gson.reflect.TypeToken<List<AccountTask>>(){}.getType(), new SearchConf(lastId, count));
	}
	
	public void incrementClients(HashMap<String, List<Integer>> playerList)
	{
		PreparedStatement preparedStatement = null;
		Statement statement = null;
		
		try
		{
			if (_connection == null || _connection.isClosed())
				_connection = DriverManager.getConnection(_connectionString, _userName, _password);
			
			statement = _connection.createStatement();
			HashMap<Integer, List<Integer>> playerIdList = new HashMap<Integer, List<Integer>>();
			String queryString = "";
			for (Entry<String, List<Integer>> entry : playerList.entrySet())
			{
				queryString += "SELECT id FROM accounts WHERE accounts.uuid = '" + entry.getKey() + "' LIMIT 1;";
			}

			statement.execute(queryString);
			statement.getUpdateCount();
			
			for (Entry<String, List<Integer>> entry : playerList.entrySet())
			{
				ResultSet resultSet = statement.getResultSet();
				
				while (resultSet.next())
				{
					for (Integer taskId : entry.getValue())
					{
						if (!playerIdList.containsKey(resultSet.getInt(1)))
							playerIdList.put(resultSet.getInt(1), new ArrayList<Integer>());
						
						playerIdList.get(resultSet.getInt(1)).add(taskId);
					}
				}
				statement.getMoreResults();
			}
			
			preparedStatement = _connection.prepareStatement(ADD_ACCOUNT_TASK);
			System.out.println("adding to mysql db.");
			for (Entry<Integer, List<Integer>> entry : playerIdList.entrySet())
			{
				for (Integer taskId : entry.getValue())
				{
					preparedStatement.setInt(1, entry.getKey());
					preparedStatement.setInt(2, taskId);
					preparedStatement.addBatch();
				}
			}

			preparedStatement.executeBatch();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		finally
		{
			if (preparedStatement != null)
			{
				try
				{
					preparedStatement.close();
				} 
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
