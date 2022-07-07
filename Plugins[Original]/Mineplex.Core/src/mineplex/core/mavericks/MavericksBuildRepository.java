package mineplex.core.mavericks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import mineplex.serverdata.database.DBPool;

/**
 * Repository for Mavericks-MasterBuilders SQL game data
 * -
 * Table to back this repository may be created with
 * 
    CREATE TABLE IF NOT EXISTS  mavericksMasterBuildersBuilds (
	accountId INT NOT NULL,
	BuildTheme VARCHAR(255) NOT NULL,
	Points DOUBLE NOT NULL,
	Place INT NOT NULL,
	Date BIGINT NOT NULL,
	Schematic BLOB,
	Reviewed TINYINT,
	PRIMARY KEY (accountId,Date),
	FOREIGN KEY (accountId) REFERENCES accounts(id) ON DELETE NO ACTION ON UPDATE NO ACTION
	);
 */
public class MavericksBuildRepository
{
	private static final String TABLE = "mavericksMasterBuildersBuilds";
	
	public CompletableFuture<List<MavericksBuildWrapper>> getToReview(boolean onlyUnreviewed, int limit, int offset)
	{
		return getToReview(onlyUnreviewed, limit, offset, true);
	}
	
	public CompletableFuture<List<MavericksBuildWrapper>> getToReview(boolean onlyUnreviewed, int limit, int offset, boolean parseData)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection conn = DBPool.getAccount().getConnection()) 
			{
				String filter = onlyUnreviewed ? "WHERE Reviewed=0 " : "";
				PreparedStatement stmt = conn.prepareStatement("SELECT BuildId," + 
							"(SELECT uuid from accounts WHERE accounts.id=" + TABLE + ".accountId)," + 
							"(SELECT name from accounts WHERE accounts.id=" + TABLE + ".accountId)," + 
							"BuildTheme,Points,Place,Date,Schematic,Particles,Reviewed FROM " + TABLE + " " + filter + 
							" ORDER BY Points DESC LIMIT " + limit + " OFFSET " + offset);
				
				ResultSet set = stmt.executeQuery();
				List<MavericksBuildWrapper> list = new ArrayList<>();
				while (set.next())
				{
					long buildId = set.getLong(1);
					UUID uuid = UUID.fromString(set.getString(2));
					String lastName = set.getString(3);
					String theme = set.getString(4);
					int votes = set.getInt(5);
					int place = set.getInt(6);
					long dateStamp = set.getLong(7);
					byte[] schematic = set.getBytes(8);
					byte[] particles = set.getBytes(9);
					boolean reviewed = set.getBoolean(10);
					MavericksBuildWrapper data = new MavericksBuildWrapper(buildId, uuid, lastName, theme, votes, place, dateStamp, schematic, particles, reviewed);
					if (parseData)
					{
						data.getParticles();
						data.getSchematic();
					}
					list.add(data);
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
	
	public CompletableFuture<Boolean> setReviewed(long buildId, boolean reviewed)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection conn = DBPool.getAccount().getConnection())
			{
				
				PreparedStatement stmt = conn.prepareStatement("UPDATE " + TABLE + " SET Reviewed=? WHERE BuildId=?");
				stmt.setBoolean(1, reviewed);
				stmt.setLong(2, buildId);
				
				return stmt.executeUpdate() > 0;
			}
			catch(SQLException e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		});
	}
	
	public CompletableFuture<Boolean> add(MavericksBuildWrapper data)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection conn = DBPool.getAccount().getConnection())
			{
				
				PreparedStatement stmt = conn.prepareStatement("REPLACE INTO " + TABLE + " (accountId, BuildTheme, Points, Place, Date, Schematic, Particles) SELECT accounts.id, ?, ?, ?, ?, ?, ? FROM accounts WHERE uuid=?");
				stmt.setString(1, data.getTheme());
				stmt.setDouble(2, data.getPoints());
				stmt.setInt(3, data.getPlace());
				stmt.setLong(4, data.getDateStamp());
				stmt.setBytes(5, data.getSchematicBytes());
				stmt.setBytes(6, data.getParticlesRaw());
				stmt.setString(7, data.getUUID().toString());
				
				return stmt.executeUpdate() > 0;
			}
			catch(SQLException e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		});
	}
}