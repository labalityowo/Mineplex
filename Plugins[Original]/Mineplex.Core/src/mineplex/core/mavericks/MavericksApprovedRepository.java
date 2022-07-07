package mineplex.core.mavericks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import mineplex.serverdata.database.DBPool;

/**
 * Repository for Mavericks-MasterBuilders SQL game data
 * -
 * Table to back this repository may be created with:
 * 
 * 
  CREATE TABLE mavericksMasterBuildersApproved (
  buildId INT NOT NULL AUTO_INCREMENT,
  ApproveDate INT NOT NULL,
  ApprovedBy VARCHAR(36) NOT NULL DEFAULT '',
  Display TINYINT(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (buildId),
  CONSTRAINT account_id FOREIGN KEY (ApprovedBy) REFERENCES accounts (id) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT build_id FOREIGN KEY (BuildId) REFERENCES mavericksMasterBuildersBuilds (BuildId) ON DELETE NO ACTION ON UPDATE NO ACTION
  )
 */
public class MavericksApprovedRepository
{
	
	private static final String TABLE_APPROVED = "mavericksMasterBuildersApproved";
	private static final String TABLE_BUILD = "mavericksMasterBuildersBuilds";
	
	public CompletableFuture<Boolean> add(MavericksBuildWrapper data, UUID approvedBy)
	{
		return add(data, approvedBy, true);
	}
	
	public CompletableFuture<Boolean> add(MavericksBuildWrapper data, UUID approvedBy, boolean display)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection conn = DBPool.getAccount().getConnection())
			{
				PreparedStatement stmt = conn.prepareStatement("REPLACE INTO " + TABLE_APPROVED + " (BuildId, ApprovedBy, Display) SELECT ?, accounts.id, ? FROM accounts WHERE uuid=?");
				stmt.setLong(1, data.getBuildId());
				stmt.setBoolean(2, display);
				stmt.setString(3, approvedBy.toString());
				
				return stmt.executeUpdate() > 0;
			}
			catch(SQLException e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		});
	}
	
	public CompletableFuture<List<MavericksApprovedWrapper>> getToDisplay(boolean onlyDisplay, int limit, int offset)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection conn = DBPool.getAccount().getConnection()) 
			{
				String filter = onlyDisplay ? " WHERE Display=1 " : " ";
				
				PreparedStatement stmt = conn.prepareStatement(
						"SELECT " +
								TABLE_APPROVED + ".BuildId, ApprovedDate," +
									"(SELECT uuid FROM accounts WHERE accounts.id=ApprovedBy)," +
								"Display, FirstDisplayed," +
								"BuildTheme," +
									"(SELECT uuid FROM accounts WHERE accounts.id=accountId)," +
									"(SELECT name FROM accounts WHERE accounts.id=accountId)," +
								"Points, Place, Date, Schematic, Particles, Reviewed " +
							"FROM " + TABLE_APPROVED + " " +
							"INNER JOIN " + TABLE_BUILD + " " +
							"ON " + TABLE_APPROVED + ".BuildId = " + TABLE_BUILD + ".BuildId" + 
							filter + 
							"LIMIT " + limit + " OFFSET " + offset);
				
				ResultSet set = stmt.executeQuery();
				List<MavericksApprovedWrapper> list = new ArrayList<>();
				while (set.next())
				{	
					long buildId = set.getLong(1);
					
					long approvedDate = set.getLong(2);
					UUID approvedBy = UUID.fromString(set.getString(3));
					boolean display = set.getBoolean(4);
					Timestamp stamp = set.getTimestamp(5);
					Long firstDisplayed = null;
					if(!set.wasNull() && stamp != null) firstDisplayed = stamp.getTime();

					
					String theme = set.getString(6);
					UUID uuid = UUID.fromString(set.getString(7));
					String lastName = set.getString(8);
					int votes = set.getInt(9);
					int place = set.getInt(10);
					long dateStamp = set.getLong(11);
					byte[] schematic = set.getBytes(12);
					byte[] particles = set.getBytes(13);
					boolean reviewed = set.getBoolean(14);
					MavericksBuildWrapper build = new MavericksBuildWrapper(buildId, uuid, lastName, theme, votes, place, dateStamp, schematic, particles, reviewed);
					
					MavericksApprovedWrapper approved = new MavericksApprovedWrapper(build, approvedDate, approvedBy, display, firstDisplayed);
					
					list.add(approved);
				}
				return list;
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		});
	}
	
	public CompletableFuture<Boolean> setDisplay(boolean display, long... buildids)
	{
		if(buildids.length == 0) return null;
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection conn = DBPool.getAccount().getConnection())
			{
				
				String where = "WHERE BuildId=?";
				for(int i = 1; i < buildids.length; i++)
				{
					where += " OR BuildId=?";
				}
				PreparedStatement stmt = conn.prepareStatement("UPDATE " + TABLE_APPROVED + " SET Display=? " + where);
				stmt.setBoolean(1, display);
				for(int i = 0; i < buildids.length; i++)
				{
					stmt.setLong(2+i, buildids[i]);
				}
				
				return stmt.executeUpdate() > 0;
			}
			catch(SQLException e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		});
	}
	
	public CompletableFuture<Boolean> setDisplayDate(long buildid)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection conn = DBPool.getAccount().getConnection())
			{
				PreparedStatement stmt = conn.prepareStatement("UPDATE " + TABLE_APPROVED + " SET FirstDisplayed=? WHERE BuildId=?");
//				stmt.setDate(1, new Date(System.currentTimeMillis()));
				stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
				stmt.setLong(2, buildid);
				
				return stmt.executeUpdate() > 0;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		});
	}

}
