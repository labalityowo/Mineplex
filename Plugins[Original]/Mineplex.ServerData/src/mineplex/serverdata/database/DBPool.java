package mineplex.serverdata.database;

import javax.sql.DataSource;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.Connection;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

public final class DBPool
{
	private static DataSource ACCOUNT;
	private static DataSource QUEUE;
	private static DataSource MINEPLEX;
	private static DataSource MINEPLEX_STATS;
	private static DataSource PLAYER_STATS;
	private static DataSource SERVER_STATS;
	private static DataSource MSSQL_MOCK;

	private static DataSource openDataSource(String url, String username, String password)
	{
		BasicDataSource source = new BasicDataSource();
		source.addConnectionProperty("autoReconnect", "true");
		source.addConnectionProperty("allowMultiQueries", "true");
		source.addConnectionProperty("zeroDateTimeBehavior", "convertToNull");
		source.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		source.setDriverClassName("com.mysql.jdbc.Driver");
		source.setUrl(url);
		source.setUsername(username);
		source.setPassword(password);
		source.setMaxTotal(5);
		source.setMaxIdle(5);
		source.setTimeBetweenEvictionRunsMillis(180 * 1000);
		source.setSoftMinEvictableIdleTimeMillis(180 * 1000);

		return source;
	}

	public static DataSource getMssqlMock()
	{
		if (MSSQL_MOCK == null)
			loadDataSources();

		return MSSQL_MOCK;
	}

	public static DataSource getAccount()
	{
		if (ACCOUNT == null)
			loadDataSources();

		return ACCOUNT;
	}

	public static DataSource getQueue()
	{
		if (QUEUE == null)
			loadDataSources();

		return QUEUE;
	}

	public static DataSource getMineplex()
	{
		if (MINEPLEX == null)
			loadDataSources();

		return MINEPLEX;
	}

	public static DataSource getMineplexStats()
	{
		if (MINEPLEX_STATS == null)
			loadDataSources();

		return MINEPLEX_STATS;
	}

	public static DataSource getPlayerStats()
	{
		if (PLAYER_STATS == null)
			loadDataSources();

		return PLAYER_STATS;
	}

	public static DataSource getServerStats()
	{
		if (SERVER_STATS == null)
			loadDataSources();

		return SERVER_STATS;
	}

	private static void loadDataSources()
	{
		try
		{
			File configFile = new File("database-config.dat");

			if (configFile.exists())
			{
				List<String> lines = Files.readAllLines(configFile.toPath(), Charset.defaultCharset());

				for (String line : lines)
				{
					deserializeConnection(line);
				}
			}
			else
			{
				System.out.println("database-config.dat not found at " + configFile.toPath().toString());
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
			System.out.println("---Unable To Parse DBPOOL Configuration File---");
		}
	}

	private static void deserializeConnection(String line)
	{
		String[] args = line.split(" ");

		if (args.length == 4)
		{
			String dbSource = args[0];
			String dbHost = args[1];
			String userName = args[2];
			String password = args[3];

			// System.out.println(dbSource + " " + dbHost + " " + userName + " " + password);

			if (dbSource.toUpperCase().equalsIgnoreCase("ACCOUNT"))
				ACCOUNT = openDataSource("jdbc:mysql://" + dbHost, userName, password);
			else if (dbSource.toUpperCase().equalsIgnoreCase("QUEUE"))
				QUEUE = openDataSource("jdbc:mysql://" + dbHost, userName, password);
			else if (dbSource.toUpperCase().equalsIgnoreCase("MINEPLEX"))
				MINEPLEX = openDataSource("jdbc:mysql://" + dbHost, userName, password);
			else if (dbSource.toUpperCase().equalsIgnoreCase("MINEPLEX_STATS"))
				MINEPLEX_STATS = openDataSource("jdbc:mysql://" + dbHost, userName, password);
			else if (dbSource.toUpperCase().equalsIgnoreCase("PLAYER_STATS"))
				PLAYER_STATS = openDataSource("jdbc:mysql://" + dbHost, userName, password);
			else if (dbSource.toUpperCase().equalsIgnoreCase("SERVER_STATS"))
				SERVER_STATS = openDataSource("jdbc:mysql://" + dbHost, userName, password);
			else if (dbSource.toUpperCase().equalsIgnoreCase("MSSQL_MOCK"))
				MSSQL_MOCK = openDataSource("jdbc:mysql://" + dbHost, userName, password);
		}
	}
}
