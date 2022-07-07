package com.mineplex.statconverter.database.mysql;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

public final class DBPool
{
	private static final Map<String, DataSource> _dataSources = new ConcurrentHashMap<>();

	public static DataSource getDataSource(String databaseId)
	{
		if (_dataSources == null || _dataSources.isEmpty())
		{
			loadDataSources();
		}
		return _dataSources.get(databaseId);
	}

	private static DataSource openDataSource(String url, String username, String password)
	{
		BasicDataSource source = new BasicDataSource();
		source.addConnectionProperty("autoReconnect", "true");
		source.addConnectionProperty("allowMultiQueries", "true");
		source.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		source.setDriverClassName("com.mysql.jdbc.Driver");
		source.setUrl(url);
		source.setUsername(username);
		source.setPassword(password);
		source.setMaxTotal(3);
		source.setMaxIdle(3);
		source.setTimeBetweenEvictionRunsMillis(180 * 1000);
		source.setSoftMinEvictableIdleTimeMillis(180 * 1000);

		return source;
	}

	private static void loadDataSources()
	{
		Map<String, DataSource> dataSources = new HashMap<>();
		try
		{
			File configFile = new File(new File(".").getCanonicalPath() + File.separator + "database-config.dat");

			if (configFile.exists())
			{
				List<String> lines = Files.readAllLines(configFile.toPath(),
						Charset.defaultCharset());

				for (String line : lines)
				{
					String[] args = line.split(" ");

					if (args.length == 4)
					{
						String dbId = args[0];
						String dbHost = args[1];
						String userName = args[2];
						String password = args[3];
						
						dataSources.put(dbId, openDataSource("jdbc:mysql://" + dbHost, userName, password));
					}
				}
			}
			else
			{
				System.out.println("database-config.dat not found at "
						+ configFile.toPath().toString());
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
			System.out.println("---Unable To Parse DBPOOL Configuration File---");
		}
		
		_dataSources.replaceAll((database, source) -> dataSources.getOrDefault(database, source));
		dataSources.forEach((database, source) -> _dataSources.putIfAbsent(database, source));
		_dataSources.entrySet().removeIf(entry -> !dataSources.containsKey(entry.getKey()));
	}
}