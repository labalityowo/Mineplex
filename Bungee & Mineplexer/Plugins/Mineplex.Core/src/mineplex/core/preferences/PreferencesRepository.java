package mineplex.core.preferences;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import mineplex.core.progression.util.SQLStatement;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;

public class PreferencesRepository extends RepositoryBase
{
	private static String UPSERT_ACCOUNT = "INSERT INTO `preferences` VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE `value`= ?;";
	private final String GET_PREFS = "SELECT * FROM `preferences` WHERE `accountId` = ?;";

	private final PreferencesManager _manager;

	public PreferencesRepository(PreferencesManager plugin)
	{
		super(DBPool.getAccount());
		_manager = plugin;
	}

	/**
	 * Save a player's preferences in SQL
	 *
	 * @param preferences The player's specific {@code {@link UserPreferences}} instance
	 */
	public void saveUserPreferences(UserPreferences preferences)
	{
		async(() ->
		{
			int accountId = preferences.getAccountId();
			try (Connection connection = getConnection())
			{
				for (Preference preference : Preference.values())
				{
					int value = preferences.isActive(preference) ? 1 : 0;
					int id = preference.getId();
					PreparedStatement statement = new SQLStatement(UPSERT_ACCOUNT)
					  .set(1, accountId).set(2, id).set(3, value).set(4, value)
					  .prepare(connection);
					executeUpdate(statement);
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});
	}

	/**
	 * This method is run on the main thread, as we need this information to be laoded before a player joins
	 * It's also not the true main thread, as it's being called on AsyncPlayerPreLoginEvent
	 * If, for whatever reason, you need this information sometime differently, please call this async.
	 *
	 * @param accountId The player's accountID
	 * @return A loaded preference for the player
	 */
	public UserPreferences loadClientInformation(int accountId)
	{
		UserPreferences preferences = new UserPreferences(accountId);

		executeQuery(GET_PREFS, resultSet ->
		{
			while (resultSet.next())
			{
				Preference preference = Preference.get(resultSet.getInt("preference"));
				boolean value = resultSet.getInt("value") == 1;
				preferences.set(preference, value);
			}
		}, new ColumnInt("accountId", accountId));

		return preferences;
	}

	/**
	 * Load a clients data async and then update the local reference
	 *
	 * @param accountId The player's accountID
	 */
	public void loadClientInformationAsync(int accountId)
	{
		async(() ->
		{
			UserPreferences preferences = loadClientInformation(accountId);
			_manager.set(accountId, preferences);
		});
	}

	public void async(Runnable runnable)
	{
		_manager.runAsync(runnable);
	}

	/**
	 * Internal method for updating the table
	 *
	 * @param preparedStatement The statement to execute
	 * @return The amount of rows effected
	 */
	private int executeUpdate(PreparedStatement preparedStatement)
	{
		try
		{
			return preparedStatement.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return -1;
		}
		finally
		{
			try
			{
				if (preparedStatement != null)
				{
					preparedStatement.close();
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
}