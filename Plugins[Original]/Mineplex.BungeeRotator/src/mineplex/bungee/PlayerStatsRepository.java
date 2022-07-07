package mineplex.bungee;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerStatsRepository extends RepositoryBase
{
	private static String SELECT_IPINFO = "SELECT id, ipAddress FROM ipInfo WHERE regionName IS NULL LIMIT 1000;";
	private static String UPDATE_IPINFO = "UPDATE ipInfo SET countryCode = ?, countryName = ?, regionCode = ?, regionName = ?, city = ?, zipCode = ?, timeZone = ?, latitude = ?, longitude = ?, metroCode = ? WHERE id = ?;";

	public PlayerStatsRepository()
	{
		super(DBPool.getPlayerStats());
	}

	public List<IpInfo> getIpAddresses()
	{
		List<IpInfo> ipinfos = new ArrayList<IpInfo>(1000);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try(Connection connection = getConnection())
		{
			preparedStatement = connection.prepareStatement(SELECT_IPINFO);
			
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next())
			{
				IpInfo ipInfo = new IpInfo();
				ipInfo.id = resultSet.getInt(1);
				ipInfo.ipAddress = resultSet.getString(2);
				
				ipinfos.add(ipInfo);
			}
			
			resultSet.close();
			preparedStatement.close();
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
		
		return ipinfos;
	}
	
	public void updateIps(List<IpInfo> ips)
	{
		PreparedStatement preparedStatement = null;
		
		try(Connection connection = getConnection())
		{
			preparedStatement = connection.prepareStatement(UPDATE_IPINFO);
			
			for (IpInfo ipInfo : ips)
			{
				preparedStatement.setString(1, ipInfo.countryCode);
				preparedStatement.setString(2, ipInfo.countryName);
				preparedStatement.setString(3, ipInfo.regionCode);
				preparedStatement.setString(4, ipInfo.regionName);
				preparedStatement.setString(5, ipInfo.city);
				preparedStatement.setString(6, ipInfo.zipCode);
				preparedStatement.setString(7, ipInfo.timeZone);
				preparedStatement.setDouble(8, ipInfo.latitude);
				preparedStatement.setDouble(9, ipInfo.longitude);
				preparedStatement.setInt(10, ipInfo.metroCode);
				preparedStatement.setInt(11, ipInfo.id);
				
				preparedStatement.addBatch();
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
