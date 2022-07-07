package mineplex.bungee.playerStats;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import mineplex.bungee.playerStats.data.IpInfo;
import mineplex.cache.player.PlayerInfo;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;

import javax.sql.DataSource;

public class PlayerStatsRepository extends RepositoryBase
{
	private static String INSERT_PLAYERINFO = "INSERT INTO playerInfo (uuid, name, version) VALUES (?, ?, ?);";
	private static String SELECT_PLAYERINFO = "SELECT id, name, version FROM playerInfo WHERE uuid = ?;";
	private static String UPDATE_PLAYERINFO = "UPDATE playerInfo SET name = ?, version = ? WHERE id = ?;";
	
	private static String INSERT_IPINFO = "INSERT INTO ipInfo (ipAddress) VALUES (?);";
	private static String SELECT_IPINFO = "SELECT id FROM ipInfo WHERE ipAddress = ?;";
	
	private static String UPDATE_PLAYERSTATS = "INSERT IGNORE INTO playerIps (playerInfoId, ipInfoId, date) VALUES (?, ?, curdate());"
											+ "INSERT IGNORE INTO playerUniqueLogins (playerInfoId, day) values(?, curdate());"
											+ "INSERT IGNORE INTO playerLoginSessions (playerInfoId, loginTime) values(?, now());";

	private static String UPDATE_LOGINSESSION = "UPDATE playerLoginSessions SET timeInGame = TIME_TO_SEC(TIMEDIFF(now(), loginTime)) / 60 WHERE id = ?;";

	public PlayerStatsRepository()
	{
		super(DBPool.getPlayerStats());
	}

	public PlayerInfo getPlayer(UUID uuid, String name, int version)
	{
		PlayerInfo playerInfo = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try(Connection connection = getConnection())
		{
			preparedStatement = connection.prepareStatement(SELECT_PLAYERINFO);
			
			preparedStatement.setString(1, uuid.toString());
			
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next())
			{
				playerInfo = new PlayerInfo(resultSet.getInt(1), uuid, resultSet.getString(2), resultSet.getInt(3));
			}
			
			resultSet.close();
			preparedStatement.close();
			
			if (playerInfo == null)
			{
				preparedStatement = connection.prepareStatement(INSERT_PLAYERINFO, Statement.RETURN_GENERATED_KEYS);
				preparedStatement.setString(1, uuid.toString());
				preparedStatement.setString(2, name);
				preparedStatement.setInt(3, version);
				
				preparedStatement.executeUpdate();
				
				int id = 0;
				
				resultSet = preparedStatement.getGeneratedKeys();
				
				while (resultSet.next())
				{
					id = resultSet.getInt(1);
				}
				
				playerInfo = new PlayerInfo(id, uuid, name, version);
				
				resultSet.close();
				preparedStatement.close();
	        }
			else if (!playerInfo.getName().equalsIgnoreCase(name) || playerInfo.getVersion() != version)
			{
				preparedStatement = connection.prepareStatement(UPDATE_PLAYERINFO);
				preparedStatement.setString(1, name);
				preparedStatement.setInt(2, version);
				preparedStatement.setInt(3, playerInfo.getId());
				
				preparedStatement.executeUpdate();
				preparedStatement.close();
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
			
			if (resultSet != null)
			{
				try
				{
					resultSet.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return playerInfo;
	}
	
	public IpInfo getIp(String ipAddress)
	{
		IpInfo ipInfo = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try(Connection connection = getConnection())
		{
			preparedStatement = connection.prepareStatement(SELECT_IPINFO);
			preparedStatement.setString(1, ipAddress);
			
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next())
			{
				ipInfo = new IpInfo();
				ipInfo.id = resultSet.getInt(1);
				ipInfo.ipAddress = ipAddress;
			}
			
			resultSet.close();
			preparedStatement.close();
			
			if (ipInfo == null)
			{
				preparedStatement = connection.prepareStatement(INSERT_IPINFO, Statement.RETURN_GENERATED_KEYS);
				preparedStatement.setString(1, ipAddress);
				
				preparedStatement.executeUpdate();
				
				int id = 0;
				
				resultSet = preparedStatement.getGeneratedKeys();
				
				while (resultSet.next())
				{
					id = resultSet.getInt(1);
				}
				
				ipInfo = new IpInfo();
				ipInfo.id = id;
				ipInfo.ipAddress = ipAddress;
				
				resultSet.close();
				preparedStatement.close();
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
			
			if (resultSet != null)
			{
				try
				{
					resultSet.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return ipInfo;
	}
	
	public int updatePlayerStats(int playerId, int ipId)
	{
		Statement statement = null;
		ResultSet resultSet= null;
		
		try(Connection connection = getConnection())
		{
			statement = connection.createStatement();

			String queryString = UPDATE_PLAYERSTATS;
			queryString = queryString.replaceFirst("\\?", playerId + "");
			queryString = queryString.replaceFirst("\\?", ipId + "");
			queryString = queryString.replaceFirst("\\?", playerId + "");
			queryString = queryString.replaceFirst("\\?", playerId + "");
			
			statement.executeUpdate(queryString, Statement.RETURN_GENERATED_KEYS);
			
			statement.getMoreResults();
			statement.getMoreResults();
			resultSet = statement.getGeneratedKeys();
			
			while (resultSet.next())
			{
				return resultSet.getInt(1);
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		finally
		{
			try
			{
				if (statement != null)
					statement.close();
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
			
			try
			{
				if (resultSet != null)
					resultSet.close();
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
		
		return -1;
	}
	
	public void updatePlayerSession(int loginSessionId)
	{
		PreparedStatement preparedStatement = null;
		
		try(Connection connection = getConnection())
		{
			preparedStatement = connection.prepareStatement(UPDATE_LOGINSESSION);
			preparedStatement.setInt(1, loginSessionId);
			
			preparedStatement.executeUpdate();
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
