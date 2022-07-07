package mineplex.core.antihack.logging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import mineplex.core.common.util.UtilTasks;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;

public class AnticheatDatabase extends RepositoryBase
{
	/*
		CREATE TABLE IF NOT EXISTS anticheat_ban_metadata (id INT NOT NULL AUTO_INCREMENT, accountId INT, banId CHAR(10) NOT NULL, data MEDIUMTEXT NOT NULL, PRIMARY KEY(id));
	 */

	private static final String INSERT_INTO_METADATA = "INSERT INTO anticheat_ban_metadata (accountId, banId, data) VALUES (?, ?, ?);";

	public AnticheatDatabase()
	{
		super(DBPool.getAccount());
	}

	public void saveMetadata(int accountId, String id, String base64, Runnable after)
	{
		try (Connection connection = getConnection())
		{
			PreparedStatement statement = connection.prepareStatement(INSERT_INTO_METADATA);
			statement.setInt(1, accountId);
			statement.setString(2, id);
			statement.setString(3, base64);

			statement.executeUpdate();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			if (after != null)
			{
				UtilTasks.onMainThread(after).run();
			}
		}
	}
}