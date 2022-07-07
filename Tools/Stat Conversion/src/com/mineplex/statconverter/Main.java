package com.mineplex.statconverter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.google.common.io.Files;
import com.mineplex.statconverter.database.mysql.DBPool;
import com.mysql.jdbc.exceptions.jdbc4.MySQLDataException;

public class Main
{
	private static final String FILE_PATH_BASE;
	
	static
	{
		String path;
		try
		{
			path = new File(".").getCanonicalPath() + File.separator;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			path = "";
		}
		
		FILE_PATH_BASE = path;
	}
	
	public static void main(String[] args)
	{
		if (new File(FILE_PATH_BASE + "doBuckets.dat").exists())
		{
			new BucketFiller();
		}
		else
		{
			new Main();
		}
	}
	
	private File _info;
	private boolean _complete = false;
	private int _totalCompleted;
	private long _startTime;
	
	public Main()
	{
		int completed = 0;
		try
		{
			if (new File(FILE_PATH_BASE + "complete.dat").exists())
			{
				return;
			}
			
			_info = new File(FILE_PATH_BASE + "converterInfo.dat");
			System.out.println(FILE_PATH_BASE + "converterInfo.dat");
			
			if (_info.exists())
			{
				String completedStr = Files.readFirstLine(_info, Charset.defaultCharset());
				if (completedStr != null && !completedStr.isEmpty())
				{
					completed = Integer.parseInt(completedStr);
				}
			}
			else
			{
				_info.createNewFile();
				Files.write(String.valueOf(0).getBytes(), _info);
			}

			_totalCompleted = completed;
			
			while (!_complete)
			{
				convertGroup();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	private void convertGroup()
	{
		_startTime = System.currentTimeMillis();
		System.out.println("[INFO] Starting next batch of 10000 (Number " + (_totalCompleted + 1) + ")");
		Map<Integer[], Long> converting = new HashMap<>(10000);
		try (Connection c = DBPool.getDataSource("ACCOUNT_PC").getConnection())
		{
			try (Statement s = c.createStatement();
				ResultSet rs = s.executeQuery("SELECT accountId, statId, value FROM accountStat LIMIT 10000;")
				)
			{
				while (rs.next())
				{
					final int accountId = rs.getInt("accountId");
					final int statId = rs.getInt("statId");
					long value = 0;
					try
					{
						value = rs.getLong("value");
					}
					catch (MySQLDataException ex)
					{
						long played = 0;
						try (ResultSet r = s.executeQuery("SELECT name FROM stats WHERE id=" + statId + ";"))
						{
							r.next();
							String[] parts = r.getString(1).split(Pattern.quote("."));
							String type = parts[0];
							String stat = parts[1];
							if (!stat.equals("ExpEarned"))
							{
								System.out.println("VALUE IS TOO BIG BUT STAT IS NOT EXPEARNED");
								System.exit(0);
								return;
							}
							
							ResultSet playedSet;
							if (type.equals("Global"))
							{
								playedSet = s.executeQuery("SELECT SUM(value) FROM accountStat WHERE accountId=" + accountId + " AND statId IN (SELECT id FROM stats WHERE name LIKE '%.Wins' OR name LIKE '%.Losses');");
							}
							else
							{
								playedSet = s.executeQuery("SELECT SUM(value) FROM accountStat WHERE accountId=" + accountId + " AND statId IN (SELECT id FROM stats WHERE name IN ('" + type + ".Wins', '" + type + ".Losses'));");
							}
							if (playedSet.next())
							{
								try
								{
									played = playedSet.getLong(1);
								}
								catch (Exception anything) {}
								finally
								{
									playedSet.close();
								}
							}
						}
						
						long newValue = played * 625; //Average xp per game network-wide
						s.executeUpdate("UPDATE accountStat SET value=" + newValue + " WHERE accountId=" + accountId + " AND statId=" + statId + ";");
						value = newValue;
					}
					converting.put(new Integer[] {rs.getInt("accountId"), rs.getInt("statId")}, value);
				}
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			_complete = true;
			return;
		}
		
		try
		{
			if (!converting.isEmpty())
			{
				try (Connection c = DBPool.getDataSource("ACCOUNT_PC").getConnection();
					PreparedStatement select = c.prepareStatement("SELECT value FROM accountStatsAllTime WHERE accountId=? AND statId=?;");
					PreparedStatement update = c.prepareStatement("UPDATE accountStatsAllTime SET value=value+? WHERE accountId=? AND statId=?;");
					PreparedStatement insert = c.prepareStatement("INSERT INTO accountStatsAllTime (accountId, statId, value) VALUES (?, ?, ?);");
					PreparedStatement delete = c.prepareStatement("DELETE FROM accountStat WHERE accountId=? AND statId=?;");
					)
				{
					for (Entry<Integer[], Long> convertEntry : converting.entrySet())
					{
						int accountId = convertEntry.getKey()[0];
						int statId = convertEntry.getKey()[1];
						long value = convertEntry.getValue();
						select.setInt(1, accountId);
						select.setInt(2, statId);
						long fetched = -1;
						try (ResultSet rs = select.executeQuery())
						{
							if (rs.next())
							{
								fetched = rs.getLong("value");
							}
						}
						finally
						{
							select.clearParameters();
						}
						long diff = value - Math.max(fetched, 0);
						if (fetched == -1)
						{
							insert.setInt(1, accountId);
							insert.setInt(2, statId);
							insert.setLong(3, value);
							insert.execute();
							insert.clearParameters();
						}
						else if (value > 0 && fetched >= 2 * value)
						{
							update.setLong(1, -1 * value);
							update.setInt(2, accountId);
							update.setInt(3, statId);
							update.execute();
							update.clearParameters();
						}
						else if (diff > 0)
						{
							update.setLong(1, diff);
							update.setInt(2, accountId);
							update.setInt(3, statId);
							update.execute();
							update.clearParameters();
						}
						
						delete.setInt(1, accountId);
						delete.setInt(2, statId);
						delete.addBatch();
					}
					delete.executeBatch();
				}
				completeGroup();
			}
			else
			{
				System.out.println("[INFO] Conversion complete");
				try
				{
					new File(FILE_PATH_BASE + "complete.dat").createNewFile();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				_complete = true;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			_complete = true;
		}
	}

	private void completeGroup()
	{
		long timeTaken = System.currentTimeMillis() - _startTime;
		_startTime = 0;
		if (_info.delete())
		{
			try
			{
				_totalCompleted++;
				_info.createNewFile();
				Files.write(String.valueOf(_totalCompleted).getBytes(), _info);
				System.out.println("[INFO] Completed group " + _totalCompleted + " of 10000 in " + timeTaken + " milliseconds");
			}
			catch (IOException e)
			{
				e.printStackTrace();
				_complete = true;
			}
		}
		else
		{
			_complete = true;
		}
	}
}