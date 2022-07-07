package mineplex.servermonitor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import mineplex.serverdata.Region;
import mineplex.serverdata.data.BungeeServer;
import mineplex.serverdata.data.DataRepository;
import mineplex.serverdata.data.DedicatedServer;
import mineplex.serverdata.data.ServerGroup;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.redis.RedisDataRepository;
import mineplex.serverdata.servers.ServerManager;

public class StatusHistoryRepository extends RepositoryBase
{
	private static String CREATE_GROUP_TABLE = "CREATE TABLE IF NOT EXISTS ServerGroupStats (id INT NOT NULL AUTO_INCREMENT, serverGroup VARCHAR(100), updated LONG, players INT, maxPlayers INT, totalNetworkCpuUsage DOUBLE(4,2), totalNetworkRamUsage DOUBLE(4,2), totalCpu MEDIUMINT, totalRam MEDIUMINT, US BOOLEAN NOT NULL DEFAULT '1', PRIMARY KEY (id));";
	private static String CREATE_DEDICATED_TABLE = "CREATE TABLE IF NOT EXISTS DedicatedServerStats (id INT NOT NULL AUTO_INCREMENT, serverName VARCHAR(100), address VARCHAR(25), updated LONG, cpu TINYINT, ram MEDIUMINT, usedCpuPercent DOUBLE(4,2), usedRamPercent DOUBLE(4,2), US BOOLEAN NOT NULL DEFAULT '1', PRIMARY KEY (id));";
	private static String CREATE_BUNGEE_TABLE = "CREATE TABLE IF NOT EXISTS BungeeStats (id INT NOT NULL AUTO_INCREMENT, address VARCHAR(25), updated LONG, players INT, maxPlayers INT, alive BOOLEAN NOT NULL, online BOOLEAN NOT NULL, US BOOLEAN NOT NULL DEFAULT '1', PRIMARY KEY (id));";
	private static String CREATE_NETWORKSTATS_TABLE = "CREATE TABLE IF NOT EXISTS NetworkStats (id INT NOT NULL AUTO_INCREMENT, updated LONG, players INT, totalNetworkCpuUsage DOUBLE(4,2), totalNetworkRamUsage DOUBLE(4,2), totalCpu MEDIUMINT, totalRam MEDIUMINT, US BOOLEAN NOT NULL DEFAULT '1', PRIMARY KEY (id));";
	
	private static String INSERT_SERVERGROUP_STATS = "INSERT INTO ServerGroupStats (serverGroup, updated, players, maxPlayers, totalNetworkCpuUsage, totalNetworkRamUsage, totalCpu, totalRam, US) VALUES (?, now(), ?, ?, ?, ?, ?, ?, ?);";
	private static String INSERT_DEDICATEDSERVER_STATS = "INSERT INTO DedicatedServerStats (serverName, address, updated, cpu, ram, usedCpuPercent, usedRamPercent, US) VALUES (?, ?, now(), ?, ?, ?, ?, ?);";
	private static String INSERT_BUNGEE_STATS = "INSERT INTO BungeeStats (address, updated, players, maxPlayers, alive, online, US) VALUES (?, now(), ?, ?, ?, ?, ?);";
	private static String INSERT_NETWORK_STATS = "INSERT INTO NetworkStats (updated, players, totalNetworkCpuUsage, totalNetworkRamUsage, totalCpu, totalRam, US) VALUES (now(), ?, ?, ?, ?, ?, ?);";
	
	private static DataRepository<BungeeServer> _repository;
	
	public StatusHistoryRepository()
	{
		super(DBPool.getServerStats());

		PreparedStatement preparedStatement = null;

		try(Connection connection = getConnection())
		{
			// Create table
			preparedStatement = connection.prepareStatement(CREATE_GROUP_TABLE);
			preparedStatement.execute();
			preparedStatement.close();
			
			// Create table
			preparedStatement = connection.prepareStatement(CREATE_DEDICATED_TABLE);
			preparedStatement.execute();
			preparedStatement.close();
			
			// Create table
			preparedStatement = connection.prepareStatement(CREATE_BUNGEE_TABLE);
			preparedStatement.execute();
			preparedStatement.close();
			
			// Create table
			preparedStatement = connection.prepareStatement(CREATE_NETWORKSTATS_TABLE);
			preparedStatement.execute();
			preparedStatement.close();
			
			_repository = new RedisDataRepository<BungeeServer>(ServerManager.getConnection(true, ServerManager.SERVER_STATUS_LABEL), ServerManager.getConnection(false, ServerManager.SERVER_STATUS_LABEL),
					Region.ALL, BungeeServer.class, "bungeeServers");
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

	public void saveServerGroupStats(int totalCpu, int totalRam, Collection<ServerGroup> collection)
	{
		PreparedStatement preparedStatement = null;
		
		try(Connection connection = getConnection())
		{
			preparedStatement = connection.prepareStatement(INSERT_SERVERGROUP_STATS);
			
			for (ServerGroup serverGroup : collection)
			{
				int serverCpu = serverGroup.getServerCount() * serverGroup.getRequiredCpu();
				int serverRam = serverGroup.getServerCount() * serverGroup.getRequiredRam();
				
				preparedStatement.setString(1, serverGroup.getName());
				preparedStatement.setInt(2, serverGroup.getPlayerCount());
				preparedStatement.setInt(3, serverGroup.getMaxPlayerCount());
				preparedStatement.setDouble(4, (double)serverCpu / (double)totalCpu * 100d);
				preparedStatement.setDouble(5, (double)serverRam / (double)totalRam * 100d);
				preparedStatement.setInt(6, serverCpu);
				preparedStatement.setInt(7, serverRam);
				preparedStatement.setBoolean(8, serverGroup.getRegion() == Region.US);
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
	
	public void saveDedicatedServerStats(List<DedicatedServer> dedicatedServers)
	{
		PreparedStatement preparedStatement = null;
		
		try(Connection connection = getConnection())
		{
			preparedStatement = connection.prepareStatement(INSERT_DEDICATEDSERVER_STATS);
			
			for (DedicatedServer dedicatedServer : dedicatedServers)
			{
				double usedCpu = dedicatedServer.getMaxCpu() == 0 ? 0 : (1d - (double)dedicatedServer.getAvailableCpu() / (double)dedicatedServer.getMaxCpu()) * 100d;
				double usedRam = dedicatedServer.getMaxRam() == 0 ? 0 : (1d - (double)dedicatedServer.getAvailableRam() / (double)dedicatedServer.getMaxRam()) * 100d;
								
				preparedStatement.setString(1, dedicatedServer.getName());
				preparedStatement.setString(2, dedicatedServer.getPrivateAddress());
				preparedStatement.setInt(3, dedicatedServer.getMaxCpu());
				preparedStatement.setInt(4, dedicatedServer.getMaxRam());
				preparedStatement.setDouble(5, usedCpu);
				preparedStatement.setDouble(6, usedRam);
				preparedStatement.setBoolean(7, dedicatedServer.getRegion() == Region.US);
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

	public void saveNetworkStats(double usedCpuPercent, double usedRamPercent, double availableCPU, double availableRAM, Region region)
	{
		int totalPlayers = 0;
		
		List<BungeeServer> bungeeServers = new ArrayList<BungeeServer>(_repository.getElements());
		
		for (Iterator<BungeeServer> bungeeIterator = bungeeServers.iterator(); bungeeIterator.hasNext();)
		{
			BungeeServer server = bungeeIterator.next();
			
			if (server.getPublicAddress().equalsIgnoreCase("127.0.0.1") || server.getPublicAddress().equalsIgnoreCase("0.0.0.0"))
				bungeeIterator.remove();
		}
		
		PreparedStatement preparedStatement = null;
		
		try(Connection connection = getConnection())
		{
			preparedStatement = connection.prepareStatement(INSERT_BUNGEE_STATS);
			
			for (BungeeServer bungeeStatusData : bungeeServers)
			{
				totalPlayers += bungeeStatusData.getPlayerCount();
				preparedStatement.setString(1, bungeeStatusData.getPublicAddress());
				preparedStatement.setInt(2, bungeeStatusData.getPlayerCount());
				preparedStatement.setInt(3, bungeeStatusData.getPlayerCount());
				preparedStatement.setBoolean(4, true);
				preparedStatement.setBoolean(5, true);
				preparedStatement.setBoolean(6, bungeeStatusData.getRegion() == Region.US);
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
		
		preparedStatement = null;
		
		try(Connection connection = getConnection())
		{
			preparedStatement = connection.prepareStatement(INSERT_NETWORK_STATS);
			preparedStatement.setInt(1, totalPlayers);
			preparedStatement.setDouble(2, usedCpuPercent);
			preparedStatement.setDouble(3, usedRamPercent);
			preparedStatement.setInt(4, (int)availableCPU);
			preparedStatement.setInt(5, (int)availableRAM);
			preparedStatement.setBoolean(6, region == Region.US);
			
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
