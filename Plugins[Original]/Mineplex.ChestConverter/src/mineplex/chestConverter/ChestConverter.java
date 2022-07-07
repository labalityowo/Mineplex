package mineplex.chestConverter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ChestConverter
{
	private static ChestConverterRepository _repository = null;
	private static SimpleDateFormat _dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

	private static Logger _logger = Logger.getLogger("Converter");
	
	private static String _connectionString = "jdbc:mysql://db.mineplex.com:3306/Account?allowMultiQueries=true";
	private static String _userName = "MilitaryPolice";
	private static String _password = "CUPr6Wuw2Rus$qap";
	
	private static Connection _connection;
	
	public static void main (String args[])
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch (ClassNotFoundException e1)
		{
			e1.printStackTrace();
		}
		
		try
		{
			FileHandler fileHandler = new FileHandler("converter.log", true);
			fileHandler.setFormatter(new Formatter()
			{
				@Override
				public String format(LogRecord record)
				{
					return record.getMessage() + "\n";
				}
			});
			_logger.addHandler(fileHandler);
			_logger.setUseParentHandlers(false);
		}
		catch (SecurityException | IOException e1)
		{
			e1.printStackTrace();
		}
		
		int limit = 50000;
		HashSet<AccountStat> accountStats = new HashSet<AccountStat>();
		
		while (true)
		{
			accountStats.clear();
			
			try
			{
				Statement statement = null;
				
				try
				{
					if (_connection == null || _connection.isClosed())
						_connection = DriverManager.getConnection(_connectionString, _userName, _password);
	
					statement =  _connection.createStatement();
	
					statement.execute("SELECT accountId, statId, value FROM Account.accountStats LIMIT " + limit + ";");
					
					ResultSet resultSet = statement.getResultSet();
					
					while (resultSet.next())
					{
						accountStats.add(new AccountStat(resultSet.getInt(1), resultSet.getInt(2), resultSet.getInt(3)));
					}
				}
				catch (Exception exception)
				{
					exception.printStackTrace();
				}
				finally
				{
					if (statement != null)
					{
						try
						{
							statement.close();
						} 
						catch (SQLException e)
						{
							e.printStackTrace();
						}
					}
				}
				
				if (accountStats.size() == 0)
				{	
					System.out.println("No accounts.");
					return;
				}
				
				PreparedStatement updateStatement = null;
				PreparedStatement insertStatement = null;
				Statement deleteStatement = null;
				
				try
				{
					if (_connection == null || _connection.isClosed())
						_connection = DriverManager.getConnection(_connectionString, _userName, _password);

					_connection.setAutoCommit(true);
					updateStatement =  _connection.prepareStatement("UPDATE accountStat SET value = value + ? WHERE accountId = ? AND statId = ? AND value < ?;");
					
					for (AccountStat stat : accountStats)
					{
						updateStatement.setLong(1, stat.value);
						updateStatement.setInt(2, stat.accountId);
						updateStatement.setInt(3, stat.statId);
						updateStatement.setLong(4, stat.value);
						
						updateStatement.addBatch();
					}
					
					int[] rowsAffected = updateStatement.executeBatch();
					_connection.setAutoCommit(false);
					int i = 0;
					int count = 0;
					
					log("Updated rows - " + limit);
					
					insertStatement =  _connection.prepareStatement("INSERT IGNORE accountStat(accountId, statId, value) VALUES (?, ?, ?);");
					
					for (AccountStat stat : accountStats)
					{
						if (rowsAffected[i] < 1)
						{
							insertStatement.setInt(1, stat.accountId);
							insertStatement.setInt(2, stat.statId);
							insertStatement.setLong(3, stat.value);
							
							insertStatement.addBatch();
							count++;
						}
						
						i++;
					}
					
					insertStatement.executeBatch();
					log("Inserted rows - " + count);

					deleteStatement =  _connection.createStatement();
					deleteStatement.executeUpdate("DELETE FROM accountStats LIMIT " + limit + ";");
					
					_connection.commit();
					
					log("Deleted rows - " + limit);
				}
				catch (Exception exception)
				{
					exception.printStackTrace();
				}
				finally
				{
					if (statement != null)
					{
						try
						{
							statement.close();
						} 
						catch (SQLException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
			catch (Exception e)
			{
				_logger.info(e.getMessage());			
			}
		}
	}
	
	private static void log(String message)
	{
		System.out.println("[" + _dateFormat.format(new Date()) + "] " + message);
	}
}
