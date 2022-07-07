package com.mineplex.statconverter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.google.common.io.Files;
import com.mineplex.statconverter.database.mysql.DBPool;

public class BucketFiller
{
	private File _info;
	
	private boolean _complete = false;
	
	private List<Integer> _statIds;
	private int _nextStat;
	private int _nextStartLimit;
	
	public BucketFiller()
	{
		int stat = 0;
		int startLimit = 0;
		try
		{
			if (new File(new File(".").getCanonicalPath() + File.separator + "completeBuckets.dat").exists())
			{
				return;
			}
			_info = new File(new File(".").getCanonicalPath() + File.separator + "bucketInfo.dat");
			System.out.println(_info.getCanonicalPath());
			
			_statIds = loadStatIds();
			if (_statIds.isEmpty() || _statIds == null)
			{
				return;
			}
			
			if (_info.exists())
			{
				String startStr = Files.readFirstLine(_info, Charset.defaultCharset());
				if (startStr != null && !startStr.isEmpty() && startStr.contains(",") && !startStr.endsWith(",") && !startStr.startsWith(","))
				{
					String[] starts = startStr.split(Pattern.quote(","));
					stat = Integer.parseInt(starts[0]);
					startLimit = Integer.parseInt(starts[1]);
				}
			}
			else
			{
				_info.createNewFile();
				stat = _statIds.get(0);
				Files.write((stat + ",0").getBytes(), _info);
			}
			
			_nextStat = stat;
			_nextStartLimit = startLimit;
			
			while (!_complete)
			{
				try
				{
					convertStat(_nextStat, _nextStartLimit);
				}
				catch (SQLException | IOException e)
				{
					e.printStackTrace();
					break;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	private List<Integer> loadStatIds()
	{
		List<Integer> ids = new ArrayList<>();
		
		try (Connection c = DBPool.getDataSource("ACCOUNT_PC").getConnection();
			PreparedStatement ps = c.prepareStatement("SELECT id FROM stats ORDER BY id DESC;");
			ResultSet rs = ps.executeQuery();
			)
		{
			while (rs.next())
			{
				ids.add(rs.getInt("id"));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
		
		return ids;
	}
	
	private void convertStat(int stat, int start) throws SQLException, IOException
	{
		System.out.println("[INFO] Starting " + stat + ": " + start + " to " + (start + 9999));
		Map<Integer, Long> accounts = new HashMap<>();
		try (Connection c = DBPool.getDataSource("ACCOUNT_PC").getConnection())
		{
			try (Statement s = c.createStatement();
				ResultSet rs = s.executeQuery("SELECT accountId, value FROM accountStatsAllTime WHERE statId=" + stat + " LIMIT " + start + ",10000;")
				)
			{
				while (rs.next())
				{
					accounts.put(rs.getInt("accountId"), rs.getLong("value"));
				}
			}
			
			if (!accounts.isEmpty())
			{
				for (Entry<Integer, Long> entry : accounts.entrySet())
				{
					Long value = entry.getValue();
					try (Statement s = c.createStatement())
					{
						s.execute("UPDATE accountStatsAllTimeBuckets SET playerCount=playerCount+1 WHERE minScore <= " + value + " AND maxScore >= " + value + " AND statId=" + stat + " LIMIT 1;");
					}
				}
				completeStatGroup(stat, start);
			}
			else
			{
				try
				{
					completeStat(stat);
				}
				catch (IndexOutOfBoundsException ex)
				{
					complete();
				}
			}
		}
	}
	
	private void completeStatGroup(int stat, int start) throws IOException
	{
		_nextStartLimit = start + 10000;
		if (_info.delete())
		{
			_info.createNewFile();
			Files.write((stat + "," + start).getBytes(), _info);
			System.out.println("[INFO] Completed " + stat + ": " + start + " to " + (_nextStartLimit - 1));
		}
		else
		{
			throw new IOException("Unable to delete info file");
		}
	}
	
	private void completeStat(int stat) throws IOException, IndexOutOfBoundsException
	{
		if (_info.delete())
		{
			_info.createNewFile();
			Files.write((stat + "," + 0).getBytes(), _info);
			System.out.println("[INFO] Conversion of stat " + stat + " complete");
		}
		else
		{
			throw new IOException("Unable to delete info file");
		}
		_nextStat = _statIds.get(_statIds.indexOf(stat) + 1);
		_nextStartLimit = 0;
	}
	
	private void complete() throws IOException
	{
		_complete = true;
		System.out.println("[INFO] Conversion of buckets complete");
		new File(new File(".").getCanonicalPath() + File.separator + "completeBuckets.dat").createNewFile();
	}
}