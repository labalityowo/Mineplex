package mineplex.core.teamspeak;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;

@SuppressWarnings("WeakerAccess")
public class TeamspeakRepository extends RepositoryBase
{
	TeamspeakRepository()
	{
		super(DBPool.getAccount());
	}

	public static final String GET_ALL_PLAYER_IDS = "SELECT `teamspeakId`, `linkDate` FROM accountTeamspeak WHERE accountId = %s;";
	public static final String SAVE_PLAYER_ID = "INSERT INTO accountTeamspeak (accountId, teamspeakId, linkDate) VALUES (?, ?, ?);";
	public static final String DELETE_PLAYER_ID = "DELETE FROM accountTeamspeak WHERE accountId = ? AND teamspeakId = ?;";

	public void save(int accountId, int id, Date now)
	{
		try (Connection connection = getConnection())
		{
			try (PreparedStatement statement = connection.prepareStatement(SAVE_PLAYER_ID))
			{
				statement.setInt(1, accountId);
				statement.setInt(2, id);
				statement.setDate(3, new java.sql.Date(now.getTime()));
				statement.executeUpdate();
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
	}

	public void delete(int accountId, int id)
	{
		try (Connection connection = getConnection())
		{
			try (PreparedStatement statement = connection.prepareStatement(DELETE_PLAYER_ID))
			{
				statement.setInt(1, accountId);
				statement.setInt(2, id);
				statement.executeUpdate();
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
	}
}
