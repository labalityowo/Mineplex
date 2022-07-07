package mineplex.ddos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class DnsMadeEasyRepository
{
	// Yip Yip actual IP because if null route happens we can't resolve the HOSTNAME DERP FACE DEFEK7!!!   -defek7
	private String _connectionString = "jdbc:mysql://10.35.74.133:3306/BungeeServers?autoReconnect=true&failOverReadOnly=false&maxReconnects=10";
	private String _userName = "root";
	private String _password = "tAbechAk3wR7tuTh";
	
	private static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS bungeeOnlineStatus (id INT NOT NULL AUTO_INCREMENT, address VARCHAR(40), online BOOLEAN NOT NULL DEFAULT 0, updated LONG, us BOOLEAN NOT NULL DEFAULT 1, lastOnline LONG, PRIMARY KEY (id), UNIQUE INDEX addressIndex(address));";
	private static String SELECT_SERVERS = "SELECT updated, lastOnline, us, now() FROM bungeeOnlineStatus;";
	
	public void initialize()
	{
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try
		{
			connection = DriverManager.getConnection(_connectionString, _userName, _password);
			
			// Create table
			preparedStatement = connection.prepareStatement(CREATE_TABLE);
			preparedStatement.execute();
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
			
			if (connection != null)
			{
				try
				{
					connection.close();
				} 
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public boolean switchToDDOSProt()
	{
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int countOffline = 0;
		
		try
		{
			connection = DriverManager.getConnection(_connectionString, _userName, _password);
			
			preparedStatement = connection.prepareStatement(SELECT_SERVERS);
			
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next())
			{
				long current = dateFormat.parse(resultSet.getString(4)).getTime();
				long updated = dateFormat.parse(resultSet.getString(1)).getTime();
				long lastOnline = dateFormat.parse(resultSet.getString(2)).getTime();
				
				if (current - updated < 70000 && current - lastOnline > 660000)
					countOffline++;
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
			
			if (connection != null)
			{
				try
				{
					connection.close();
				} 
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}

		//if (countOffline > 5)
			System.out.println(countOffline + " offline bungees.");
		
		return true;
	}
}
